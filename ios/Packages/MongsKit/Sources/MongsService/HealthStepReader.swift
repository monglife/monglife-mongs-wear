import Foundation
import MongsModel

/// HealthKit 걸음 조회
///
/// Android `data/device-data/.../manager/HealthServicesStepManager.kt` +
/// `StepSensorManager.kt` 자리를 대신한다.
///
/// 프로토콜로 두는 이유는 테스트다. HealthKit 은 시뮬레이터에서도 권한 시트를 띄우고
/// 실제 데이터가 필요해서, 회계 로직을 검증하려면 스텁이 필요하다.
public protocol HealthStepReader: Sendable {

    /// 이 기기에서 HealthKit 걸음을 읽을 수 있는지
    func isAvailable() -> Bool

    /// 읽기 권한 요청. 이미 허용됐으면 아무 일도 하지 않는다.
    func requestAuthorization() async throws

    /// 앵커 이후의 새 걸음 샘플을 읽는다.
    ///
    /// - Parameter anchor: 지난번 조회가 준 앵커. `nil` 이면 HealthKit 이 가진 과거를 전부 준다.
    /// - Returns: 샘플별 걸음 수와 다음에 쓸 앵커
    func readSteps(since anchor: Data?) async throws -> (counts: [Int], anchor: Data?)

    /// 새 걸음 데이터가 생기면 알린다.
    ///
    /// Android 는 `PassiveListenerService`(앱이 죽어도 OS 가 깨움) + 15분 WorkManager 였다.
    /// watchOS 는 `HKObserverQuery` + 백그라운드 전달이 그 역할을 한다.
    func observeUpdates() -> AsyncStream<Void>
}

/// 걸음 수집 오류
public enum HealthStepError: Error, Equatable, Sendable {
    /// 이 기기가 HealthKit 을 지원하지 않는다
    case unavailable
    /// 사용자가 권한을 주지 않았다
    case authorizationDenied
    case query(String)

    public var message: String {
        switch self {
        case .unavailable: "이 기기에서는 걸음 수를 읽을 수 없습니다."
        case .authorizationDenied: "건강 앱에서 걸음 수 읽기를 허용해 주세요."
        case .query: "걸음 수를 읽지 못했습니다."
        }
    }
}
