import Foundation

/// 적립 계산 결과
///
/// Android `domain/device-domain/.../model/StepAccumulation.kt` 이식.
public struct StepAccumulation: Sendable, Equatable {
    /// 이번에 지갑에 더할 걸음 수 (항상 0 이상)
    public let credited: Int
    /// 다음 조회에 쓸 커서
    public let anchor: Data?

    public init(credited: Int, anchor: Data?) {
        self.credited = credited
        self.anchor = anchor
    }
}

/// 걸음 수 적립 계산
///
/// Android `domain/device-domain/.../model/StepAccumulator.kt` 이식.
/// 순수 함수라 단위 테스트로 전부 덮을 수 있고, 이중 카운트/유실 버그가 날 수 있는 지점도 여기뿐이다.
///
/// **원본보다 크게 단순해졌다.** Android 는 Health Services 가 "부팅 이후 경과 시간" 기준의
/// 데이터포인트를 주기 때문에 재부팅을 직접 감지해야 했다 —
/// `StepCursor.bootMarkOf` 가 10초 단위로 양자화하고 30초 허용오차로 재부팅을 판정하는 로직,
/// 일일 누계 타입의 자정 창 처리, 센서 역행 감지가 전부 그것 때문이었다.
///
/// HealthKit 의 `HKAnchoredObjectQuery` 는 **앵커가 그 역할을 대신한다.** 앵커 이후의
/// 새 샘플만 돌려주므로 재전달·중복이 애초에 없고, 타임스탬프도 부팅 상대가 아니라 벽시계다.
public enum StepAccumulator {

    /// 한 번에 인정하는 걸음 수 상한.
    ///
    /// 센서 이상값이나 깨진 배치가 지갑을 오염시키는 것을 막는 안전판이다.
    /// Android 와 같은 값을 쓴다 — 사람이 한 배치에 이만큼 걸을 수는 없다.
    public static let maxCreditPerBatch = 100_000

    /// HealthKit 앵커드 쿼리 결과를 지갑에 더할 값으로 바꾼다.
    ///
    /// - Parameters:
    ///   - currentAnchor: 지금까지 저장해 둔 앵커. `nil` 이면 아직 기준선을 잡지 않았다.
    ///   - sampleCounts: 앵커 이후 새로 들어온 샘플들의 걸음 수
    ///   - newAnchor: 이번 조회가 돌려준 새 앵커
    public static func applyHealthSamples(
        currentAnchor: Data?,
        sampleCounts: [Int],
        newAnchor: Data?
    ) -> StepAccumulation {

        // ⚠️ 첫 관측은 기준선만 잡고 적립하지 않는다.
        //
        // 앵커 없이 물으면 HealthKit 은 **가지고 있는 과거 샘플을 전부** 돌려준다.
        // 그대로 적립하면 앱을 깔기 전에 걸은 걸음이 통째로 입금된다.
        // Android 도 같은 이유로 첫 일일 누계·첫 센서 누계를 적립하지 않는다.
        guard currentAnchor != nil else {
            return StepAccumulation(credited: 0, anchor: newAnchor)
        }

        let sum = sampleCounts.reduce(into: 0) { total, count in
            // 음수 샘플은 있을 수 없지만, 있어도 잔액을 깎지는 않는다.
            if count > 0 { total += count }
        }

        return StepAccumulation(
            credited: min(max(sum, 0), maxCreditPerBatch),
            // 조회가 앵커를 주지 않았으면 기존 것을 유지한다. 앵커를 잃으면
            // 다음 조회가 과거 전체를 다시 끌어온다.
            anchor: newAnchor ?? currentAnchor
        )
    }
}

/// 환전 실패분 복구 판정
///
/// Android `domain/device-domain/.../model/StepRestore.kt` 이식.
/// 서버가 보내는 복구 알림은 MQTT 라 같은 알림이 두 번 도착할 수 있다.
public enum StepRestore {

    /// 보관할 복구 이력 개수.
    ///
    /// 이력은 중복 판정에만 쓰이므로 무한정 쌓을 이유가 없다.
    /// 복구는 환전 실패라는 드문 사건에만 발생하므로 이 정도면 재전달 창을 충분히 덮는다.
    public static let maxAppliedEventIds = 50

    public struct Result: Sendable, Equatable {
        /// 0 이면 반영하지 않는다는 뜻이다.
        public let restoredWalkingCount: Int
        public let appliedEventIds: [String]
    }

    /// - Parameter appliedEventIds: 이미 반영한 복구 식별자 (오래된 것부터)
    public static func apply(
        appliedEventIds: [String],
        restoreWalkingCount: Int,
        eventId: String
    ) -> Result {
        let ignored = Result(restoredWalkingCount: 0, appliedEventIds: appliedEventIds)

        // 식별자가 없으면 중복을 가려낼 수 없다. 이중 적립보다는 반영하지 않는 쪽이 안전하다.
        guard !eventId.trimmingCharacters(in: .whitespaces).isEmpty else { return ignored }
        guard restoreWalkingCount > 0 else { return ignored }
        guard !appliedEventIds.contains(eventId) else { return ignored }

        return Result(
            restoredWalkingCount: restoreWalkingCount,
            appliedEventIds: (appliedEventIds + [eventId]).suffix(maxAppliedEventIds).map { $0 }
        )
    }
}
