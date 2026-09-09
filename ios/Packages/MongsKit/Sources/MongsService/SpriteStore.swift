import Foundation

/// 디코드한 스프라이트 캐시
///
/// Android `assets/LocalImageLoader.kt` 는 Coil `ImageLoader` 를 `CompositionLocal` 로
/// 한 번만 만들어 내려보낸다. 그 파일의 주석이 지적하듯, 로더를 매 컴포지션마다 새로 만들면
/// GIF 디코드가 0번 프레임부터 다시 시작한다. 여기서도 같은 이유로 **디코드 결과를 공유**한다.
///
/// 펫 몸통 33종 + 표정 8종이 전부라 전량 캐시해도 메모리 부담이 없다.
public actor SpriteStore {

    private var cache: [String: AnimatedSpriteSource] = [:]
    private let bundle: Bundle
    private let subdirectory: String?

    public init(bundle: Bundle = .main, subdirectory: String? = "Sprites") {
        self.bundle = bundle
        self.subdirectory = subdirectory
    }

    /// 이름으로 스프라이트를 가져온다. 확장자는 붙이지 않는다.
    ///
    /// GIF 를 먼저 찾고 없으면 PNG 로 떨어진다 — Android 의 `isPng` 폴백과 같은 의도다.
    /// 원본 에셋에 `.gif` / `.GIF` 가 섞여 있어(26개가 대문자) 양쪽 다 시도한다.
    public func sprite(named name: String) -> AnimatedSpriteSource? {
        if let cached = cache[name] { return cached }

        for ext in ["gif", "GIF", "png"] {
            guard
                let url = bundle.url(forResource: name, withExtension: ext, subdirectory: subdirectory),
                let data = try? Data(contentsOf: url),
                let sprite = AnimatedSpriteSource.decode(data: data)
            else { continue }

            cache[name] = sprite
            return sprite
        }
        return nil
    }

    /// 화면에 올리기 전에 미리 디코드해 둔다. 첫 프레임에서 끊기는 걸 막는다.
    public func preload(_ names: [String]) {
        for name in names { _ = sprite(named: name) }
    }

    public func cachedCount() -> Int { cache.count }
}
