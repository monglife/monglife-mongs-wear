#if canImport(UserNotifications)
import Foundation
import UserNotifications

/// 알림 권한
///
/// Android `PermissionUtil.verifyNotificationPermission()` 대응.
///
/// ⚠️ 걸음(활동) 권한과 달리 **알림은 iOS 도 상태를 알려준다.**
/// HealthKit 읽기 권한은 `.notDetermined` 만 돌려주므로 같은 방식이 통하지 않는다
/// (`ios/CLAUDE.md` 의 HealthKit 절 참고).
public struct NotificationPermission: Sendable {

    public init() {}

    public func isGranted() async -> Bool {
        let settings = await UNUserNotificationCenter.current().notificationSettings()
        return switch settings.authorizationStatus {
        case .authorized, .provisional, .ephemeral: true
        default: false
        }
    }

    /// 아직 안 물어봤으면 시스템 다이얼로그를 띄운다. 이미 정해졌으면 아무것도 하지 않는다
    /// (iOS 는 한 번 거부하면 앱이 다시 물을 수 없다 — 설정 앱에서만 바꿀 수 있다).
    @discardableResult
    public func request() async -> Bool {
        let center = UNUserNotificationCenter.current()
        let settings = await center.notificationSettings()
        guard settings.authorizationStatus == .notDetermined else {
            return await isGranted()
        }
        return (try? await center.requestAuthorization(options: [.alert, .sound, .badge])) ?? false
    }
}
#endif
