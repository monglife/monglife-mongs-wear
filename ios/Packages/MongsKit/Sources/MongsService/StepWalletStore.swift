import Foundation
import MongsModel

/// 걸음 지갑 저장소
///
/// Android `data/device-data/.../persistence/datastore/DeviceDataStore.kt` 의
/// 걸음 부분(273 LOC 중 절반가량) 이식.
///
/// **actor 인 것이 곧 트랜잭션이다.** Android 는 `store.edit { }` 안에서 읽기-계산-쓰기를
/// 한 번에 끝내야 했다 — HealthKit 알림과 폴링이 동시에 돌면 밖에서 계산하고 나중에 저장하는
/// 방식은 적립을 통째로 잃는다. actor 는 메서드 하나가 원자적으로 돌므로 같은 보장을 준다.
public actor StepWalletStore {

    private static let key = "mongs.step.wallet.v1"

    private let store: any KeyValueStore
    private var state: StepWalletState

    public init(store: any KeyValueStore = UserDefaultsStore()) {
        self.store = store

        if let data = store.data(forKey: Self.key),
           let decoded = try? JSONDecoder().decode(StepWalletState.self, from: data) {
            self.state = Self.migrated(decoded)
        } else {
            self.state = StepWalletState()
        }
    }

    public func current() -> StepWalletState { state }

    // MARK: - 적립

    /// 수집 경로가 맞을 때만 적립한다.
    ///
    /// **source gate.** Android 가 `creditStep(expectedSource)` 로 넣은 장치다.
    /// 활성 경로는 언제나 정확히 하나인데, 이전 버전이 등록해 둔 관측자가 아직 살아 있거나
    /// 권한이 사라져 경로가 `none` 으로 내려간 뒤에도 알림이 도착할 수 있다.
    /// 그걸 그대로 적립하면 같은 걸음이 두 번 들어간다.
    @discardableResult
    public func credit(
        expectedSource: StepSource,
        sampleCounts: [Int],
        newAnchor: Data?
    ) -> StepWalletState {
        guard state.source == expectedSource else { return state }

        let accumulation = StepAccumulator.applyHealthSamples(
            currentAnchor: state.anchor,
            sampleCounts: sampleCounts,
            newAnchor: newAnchor
        )

        var next = state
        next.balance = min(max(state.balance + accumulation.credited, 0), Int(Int32.max))
        next.anchor = accumulation.anchor
        return save(next)
    }

    // MARK: - 차감 / 복구

    /// 환전 차감. 잔액이 모자라면 던지고 **아무것도 바꾸지 않는다.**
    ///
    /// Android 는 `store.edit` 안에서 던져 편집을 롤백했다. 여기서는 저장 전에 검사한다.
    @discardableResult
    public func consume(_ amount: Int) throws -> StepWalletState {
        guard amount > 0, state.balance >= amount else {
            throw DeviceError.notEnoughWalkingCount(requested: amount, balance: state.balance)
        }
        var next = state
        next.balance -= amount
        return save(next)
    }

    /// 환전 실패분 복구.
    ///
    /// **적립 경로(`credit`)를 타지 않는다.** 복구는 이미 차감했던 걸음을 되돌리는 것이지
    /// 센서가 감지한 걸음이 아니라서 source gate 를 통과할 수 없다.
    /// 중복 판정과 잔액 반영은 같은 호출 안에서 끝난다 — 나누면 MQTT 재전달 두 개가
    /// 동시에 통과한다.
    @discardableResult
    public func restore(walkingCount: Int, eventId: String) -> StepWalletState {
        let result = StepRestore.apply(
            appliedEventIds: state.appliedRestoreEventIds,
            restoreWalkingCount: walkingCount,
            eventId: eventId
        )
        guard result.restoredWalkingCount > 0 else { return state }

        var next = state
        next.balance = min(state.balance + result.restoredWalkingCount, Int(Int32.max))
        next.appliedRestoreEventIds = result.appliedEventIds
        return save(next)
    }

    // MARK: - 수집 경로

    /// 수집 경로를 바꾼다.
    ///
    /// 경로가 바뀌면 **앵커를 버린다.** 앵커는 경로마다 의미가 다르고,
    /// 남은 값으로 첫 조회를 하면 엉뚱한 델타가 나온다.
    /// Android 도 `setStepSource` 에서 커서를 비운다.
    @discardableResult
    public func setSource(_ source: StepSource) -> StepWalletState {
        guard state.source != source else { return state }

        var next = state
        next.source = source
        next.anchor = nil
        return save(next)
    }

    // MARK: - 내부

    @discardableResult
    private func save(_ next: StepWalletState) -> StepWalletState {
        state = next
        if let data = try? JSONEncoder().encode(next) {
            store.set(data, forKey: Self.key)
        }
        return next
    }

    /// 스키마 마이그레이션
    ///
    /// Android `migrateStepSchema()` 대응. 구조가 바뀌면 잔액을 살릴 수 없는 경우가 있는데,
    /// 그때는 0 으로 초기화하는 것을 받아들인다 — 서버에 사본이 없어 복원할 방법이 없다.
    private static func migrated(_ state: StepWalletState) -> StepWalletState {
        guard state.schemaVersion < StepWalletState.currentSchemaVersion else { return state }
        return StepWalletState()
    }
}
