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

    init() {
        let configResult = Result { try AppConfig.load() }
        self.configResult = configResult

        // Keychain 은 세션과 기기 식별자가 함께 쓴다.
        let secureStore = KeychainStore()
        // installMarker 로 재설치를 감지해 세션을 버린다.
        // Keychain 은 앱 삭제 후에도 남지만, 세션까지 살아남으면 Android 와 동작이 달라진다.
        let tokenStore = TokenStore(store: secureStore, installMarker: UserDefaultsStore())

        if let config = try? configResult.get() {
            let api = APIClient(config: config, tokenStore: tokenStore)
            self.authService = AuthService(
                api: api,
                tokenStore: tokenStore,
                signInClient: AppleSignInClient(),
                identity: Self.clientIdentity(secureStore: secureStore)
            )
            self.mongService = MongService(api: api, cache: MongCache())
        } else {
            // 설정을 못 읽으면 네트워크 계층을 만들 수 없다. 앱은 오류 화면만 띄운다.
            self.authService = nil
            self.mongService = nil
        }

        // 시뮬레이터에는 걸음 데이터가 없어 항상 0 이 나온다. 건강 앱에서 직접 넣거나
        // 실기기로 확인해야 한다. `SimulatedStepService()` 로 바꾸면 UI 만 빠르게 볼 수 있다.
        self.stepService = HealthKitStepService(
            wallet: StepWalletStore(),
            reader: HealthKitStepReader()
        )
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
