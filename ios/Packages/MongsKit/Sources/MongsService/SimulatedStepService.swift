import Foundation
import MongsModel

/// `StepService` 의 시뮬레이션 구현
///
/// Android `data/device-data/.../adapter/DevicePersistenceAdapter.kt` 자리를 채우는 골격이다.
/// 원본은 Health Services / TYPE_STEP_COUNTER → `StepCollector` → DataStore 지갑으로
/// 이어지지만, 여기서는 **서버도 HealthKit 도 없이** 배선만 검증하기 위해 걸음이 저절로
/// 쌓이는 것처럼 흉내 낸다.
///
/// 다음 단계에서 이 타입 옆에 `HealthKitStepService` 를 만들고 `AppContainer` 에서 갈아끼운다.
/// ViewModel 위쪽은 하나도 바뀌지 않는다.
public actor SimulatedStepService: StepService {

    private var step: Step
    private var source: StepSource = .unresolved
    private var continuations: [UUID: AsyncStream<Step>.Continuation] = [:]
    private var collectionTask: Task<Void, Never>?

    private let stepsPerTick: Int
    private let tickInterval: Duration

    /// - Parameters:
    ///   - initialWalkingCount: 시작 잔액. 데모에서 환전 버튼을 바로 눌러 볼 수 있도록 기본값을 준다.
    ///   - stepsPerTick: 한 틱마다 적립할 걸음 수
    ///   - tickInterval: 틱 간격
    public init(
        initialWalkingCount: Int = 2_450,
        stepsPerTick: Int = 17,
        tickInterval: Duration = .seconds(1)
    ) {
        // available 은 false 로 시작한다. 수집 경로가 정해지기 전에는 0 이 아니라 "-" 를 보여야 한다.
        self.step = Step(walkingCount: initialWalkingCount, pendingWalkingCount: 0, available: false)
        self.stepsPerTick = stepsPerTick
        self.tickInterval = tickInterval
    }

    deinit {
        collectionTask?.cancel()
    }

    // MARK: - StepService

    public func startCollection() async {
        // 멱등. Android 의 StepCollectionCoordinator 와 같은 계약이다 —
        // 앱 시작 / 권한 허용 직후 등 여러 진입점에서 반복 호출된다.
        guard collectionTask == nil else { return }

        source = .simulated
        publish(Step(walkingCount: step.walkingCount, pendingWalkingCount: 0, available: true))

        let interval = tickInterval
        collectionTask = Task { [weak self] in
            while !Task.isCancelled {
                do {
                    try await Task.sleep(for: interval)
                } catch {
                    return
                }
                await self?.tick()
            }
        }
    }

    public func currentStep() async -> Step {
        step
    }

    public func stepStream() async -> AsyncStream<Step> {
        let (stream, continuation) = AsyncStream<Step>.makeStream()
        let id = UUID()
        continuations[id] = continuation

        // StateFlow 처럼 구독 즉시 현재 값을 한 번 흘려준다.
        continuation.yield(step)

        continuation.onTermination = { [weak self] _ in
            Task { await self?.removeContinuation(id) }
        }
        return stream
    }

    @discardableResult
    public func exchange(units: Int) async throws -> ExchangeResult {
        let walkingCount = StepExchangeRate.walkingCount(units: units)

        guard step.canConsume(walkingCount) else {
            throw DeviceError.notEnoughWalkingCount(
                requested: walkingCount,
                balance: step.walkingCount
            )
        }

        let next = Step(
            walkingCount: step.walkingCount - walkingCount,
            pendingWalkingCount: step.pendingWalkingCount,
            available: step.available
        )
        publish(next)

        return ExchangeResult(step: next, payPoint: StepExchangeRate.payPoint(units: units))
    }

    // MARK: - 내부

    /// 현재 수집 경로. 테스트/디버그용.
    public func currentSource() -> StepSource { source }

    public func stopCollection() {
        collectionTask?.cancel()
        collectionTask = nil
        source = .none
        publish(Step(walkingCount: step.walkingCount, pendingWalkingCount: 0, available: false))
    }

    private func tick() {
        publish(Step(
            walkingCount: step.walkingCount + stepsPerTick,
            pendingWalkingCount: step.pendingWalkingCount,
            available: true
        ))
    }

    /// 시뮬레이터에는 되돌릴 서버 왕복이 없다. 잔액만 그대로 되돌려 준다.
    public func applyRestore(walkingCount: Int, eventId: String) async {
        publish(Step(
            walkingCount: step.walkingCount + walkingCount,
            pendingWalkingCount: step.pendingWalkingCount,
            available: step.available
        ))
    }

    private func publish(_ next: Step) {
        step = next
        for continuation in continuations.values {
            continuation.yield(next)
        }
    }

    private func removeContinuation(_ id: UUID) {
        continuations[id] = nil
    }
}
