import Foundation
import Security

/// 안전 저장소
///
/// Android 는 DataStore 에 넣기 전 `SessionCipher` 로 AES-GCM 직접 암호화하고,
/// Keystore `Cipher.init` 이 TEE 왕복이라 느려서 복호화 캐시까지 뒀다.
/// **iOS 에서는 그게 전부 필요 없다** — Keychain 이 저장과 암호화를 함께 한다.
///
/// 프로토콜로 두는 이유는 테스트 때문이다. 실제 Keychain 은 호스트에서 도는
/// `swift test` 에서 접근 권한 문제를 일으키므로 인메모리 구현으로 갈아끼운다.
public protocol SecureStore: Sendable {
    func data(forKey key: String) throws -> Data?
    func set(_ data: Data, forKey key: String) throws
    func remove(forKey key: String) throws
}

/// Keychain 구현
public struct KeychainStore: SecureStore {

    public enum StoreError: Error, Equatable {
        case unexpectedStatus(OSStatus)
    }

    private let service: String

    public init(service: String = Bundle.main.bundleIdentifier ?? "com.monglife.mongs.wear.ios") {
        self.service = service
    }

    private func query(_ key: String) -> [String: Any] {
        [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: key,
        ]
    }

    public func data(forKey key: String) throws -> Data? {
        var query = query(key)
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne

        var item: CFTypeRef?
        let status = SecItemCopyMatching(query as CFDictionary, &item)

        switch status {
        case errSecSuccess: return item as? Data
        case errSecItemNotFound: return nil
        default: throw StoreError.unexpectedStatus(status)
        }
    }

    public func set(_ data: Data, forKey key: String) throws {
        // 앱이 잠긴 동안에도 읽어야 한다 — 백그라운드 걸음 수집이 세션을 필요로 한다.
        // Android 가 `setUserAuthenticationRequired(false)` 를 준 것과 같은 이유다.
        let attributes: [String: Any] = [
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock,
        ]

        let updateStatus = SecItemUpdate(query(key) as CFDictionary, attributes as CFDictionary)
        if updateStatus == errSecSuccess { return }
        guard updateStatus == errSecItemNotFound else {
            throw StoreError.unexpectedStatus(updateStatus)
        }

        var insert = query(key)
        insert.merge(attributes) { _, new in new }
        let addStatus = SecItemAdd(insert as CFDictionary, nil)
        guard addStatus == errSecSuccess else {
            throw StoreError.unexpectedStatus(addStatus)
        }
    }

    public func remove(forKey key: String) throws {
        let status = SecItemDelete(query(key) as CFDictionary)
        guard status == errSecSuccess || status == errSecItemNotFound else {
            throw StoreError.unexpectedStatus(status)
        }
    }
}

/// 테스트용 인메모리 구현
public final class InMemorySecureStore: SecureStore, @unchecked Sendable {

    private let lock = NSLock()
    private var storage: [String: Data] = [:]

    public init() {}

    public func data(forKey key: String) throws -> Data? {
        lock.withLock { storage[key] }
    }

    public func set(_ data: Data, forKey key: String) throws {
        lock.withLock { storage[key] = data }
    }

    public func remove(forKey key: String) throws {
        _ = lock.withLock { storage.removeValue(forKey: key) }
    }
}
