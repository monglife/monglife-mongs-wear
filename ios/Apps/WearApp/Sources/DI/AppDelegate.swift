import MongsService
import SwiftUI
import UserNotifications
import WatchKit

/// APNs 등록 콜백을 받는 자리
///
/// SwiftUI 만으로는 `didRegisterForRemoteNotificationsWithDeviceToken` 을 받을 수 없다.
/// watchOS 는 `WKApplicationDelegate` 를 `@WKApplicationDelegateAdaptor` 로 붙인다
/// (iOS 의 `UIApplicationDelegateAdaptor` 대응).
///
/// Android 는 `FirebaseMessagingService.onNewToken` 이 같은 일을 한다.
final class AppDelegate: NSObject, WKApplicationDelegate {

    /// 델리게이트는 SwiftUI 가 만들기 때문에 컨테이너를 주입받을 수 없다.
    /// 앱이 뜨면서 `MongsWearApp` 이 채워 준다.
    var pushService: PushService?

    private let notificationDelegate = PushNotificationDelegate()

    func applicationDidFinishLaunching() {
        UNUserNotificationCenter.current().delegate = notificationDelegate
    }

    func didRegisterForRemoteNotifications(withDeviceToken deviceToken: Data) {
        guard let pushService else { return }
        Task { await pushService.didRegister(deviceToken: deviceToken) }
    }

    func didFailToRegisterForRemoteNotificationsWithError(_ error: any Error) {
        Task { await pushService?.didFailToRegister(error) }
    }
}
