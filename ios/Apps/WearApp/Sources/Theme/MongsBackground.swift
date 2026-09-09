import SwiftUI

/// 기본 배경
///
/// Android `component/common/background/DefaultBackground.kt` 이식.
/// `LayoutView` 가 화면 전체 뒤에 한 장 깔아 두는 것 — 로그인·로딩·업데이트 안내가 이걸 쓴다.
/// 원본은 정적 PNG `map_mp000` 을 `ContentScale.Crop` 으로 그린다.
struct DefaultBackground: View {

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        ZStack {
            // 스프라이트가 아직 준비되지 않았을 때 흰 화면이 번쩍이지 않게 한다.
            Color.black
            AnimatedSprite(sprite: loader.sprite(named: "map_mp000"), contentMode: .fill)
        }
        .clipped()
        .ignoresSafeArea()
    }
}

/// 메인 배경
///
/// Android `component/common/background/MainBackground.kt` 이식.
///
/// 사용자가 고른 맵을 그리고 그 위에 **검은 베일**을 덮는다. 베일의 진하기는 쪽마다 다르다 —
/// 슬롯 쪽만 0.0(원본 그대로)이고 나머지는 0.4 로 어둡게 해서 펫으로 시선을 모은다.
///
/// 기본 맵(MP000)만 애니메이션 GIF 를 쓰고 나머지는 정적 PNG 다.
///
/// ⚠️ **원본과 한 가지 다르게 간다.** 원본은 기본 맵에만 `ContentScale` 을 주지 않아
/// `Fit` 으로 그리는데, Wear 화면은 원형 1:1 이라 정사각형 맵이 그대로 꽉 찬다.
/// watchOS 화면은 세로로 길어서(예: 416×496) 같은 설정이면 **위아래에 검은 띠가 남는다.**
/// 그래서 여기서는 두 경우 모두 `fill`(= Crop) 로 채운다 — 좌우가 조금 잘리는 대신 여백이 없다.
struct MainBackground: View {

    /// 서버가 주는 맵 코드. `nil` 이면 기본 배경으로 떨어진다.
    var mapCode: String? = "MP000"
    /// 0 = 원본 그대로, 1 = 완전히 검게. 원본의 `pagerBrightnesses` 대응.
    var dim: Double = 0

    @Environment(SpriteLoader.self) private var loader

    /// 기본 맵은 애니메이션이 있다.
    private var isDefaultMap: Bool { mapCode == "MP000" }

    private var spriteName: String? {
        guard let mapCode else { return nil }
        return isDefaultMap ? "map_mp000_gif" : "map_\(mapCode.lowercased())"
    }

    var body: some View {
        ZStack {
            Color.black

            if let spriteName {
                AnimatedSprite(sprite: loader.sprite(named: spriteName), contentMode: .fill)
                Color.black.opacity(dim)
            } else {
                DefaultBackground()
            }
        }
        .clipped()
        .ignoresSafeArea()
    }
}
