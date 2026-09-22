#if canImport(UserNotifications)
import Foundation
import MongsModel
import UserNotifications

/// 푸시 알림
///
/// Android `app/wear-app/.../service/NotificationService.kt` (FCM) 대응.
///
/// ## Android 와 달라지는 지점
///
/// 원본은 **데이터 메시지**를 받아 앱 코드가 직접 알림을 만든다. 그래서 표시 직전에
/// `getNotificationOptionUseCase()` 로 사용자의 알림 옵션을 확인하고, 꺼져 있으면 안 띄운다.
///
/// APNs 의 `alert` 푸시는 **시스템이 먼저 표시**한다. 앱 코드가 끼어들 자리가 없으므로
/// 같은 게이트를 클라이언트에 둘 수 없다. → **옵션 판단이 서버로 올라가야 한다.**
/// (`content-available` 무음 푸시로 흉내낼 수는 있지만 watchOS 에서 전달이 보장되지 않아
/// "알림이 가끔 안 온다"가 된다. 그 길은 택하지 않았다.)
///
/// 그래서 기기 등록에 알림 옵션을 함께 실어 보낸다 — 서버 작업이 필요하다.
/// 자세한 건 `~/Desktop` 의 백엔드 핸드오프 문서 참고.
public actor PushService {

    private let authService: AuthService
    private let optionStore: DeviceOptionStore

    /// 마지막으로 서버에 보낸 토큰. 같은 값을 반복해서 보내지 않는다.
    private var syncedToken: String?
    /// 등록 콜백이 세션보다 먼저 올 수 있어 토큰을 들고 있다가 나중에 보낸다.
    private var pendingToken: String?

    public init(authService: AuthService, optionStore: DeviceOptionStore) {
        self.authService = authService
        self.optionStore = optionStore
    }

    /// 알림 권한 요청. 이미 정해졌으면 시스템이 다이얼로그를 띄우지 않는다.
    @discardableResult
    public func requestAuthorization() async -> Bool {
        await NotificationPermission().request()
    }

    /// APNs 등록 콜백이 준 토큰.
    ///
    /// `Data` 를 16진 문자열로 바꾼다 — 서버가 그 형태를 기대한다.
    public func didRegister(deviceToken: Data) async {
        let token = deviceToken.map { String(format: "%02x", $0) }.joined()
        pendingToken = token
        await syncIfPossible()
    }

    public func didFailToRegister(_ error: any Error) {
        MongsLog.push("APNs 등록 실패 — \(error)")
    }

    /// 로그인 직후 등 세션이 생긴 시점에 부른다.
    /// 토큰이 이미 와 있으면 그때 서버로 올린다.
    public func syncIfPossible() async {
        guard let token = pendingToken, token != syncedToken else { return }
        do {
            try await authService.registerDevice(pushToken: token)
            syncedToken = token
            MongsLog.push("기기 토큰 동기화 완료")
        } catch {
            // 실패해도 앱을 막지 않는다. 다음 실행에서 다시 시도된다.
            MongsLog.push("기기 토큰 동기화 실패 — \(error)")
        }
    }

    /// 알림 옵션이 바뀌면 서버에도 알린다.
    ///
    /// 지금은 기기 등록을 다시 보내는 것으로 대신한다 —
    /// 서버에 옵션 전용 엔드포인트가 생기면 그쪽으로 바꾼다.
    public func notificationOptionChanged() async {
        syncedToken = nil
        await syncIfPossible()
    }
}

/// `UNUserNotificationCenterDelegate` 구현
///
/// Android 는 알림 탭 시 `MainActivity` 를 `CLEAR_TASK` 로 새로 연다.
/// watchOS 는 탭하면 앱이 알아서 앞으로 나오므로 따로 할 일이 없다 —
/// 딥링크가 필요해지면 여기가 그 자리다.
public final class PushNotificationDelegate: NSObject, UNUserNotificationCenterDelegate {

    public override init() { super.init() }

    /// 앱이 떠 있을 때도 배너를 띄운다.
    ///
    /// 기본값은 **표시하지 않음**이다. 원본은 FCM 데이터 메시지를 받아 직접 알림을 만들기
    /// 때문에 앱이 열려 있어도 헤드업으로 뜬다 — 그 동작에 맞춘다.
    public func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        [.banner, .sound]
    }

    public func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse
    ) async {
        MongsLog.push("알림 탭 — \(response.notification.request.identifier)")
    }
}
#endif
