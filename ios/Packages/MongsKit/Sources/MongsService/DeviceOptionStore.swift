import Foundation
import MongsModel

/// 기기 옵션 보관소
///
/// Android `data/device-data/.../DeviceOptionEntity` + `DevicePersistenceAdapter` 의
/// 옵션 부분 이식. 원본은 Room 테이블 한 줄이지만 여기서는 값이 하나뿐이라
/// `UserDefaults`(= `KeyValueStore`) 로 충분하다.
///
/// 알림 옵션은 **권한과 다른 값이다.** 권한이 있어도 사용자가 앱 안에서 끌 수 있다.
public actor DeviceOptionStore {

    private enum Key {
        static let notification = "mongs.device.notificationOption"
    }

    private let store: any KeyValueStore
    private var continuations: [UUID: AsyncStream<Bool>.Continuation] = [:]

    public init(store: any KeyValueStore) {
        self.store = store
    }

    /// 기본값은 **켜짐** 이다 — Android `DeviceOptionEntity` 의 초기값과 같다.
    public func notificationOption() -> Bool {
        guard let data = store.data(forKey: Key.notification),
              let value = try? JSONDecoder().decode(Bool.self, from: data)
        else { return true }
        return value
    }

    public func setNotificationOption(_ value: Bool) {
        guard let data = try? JSONEncoder().encode(value) else { return }
        store.set(data, forKey: Key.notification)
        for continuation in continuations.values { continuation.yield(value) }
    }

    /// Android `ObserveNotificationOptionUseCase` 대응.
    public func notificationOptionStream() -> AsyncStream<Bool> {
        let (stream, continuation) = AsyncStream<Bool>.makeStream()
        let id = UUID()
        continuations[id] = continuation
        continuation.yield(notificationOption())
        continuation.onTermination = { [weak self] _ in
            Task { await self?.remove(id) }
        }
        return stream
    }

    private func remove(_ id: UUID) {
        continuations[id] = nil
    }
}
