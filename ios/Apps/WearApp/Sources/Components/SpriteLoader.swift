import MongsService
import Observation
import SwiftUI

/// 뷰가 동기적으로 스프라이트를 꺼내 쓸 수 있게 하는 어댑터
///
/// `SpriteStore` 는 actor 라 `await` 가 필요한데 SwiftUI `body` 는 동기다.
/// 그래서 디코드 결과를 여기에 모아 두고 `body` 는 읽기만 한다.
///
/// **없는 스프라이트를 요청하면 스스로 준비한다.** 처음에는 `preload` 목록에 적은 것만
/// 돌려줬는데, 목록에서 빠진 스프라이트는 오류 없이 **그 부분만 비어서** 그려졌다
/// (버튼 배경을 빠뜨려 글자만 뜨는 식). 화면을 고칠 때마다 목록을 함께 챙겨야 하는 건
/// 잊기 쉬운 규칙이라, 요청 자체가 로딩을 시작하도록 바꿨다.
/// `preload` 는 이제 **첫 프레임 깜빡임을 줄이는 최적화**이지 필수가 아니다.
///
/// Android `LocalImageLoader.kt` 가 Coil `ImageLoader` 를 공유하던 자리다.
/// 그 파일 주석대로 로더를 새로 만들면 GIF 가 0번 프레임부터 다시 시작한다.
@Observable
@MainActor
final class SpriteLoader {

    private var ready: [String: AnimatedSpriteSource] = [:]
    /// 지금 디코드 중인 것. 같은 스프라이트를 여러 뷰가 동시에 요청해도 한 번만 읽는다.
    private var loading: Set<String> = []
    private let store = SpriteStore()

    /// 준비된 스프라이트를 돌려준다. 없으면 준비를 시작하고 `nil` 을 준다 —
    /// 디코드가 끝나면 관찰을 통해 뷰가 다시 그려진다.
    func sprite(named name: String) -> AnimatedSpriteSource? {
        if let sprite = ready[name] { return sprite }

        // body 안에서 상태를 바로 바꾸면 "뷰 갱신 중 수정" 이 되므로 다음 차례로 미룬다.
        Task { await load(name) }
        return nil
    }

    /// 화면에 올리기 전에 미리 디코드해 둔다. 첫 프레임에서 비어 보이는 걸 막는다.
    func preload(_ names: [String]) async {
        for name in names {
            await load(name)
        }
    }

    private func load(_ name: String) async {
        guard ready[name] == nil, !loading.contains(name) else { return }

        loading.insert(name)
        defer { loading.remove(name) }

        if let sprite = await store.sprite(named: name) {
            ready[name] = sprite
        }
    }
}
