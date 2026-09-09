import Foundation

/// 평범한 로컬 저장소
///
/// Android `DataStore Preferences` 자리. 비밀값이 아닌 것만 담는다
/// (토큰은 `SecureStore` → Keychain 으로 간다).
///
/// `SecureStore` 와 같은 이유로 프로토콜이다 — 테스트가 디스크를 건드리지 않게 한다.
/// `UserDefaults` 를 직접 actor 에 넘기면 Swift 6 에서 Sendable 위반이기도 하다.
public protocol KeyValueStore: Sendable {
    func data(forKey key: String) -> Data?
    func set(_ data: Data, forKey key: String)
}

public struct UserDefaultsStore: KeyValueStore {

    private let suiteName: String?

    public init(suiteName: String? = nil) {
        self.suiteName = suiteName
    }

    private var defaults: UserDefaults {
        suiteName.flatMap(UserDefaults.init(suiteName:)) ?? .standard
    }

    public func data(forKey key: String) -> Data? { defaults.data(forKey: key) }
    public func set(_ data: Data, forKey key: String) { defaults.set(data, forKey: key) }
}

/// 테스트용 인메모리 구현
public final class InMemoryKeyValueStore: KeyValueStore, @unchecked Sendable {

    private let lock = NSLock()
    private var storage: [String: Data] = [:]

    public init() {}

    public func data(forKey key: String) -> Data? { lock.withLock { storage[key] } }
    public func set(_ data: Data, forKey key: String) { lock.withLock { storage[key] = data } }
}
