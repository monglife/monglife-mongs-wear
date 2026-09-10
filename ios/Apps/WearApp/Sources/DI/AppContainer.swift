import Foundation
import MongsModel
import MongsService
import MongsViewModel
import Observation
import WatchKit

/// 수동 DI 컨테이너
///
/// Android `app/wear-app/.../module/*.kt` (Hilt `@Module`) 자리를 대신한다.
/// Hilt 모듈이 하던 일 — "구현체를 한곳에서 고르고 ViewModel 을 조립한다" — 는
/// 여기서도 똑같이 된다.
@Observable
@MainActor
final class AppContainer {

    /// 설정 로딩 결과. 실패해도 앱을 죽이지 않고 화면에 원인을 띄운다 —
    /// 잘못된 서버를 바라보고 도는 것보다 낫고, 원인도 바로 보인다.
    let configResult: Result<AppConfig, any Error>

    private let stepService: any StepService
    private let authService: AuthService?
    private let mongService: MongService?
    private let playerService: PlayerService?
    /// MQTT 실시간 갱신. 자격증명이나 설정이 없으면 nil 이고, 그래도 앱은 그대로 돈다.
    private let realtimeService: RealtimeService?
    private let optionStore = DeviceOptionStore(store: UserDefaultsStore())
    /// APNs. 설정을 못 읽으면 인증 서비스가 없어 nil 이다.
    private(set) var pushService: PushService?
    private let storeService: StoreService?
    private let communityService: CommunityService?
    private let collectionService: CollectionService?
    private let locationClient = LocationClient()

    init() {
        let configResult = Result { try AppConfig.load() }
        self.configResult = configResult

        // Keychain 은 세션과 기기 식별자가 함께 쓴다.
        let secureStore = KeychainStore()
        // installMarker 로 재설치를 감지해 세션을 버린다.
        // Keychain 은 앱 삭제 후에도 남지만, 세션까지 살아남으면 Android 와 동작이 달라진다.
        let tokenStore = TokenStore(store: secureStore, installMarker: UserDefaultsStore())

        // 시뮬레이터에는 걸음 데이터가 없어 항상 0 이 나온다. 건강 앱에서 직접 넣거나
        // 실기기로 확인해야 한다. `SimulatedStepService()` 로 바꾸면 UI 만 빠르게 볼 수 있다.
        let stepService: any StepService = HealthKitStepService(
            wallet: StepWalletStore(),
            reader: HealthKitStepReader()
        )
        self.stepService = stepService

        if let config = try? configResult.get() {
            let api = APIClient(config: config, tokenStore: tokenStore)
            let authService = AuthService(
                api: api,
                tokenStore: tokenStore,
                signInClient: AppleSignInClient(),
                identity: Self.clientIdentity(secureStore: secureStore)
            )
            self.authService = authService
            let mongService = MongService(api: api, cache: MongCache())
            let playerService = PlayerService(api: api)
            self.mongService = mongService
            self.playerService = playerService
            self.pushService = PushService(authService: authService, optionStore: optionStore)
            self.collectionService = CollectionService(api: api, location: locationClient)
            self.communityService = CommunityService(
                api: api,
                identity: Self.clientIdentity(secureStore: secureStore)
            )
            self.storeService = StoreService(
                api: api,
                purchases: PurchaseClient(),
                playerService: playerService
            )
            self.realtimeService = RealtimeService(
                broker: MQTTBroker(config: config),
                mongService: mongService,
                playerService: playerService,
                stepService: stepService,
                tokenStore: tokenStore,
                deviceIdentifier: DeviceIdentifierStore(store: secureStore)
            )
        } else {
            // 설정을 못 읽으면 네트워크 계층을 만들 수 없다. 앱은 오류 화면만 띄운다.
            self.authService = nil
            self.mongService = nil
            self.playerService = nil
            self.pushService = nil
            self.storeService = nil
            self.communityService = nil
            self.collectionService = nil
            self.realtimeService = nil
        }
    }

    // MARK: - 실시간 갱신

    /// 로그인 뒤 메인이 뜰 때 시작한다.
    func startRealtime() async {
        await realtimeService?.start()
    }

    // MARK: - 푸시

    /// 로그인 뒤 메인이 뜰 때 알림 권한을 묻고 APNs 에 등록한다.
    ///
    /// **로그인 화면에서 묻지 않는다.** 아직 계정이 없어 보낼 알림도 없고,
    /// iOS 는 한 번 거부하면 앱이 다시 물을 수 없어서 물어보는 시점이 중요하다.
    func startPush() async {
        guard let pushService else { return }
        guard await pushService.requestAuthorization() else { return }
        // 등록은 메인 스레드에서 부른다. 토큰은 AppDelegate 콜백으로 돌아온다.
        await MainActor.run { WKApplication.shared().registerForRemoteNotifications() }
        // 토큰이 이미 와 있으면(재실행) 여기서 바로 올라간다.
        await pushService.syncIfPossible()
    }

    /// 현재 몽이 바뀔 때마다 구독을 갈아끼운다.
    func observeRealtimeMong(_ mongId: Int64?) async {
        await realtimeService?.observeMong(mongId)
    }

    /// 로그아웃 시 구독과 연결을 정리한다.
    func stopRealtime() async {
        await realtimeService?.stop()
    }

    var config: AppConfig? { try? configResult.get() }

    func makeRootViewModel() -> RootViewModel? {
        authService.map { RootViewModel(authService: $0) }
    }

    func makeMainStepViewModel() -> MainStepViewModel {
        MainStepViewModel(stepService: stepService)
    }

    func makeMainSlotViewModel() -> MainSlotViewModel? {
        mongService.map { MainSlotViewModel(mongService: $0) }
    }

    func makeExchangeViewModel(kind: ExchangeViewModel.Kind) -> ExchangeViewModel? {
        guard let mongService, let playerService else { return nil }
        return ExchangeViewModel(
            kind: kind,
            stepService: stepService,
            playerService: playerService,
            mongService: mongService
        )
    }

    func makeSettingViewModel(signOut: @escaping @Sendable () async -> Void) -> SettingViewModel {
        SettingViewModel(
            optionStore: optionStore,
            notification: NotificationPermission(),
            stepService: stepService,
            location: locationClient,
            signOut: signOut,
            notificationOptionChanged: { [pushService] in
                await pushService?.notificationOptionChanged()
            }
        )
    }

    func makeCollectionViewModel(kind: CollectionItem.Kind) -> CollectionViewModel? {
        collectionService.map { CollectionViewModel(kind: kind, service: $0) }
    }

    func makeMapSearchViewModel() -> MapSearchViewModel? {
        collectionService.map { MapSearchViewModel(service: $0, location: locationClient) }
    }

    func makeNoticeViewModel() -> NoticeViewModel? {
        communityService.map { NoticeViewModel(service: $0) }
    }

    func makeFeedbackViewModel() -> FeedbackViewModel? {
        communityService.map { FeedbackViewModel(service: $0) }
    }

    func makeRandomDrawViewModel() -> RandomDrawViewModel? {
        mongService.map { RandomDrawViewModel(mongService: $0) }
    }

    func makeChargeViewModel() -> ChargeViewModel? {
        guard let storeService, let playerService else { return nil }
        return ChargeViewModel(storeService: storeService, playerService: playerService)
    }

    func makeFeedViewModel(kind: FeedItem.Kind) -> FeedViewModel? {
        guard let mongService else { return nil }
        return FeedViewModel(kind: kind, mongService: mongService)
    }

    func makeInventoryViewModel() -> InventoryViewModel? {
        guard let mongService else { return nil }
        return InventoryViewModel(mongService: mongService)
    }

    func makeSlotPickViewModel() -> SlotPickViewModel? {
        guard let mongService, let playerService else { return nil }
        return SlotPickViewModel(mongService: mongService, playerService: playerService)
    }

    // MARK: - 기기 정보

    /// Android 는 `Settings.Secure.ANDROID_ID` / `Build.MODEL` / `versionName` 을 쓴다.
    ///
    /// `deviceId` 만은 `identifierForVendor` 를 쓰지 않는다 — 앱을 지우면 값이 바뀌는데,
    /// 이 값은 로그인 키이자 MQTT 토픽(`{prefix}/device/{deviceId}/step/restore`)에 들어간다.
    /// Keychain 에 UUID 를 넣어 재설치에도 살아남게 한다.
    private static func clientIdentity(secureStore: some SecureStore) -> ClientIdentity {
        let bundle = Bundle.main
        return ClientIdentity(
            deviceId: DeviceIdentifierStore.blockingIdentifier(store: secureStore),
            appPackageName: bundle.bundleIdentifier ?? "com.mongs.wear",
            deviceName: WKInterfaceDevice.current().model,
            // 서버 앱 버전 검증에 쓰는 값. 보통 앱의 실제 버전이지만,
            // 개발 서버에 그 버전 행이 없을 때 Configurations/*.xcconfig 에서 덮어쓴다.
            buildVersion: bundle.object(forInfoDictionaryKey: "MongsBuildVersion") as? String
                ?? bundle.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String
                ?? "0"
        )
    }
}
