import Foundation
import MongsModel

/// 세션 보관소
///
/// Android `core/data-core/.../persistence/datastore/SessionDataStore.kt` 대응.
/// 원본의 172 LOC 중 대부분은 AES 암복호화·복호화 캐시·평문 마이그레이션이었고
/// Keychain 이 그걸 대신하므로 여기서는 읽고 쓰고 지우는 것만 남는다.
public actor TokenStore {

    private static let sessionKey = "session"
    /// 설치 표식. Keychain 이 아니라 **UserDefaults 에 둔다** — 아래 설명 참고.
    private static let installMarkerKey = "mongs.install.marker.v1"

    private let store: any SecureStore
    private let installMarker: (any KeyValueStore)?
    private var cached: Session?
    private var loaded = false

    /// - Parameter installMarker: 재설치를 감지할 저장소.
    ///   `nil` 이면 감지를 하지 않는다(테스트에서 매번 지워지는 걸 피하기 위함).
    public init(store: any SecureStore, installMarker: (any KeyValueStore)? = nil) {
        self.store = store
        self.installMarker = installMarker
    }

    public func session() -> Session? {
        if loaded { return cached }

        loaded = true
        clearSessionIfReinstalled()
        guard
            let data = try? store.data(forKey: Self.sessionKey),
            let session = try? JSONDecoder().decode(Session.self, from: data)
        else {
            // 저장된 값이 깨졌으면 세션 없음으로 떨어진다. Android 도 복호화 실패를
            // 예외가 아니라 "세션 없음"으로 처리한다 — 로그인 화면으로 보내면 복구된다.
            cached = nil
            return nil
        }

        cached = session
        return session
    }

    @discardableResult
    public func save(_ session: Session) -> Session {
        cached = session
        loaded = true
        if let data = try? JSONEncoder().encode(session) {
            try? store.set(data, forKey: Self.sessionKey)
        }
        return session
    }

    public func delete() {
        cached = nil
        loaded = true
        try? store.remove(forKey: Self.sessionKey)
    }

    /// 앱을 지웠다 다시 깔았으면 세션을 버린다.
    ///
    /// **iOS 는 앱을 삭제해도 Keychain 항목이 남는다.** 그게 `deviceId` 에는 의도한 동작이지만
    /// (재설치해도 같은 기기로 인식돼야 한다), 세션에는 아니다 —
    /// Android 는 DataStore 가 앱과 함께 지워져 재설치하면 로그아웃 상태로 시작한다.
    /// 그 동작을 맞춘다.
    ///
    /// UserDefaults 는 앱과 함께 지워지므로, 표식이 없으면 "새로 설치됐다"는 뜻이다.
    private func clearSessionIfReinstalled() {
        guard let installMarker else { return }
        guard installMarker.data(forKey: Self.installMarkerKey) == nil else { return }

        try? store.remove(forKey: Self.sessionKey)
        installMarker.set(Data([1]), forKey: Self.installMarkerKey)
    }
}

/// 기기 식별자 보관소
///
/// Android 는 `Settings.Secure.ANDROID_ID` 를 쓴다. iOS 의 `identifierForVendor` 는
/// **앱을 지우면 값이 바뀐다.** `deviceId` 는 로그인 요청의 키이자 MQTT 토픽
/// (`{prefix}/device/{deviceId}/step/restore`) 에 들어가므로 바뀌면 곤란하다.
///
/// 그래서 UUID 를 직접 만들어 Keychain 에 넣는다. Keychain 은 앱 삭제 후에도 남아
/// 재설치 시 같은 값이 돌아온다.
public actor DeviceIdentifierStore {

    private static let key = "device-identifier"

    private let store: any SecureStore
    private var cached: String?

    public init(store: any SecureStore) {
        self.store = store
    }

    public func identifier() -> String {
        if let cached { return cached }

        if let data = try? store.data(forKey: Self.key),
           let existing = String(data: data, encoding: .utf8),
           !existing.isEmpty {
            cached = existing
            return existing
        }

        let created = UUID().uuidString
        try? store.set(Data(created.utf8), forKey: Self.key)
        cached = created
        return created
    }

    /// 동기 진입점
    ///
    /// `ClientIdentity` 는 앱 시작 시 한 번 만들어지는 값 타입이라 `await` 를 쓸 수 없는
    /// 자리(컨테이너 `init`)에서 필요하다. Keychain 접근 한 번이라 비용도 무시할 만하다.
    public static func blockingIdentifier(store: some SecureStore) -> String {
        if let data = try? store.data(forKey: key),
           let existing = String(data: data, encoding: .utf8),
           !existing.isEmpty {
            return existing
        }
        let created = UUID().uuidString
        try? store.set(Data(created.utf8), forKey: key)
        return created
    }
}
