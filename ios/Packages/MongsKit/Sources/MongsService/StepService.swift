import MongsModel

/// 걸음 수 서비스
///
/// Android 의 `DevicePersistencePort` + `Observe/Start/ExchangeWalkingCountUseCase` 를
/// 하나로 합친 것. MVVM 에서는 ViewModel 이 이 프로토콜을 직접 부른다.
///
/// 프로토콜로 두는 이유는 구현을 갈아끼우기 위해서다 —
/// 지금은 `SimulatedStepService`, 다음은 `HealthKitStepService`.
public protocol StepService: Sendable {

    /// 걸음 수 수집 시작. 멱등이다 — 이미 수집 중이면 아무 일도 하지 않는다.
    func startCollection() async

    /// 현재 걸음 수
    func currentStep() async -> Step

    /// 걸음 수 스트림. 구독 즉시 현재 값이 한 번 흘러온다.
    func stepStream() async -> AsyncStream<Step>

    /// 걸음 수 환전
    ///
    /// Android `ExchangeWalkingCountUseCase` 의 **로컬 차감 부분만** 옮겼다.
    /// 원본은 차감 후 `POST user/step/exchange` 를 보내고, 실패하면 서버가
    /// `{prefix}/device/{deviceId}/step/restore` 로 복구 이벤트를 민다.
    /// 그 왕복은 네트워크 레이어를 붙일 때 이 메서드 안에서 채운다.
    @discardableResult
    func exchange(units: Int) async throws -> ExchangeResult

    /// 서버가 민 걸음 복구 이벤트를 반영한다.
    ///
    /// `eventId` 로 중복을 거른다 — MQTT QoS 2 라도 재전달은 일어난다.
    func applyRestore(walkingCount: Int, eventId: String) async
}

/// 환전 결과
public struct ExchangeResult: Sendable, Equatable {
    public let step: Step
    public let payPoint: Int

    public init(step: Step, payPoint: Int) {
        self.step = step
        self.payPoint = payPoint
    }
}
