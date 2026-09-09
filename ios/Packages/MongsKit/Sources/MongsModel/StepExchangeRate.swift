/// 걸음 수 환전 환율
///
/// Android `domain/device-domain/.../model/StepExchangeRate.kt` 이식.
///
/// 1000 걸음 = 100 payPoint. 이 값이 ViewModel 과 View 에 흩어져 있으면 화면에 표시한
/// 금액과 실제 요청 금액이 어긋날 수 있어 한곳에 모은다.
public enum StepExchangeRate {

    public static let walkingCountPerUnit = 1_000
    public static let payPointPerUnit = 100

    /// 잔액으로 환전 가능한 최대 단위 수
    public static func maxExchangeableUnits(walkingCount: Int) -> Int {
        max(walkingCount / walkingCountPerUnit, 0)
    }

    public static func payPoint(units: Int) -> Int {
        max(units, 0) * payPointPerUnit
    }

    public static func walkingCount(units: Int) -> Int {
        max(units, 0) * walkingCountPerUnit
    }
}
