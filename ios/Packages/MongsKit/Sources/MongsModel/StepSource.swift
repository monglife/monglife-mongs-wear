/// 걸음 수 수집 경로
///
/// Android `domain/device-domain/.../model/StepSource.kt` 이식.
/// Wear OS 의 Health Services / TYPE_STEP_COUNTER 자리를 watchOS 의 HealthKit 이 대신한다.
///
/// 동시에 두 경로가 적립하면 그대로 이중 카운트가 되므로, 언제나 정확히 하나만 활성이다.
public enum StepSource: String, Sendable, CaseIterable, Codable {

    /// 아직 어떤 경로를 쓸지 결정하지 않음
    case unresolved

    /// HealthKit `HKQuantityTypeIdentifier.stepCount` 앵커드 쿼리 (구간 delta)
    /// — Android 의 `HEALTH_STEPS` 대응
    case healthKitAnchored

    /// HealthKit 통계 쿼리로 읽는 자정부터의 일일 누계
    /// — Android 의 `HEALTH_DAILY_STEPS` 대응
    case healthKitDaily

    /// 데모/테스트용 인메모리 시뮬레이션. 실기기 수집 경로가 아니다.
    case simulated

    /// 수집 불가 (권한 없음 / HealthKit 미가용)
    case none

    public var isCollecting: Bool {
        self != .unresolved && self != .none
    }

    public var isHealthKit: Bool {
        self == .healthKitAnchored || self == .healthKitDaily
    }
}
