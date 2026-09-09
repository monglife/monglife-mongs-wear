/// 걸음 수 지갑
///
/// Android `domain/device-domain/.../model/Step.kt` 이식.
///
/// 걸음 수는 기기 로컬에만 존재한다. 서버는 걸음 수를 보관하지 않으므로
/// 앱을 지우거나 기기를 바꾸면 미환전 잔액도 함께 사라진다.
public struct Step: Sendable, Equatable {

    /// 확정 잔액 (적립 누계 - 환전 누계). 환전 가능 여부는 이 값으로만 판단한다.
    public let walkingCount: Int

    /// 센서로는 감지됐지만 아직 지갑에 반영되지 않은 걸음. 표시 전용이다.
    ///
    /// HealthKit 은 걸음을 배치로 내려주므로 걷는 중에는 지갑 반영이 몇 분씩 늦는데,
    /// 그동안 화면 숫자가 멈춰 보이지 않도록 하는 값이다.
    public let pendingWalkingCount: Int

    /// 걸음 수집 경로가 실제로 동작 중인지. false 면 UI 는 숫자 대신 "-" 를 그린다.
    public let available: Bool

    public init(walkingCount: Int, pendingWalkingCount: Int = 0, available: Bool = true) {
        self.walkingCount = walkingCount
        self.pendingWalkingCount = pendingWalkingCount
        self.available = available
    }

    /// 화면에 보여 줄 걸음 수
    public var currentWalkingCount: Int {
        let sum = Int64(walkingCount) + Int64(pendingWalkingCount)
        return Int(min(max(sum, 0), Int64(Int32.max)))
    }

    /// 환전 가능 여부
    ///
    /// `pendingWalkingCount` 는 일부러 제외한다. 아직 지갑에 들어오지 않은 걸음까지
    /// 환전하면 배치가 끝내 도착하지 않았을 때 잔액이 음수로 밀린다.
    public func canConsume(_ amount: Int) -> Bool {
        amount > 0 && amount <= walkingCount
    }

    /// 환전 가능한 단위 수 (1000 걸음 = 1단위)
    ///
    /// 확정 잔액만 본다 — `currentWalkingCount` 를 쓰면 아직 지갑에 안 들어온 걸음까지
    /// 환전 대상이 되어 버린다.
    public var exchangeableUnits: Int {
        StepExchangeRate.maxExchangeableUnits(walkingCount: walkingCount)
    }
}
