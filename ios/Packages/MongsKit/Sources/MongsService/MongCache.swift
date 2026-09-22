import Foundation
import MongsModel

/// 현재 몽 캐시
///
/// Android `data/mong-data/.../persistence/` 의 Room DB(`MONGS-MONG-DATABASE`, 테이블 2개) 자리다.
///
/// **SwiftData 를 쓰지 않는다.** 캐시할 대상이 현재 몽 **하나**뿐이라 스키마·마이그레이션·
/// 컨테이너를 들일 이유가 없다. Room 이 필요했던 건 Android 가 여러 슬롯의 몽과 옵션을
/// 관계로 들고 있었기 때문이다.
///
/// 오프라인에서도 마지막 몽을 보여주기 위해 디스크에 남긴다.
public actor MongCache {

    private static let key = "mongs.mong.current.v1"

    private let store: any KeyValueStore
    private var cached: Mong?
    private var loaded = false

    public init(store: any KeyValueStore = UserDefaultsStore()) {
        self.store = store
    }

    public func mong() -> Mong? {
        if loaded { return cached }
        loaded = true

        guard let data = store.data(forKey: Self.key) else { return nil }
        // 모델이 바뀌어 디코딩이 깨지면 캐시가 없는 것으로 본다.
        // 다음 refresh 가 서버에서 다시 채운다.
        cached = try? JSONDecoder().decode(Mong.self, from: data)
        return cached
    }

    public func save(_ mong: Mong?) {
        cached = mong
        loaded = true

        if let mong, let data = try? JSONEncoder().encode(mong) {
            store.set(data, forKey: Self.key)
        } else {
            store.set(Data(), forKey: Self.key)
        }
    }
}
