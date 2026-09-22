import Foundation
import MongsModel
import MongsService
import Observation

/// 설정 ViewModel
///
/// Android `pages/setting/SettingViewModel.kt` 이식.
///
/// 원본은 알림·활동·위치 세 권한의 **부여 여부를 직접 읽어** 스위치에 그렸다.
/// iOS 는 그게 절반만 가능하다:
/// - 알림 — `UNUserNotificationCenter` 가 실제 상태를 준다. 그대로 옮긴다.
/// - 활동(HealthKit) — 읽기 권한은 **언제나 `.notDetermined`** 다. 상태를 알 수 없으니
///   스위치를 그리면 거짓말이 된다. 요청만 다시 보낼 수 있는 줄로 바꿨다.
/// - 위치 — `CLLocationManager.authorizationStatus` 가 실제 상태를 준다. 그대로 옮긴다.
@Observable
@MainActor
public final class SettingViewModel: ErrorReportingViewModel {

    public private(set) var isLoading = true
    public private(set) var notificationOption = true
    public private(set) var notificationPermission = false
    /// 위치 권한. **iOS 도 상태를 알려준다** — HealthKit 읽기와 다르다.
    public private(set) var locationPermission = false
    /// 로그아웃 확인 다이얼로그
    public var isLogoutConfirmPresented = false
    /// 로그아웃이 끝나 루트가 로그인 화면으로 돌아가야 하는지
    public private(set) var didSignOut = false

    private let optionStore: DeviceOptionStore
    private let notification: NotificationPermission
    private let stepService: any StepService
    private let location: LocationClient
    private let signOutAction: @Sendable () async -> Void
    /// 알림 옵션이 바뀌면 서버에도 알려야 한다 — APNs 는 표시 여부를 서버가 정한다
    /// (`PushService` 주석 참고).
    private let notificationOptionChanged: @Sendable () async -> Void

    public init(
        optionStore: DeviceOptionStore,
        notification: NotificationPermission,
        stepService: any StepService,
        location: LocationClient = LocationClient(),
        signOut: @escaping @Sendable () async -> Void,
        notificationOptionChanged: @escaping @Sendable () async -> Void = {}
    ) {
        self.optionStore = optionStore
        self.notification = notification
        self.stepService = stepService
        self.location = location
        self.signOutAction = signOut
        self.notificationOptionChanged = notificationOptionChanged
    }

    public func load() async {
        isLoading = true
        defer { isLoading = false }

        notificationOption = await optionStore.notificationOption()
        notificationPermission = await notification.isGranted()
        locationPermission = location.isAuthorized
    }

    /// 원본은 권한이 없으면 스위치를 잠근다. 권한 없이 켜 봐야 알림이 오지 않기 때문이다.
    public var canToggleNotification: Bool { notificationPermission }

    public func toggleNotificationOption() async {
        guard canToggleNotification else { return }
        let next = !notificationOption
        notificationOption = next
        await optionStore.setNotificationOption(next)
        await notificationOptionChanged()
    }

    /// 알림 권한 요청. 이미 정해진 뒤라면 시스템이 다이얼로그를 띄우지 않으므로
    /// 사용자는 설정 앱에서 직접 바꿔야 한다.
    public func requestNotificationPermission() async {
        notificationPermission = await notification.request()
        if !notificationPermission { notificationOption = false }
    }

    /// 위치 권한 요청.
    ///
    /// 맵 탐색이 쓴다. 아직 안 물었으면 시스템 다이얼로그가 뜨고,
    /// 이미 거부됐으면 설정 앱에서만 바꿀 수 있다 (알림과 같다).
    public func requestLocationPermission() async {
        _ = try? await location.current()
        locationPermission = location.isAuthorized
    }

    /// 활동(걸음) 권한 요청.
    ///
    /// iOS 는 결과를 알려주지 않는다. 요청만 다시 보내고 상태는 그리지 않는다.
    public func requestActivityPermission() async {
        await stepService.startCollection()
    }

    public func askLogout() { isLogoutConfirmPresented = true }
    public func cancelLogout() { isLogoutConfirmPresented = false }

    public func logout() async {
        isLogoutConfirmPresented = false
        isLoading = true
        await signOutAction()
        didSignOut = true
        isLoading = false
    }

    public func recoverFromError(_ error: any Error) async {
        isLogoutConfirmPresented = false
        isLoading = false
    }
}
