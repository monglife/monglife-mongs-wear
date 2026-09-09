import Foundation
import MongsModel

/// HealthKit 기반 걸음 서비스
///
/// Android `data/device-data/.../coordinator/StepCollectionCoordinator.kt` +
/// `collector/StepCollector.kt` + `adapter/DevicePersistenceAdapter.kt` 를 합친 것.
///
/// Android 는 수집 경로가 셋(Health Services delta / 일일누계 / 하드웨어 센서)이라
/// 어느 것을 쓸지 고르는 `resolveSource` 와 15분 주기 재동기화, 부팅 브로드캐스트가 필요했다.
/// watchOS 는 HealthKit 하나뿐이라 그 전부가 사라진다.
public actor HealthKitStepService: StepService {

    private let wallet: StepWalletStore
    private let reader: any HealthStepReader
    /// 환전을 서버에 알린다. 네트워크 계층이 붙기 전에는 `nil` 이다.
    private let exchangeRemotely: (@Sendable (Int) async throws -> Void)?

    private var continuations: [UUID: AsyncStream<Step>.Continuation] = [:]
    private var observeTask: Task<Void, Never>?

    public init(
        wallet: StepWalletStore,
        reader: any HealthStepReader,
        exchangeRemotely: (@Sendable (Int) async throws -> Void)? = nil
    ) {
        self.wallet = wallet
        self.reader = reader
        self.exchangeRemotely = exchangeRemotely
    }

    deinit {
        observeTask?.cancel()
    }

    // MARK: - StepService

    /// 수집을 시작한다. 멱등이다.
    ///
    /// Android `StepCollectionCoordinator.synchronize()` 대응 — 앱 시작, 권한 허용 직후 등
    /// 여러 진입점에서 반복 호출된다.
    public func startCollection() async {
        guard observeTask == nil else { return }

        guard reader.isAvailable() else {
            await setSource(.none)
            return
        }

        do {
            try await reader.requestAuthorization()
        } catch {
            // 요청 자체가 실패한 경우다. 사용자가 "허용 안 함"을 누른 경우는 여기 오지 않는다 —
            // Apple 은 읽기 권한 거부를 앱에 알려주지 않는다 (HealthKitStepReader 주석 참고).
            // 거부되면 조회가 빈 결과를 주므로 잔액이 늘지 않는 것으로만 드러난다.
            await setSource(.none)
            return
        }

        await setSource(.healthKitAnchored)

        // 앱이 꺼져 있는 동안 쌓인 걸음을 먼저 따라잡는다.
        await synchronize()

        let updates = reader.observeUpdates()
        observeTask = Task { [weak self] in
            for await _ in updates {
                await self?.synchronize()
            }
        }
    }

    public func currentStep() async -> Step {
        await wallet.current().step()
    }

    public func stepStream() async -> AsyncStream<Step> {
        let (stream, continuation) = AsyncStream<Step>.makeStream()
        let id = UUID()
        continuations[id] = continuation

        // 구독 즉시 현재 값을 한 번 흘려준다.
        continuation.yield(await wallet.current().step())

        continuation.onTermination = { [weak self] _ in
            Task { await self?.removeContinuation(id) }
        }
        return stream
    }

    @discardableResult
    public func exchange(units: Int) async throws -> ExchangeResult {
        let walkingCount = StepExchangeRate.walkingCount(units: units)

        // 먼저 로컬에서 차감한다. 잔액이 모자라면 여기서 던지고 서버를 부르지 않는다.
        let afterConsume = try await wallet.consume(walkingCount)
        publish(afterConsume.step())

        if let exchangeRemotely {
            do {
                try await exchangeRemotely(walkingCount)
            } catch {
                // 서버가 실패하면 걸음은 서버가 MQTT 로 되돌려 준다
                // (`{prefix}/device/{deviceId}/step/restore`). 여기서 임의로 되돌리면
                // 서버 복구와 겹쳐 이중 적립이 된다.
                throw error
            }
        }

        return ExchangeResult(
            step: afterConsume.step(),
            payPoint: StepExchangeRate.payPoint(units: units)
        )
    }

    // MARK: - 복구

    /// 서버가 MQTT 로 밀어 준 환전 실패분을 반영한다.
    public func applyRestore(walkingCount: Int, eventId: String) async {
        let next = await wallet.restore(walkingCount: walkingCount, eventId: eventId)
        publish(next.step())
    }

    // MARK: - 내부

    /// HealthKit 에서 새 걸음을 읽어 지갑에 반영한다.
    private func synchronize() async {
        let state = await wallet.current()
        guard state.source.isCollecting else { return }

        guard let result = try? await reader.readSteps(since: state.anchor) else { return }

        let next = await wallet.credit(
            expectedSource: .healthKitAnchored,
            sampleCounts: result.counts,
            newAnchor: result.anchor
        )
        publish(next.step())
    }

    private func setSource(_ source: StepSource) async {
        let next = await wallet.setSource(source)
        publish(next.step())
    }

    private func publish(_ step: Step) {
        for continuation in continuations.values {
            continuation.yield(step)
        }
    }

    private func removeContinuation(_ id: UUID) {
        continuations[id] = nil
    }
}
