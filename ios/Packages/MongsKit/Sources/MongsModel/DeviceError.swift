/// Android `application/device-application/.../error/DeviceErrorCode.kt` +
/// `exception/DeviceException.kt` 에 대응.
///
/// Android 는 `ErrorCode.isMessageShow()` 로 토스트 노출 여부를 가른다.
/// 같은 개념을 `isMessageShown` 으로 옮겼다.
public enum DeviceError: Error, Equatable, Sendable {

    /// 환전하려는 걸음 수가 확정 잔액을 넘음
    case notEnoughWalkingCount(requested: Int, balance: Int)

    /// 걸음 수집 경로를 확보하지 못함 (권한 없음 / HealthKit 미가용)
    case stepCollectionUnavailable

    public var code: String {
        switch self {
        case .notEnoughWalkingCount: "DEVICE-STEP-001"
        case .stepCollectionUnavailable: "DEVICE-STEP-002"
        }
    }

    public var message: String {
        switch self {
        case let .notEnoughWalkingCount(requested, balance):
            "환전할 걸음 수가 부족합니다. (요청 \(requested) / 잔액 \(balance))"
        case .stepCollectionUnavailable:
            "걸음 수를 수집할 수 없습니다."
        }
    }

    /// 사용자에게 메시지를 띄울 오류인지
    public var isMessageShown: Bool { true }
}
