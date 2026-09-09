import MongsService
import SwiftUI

/// 애니메이션 스프라이트 뷰
///
/// Android 는 Coil 이 GIF 재생을 맡지만 SwiftUI 에는 GIF 를 그리는 뷰가 없다.
/// `TimelineView(.animation)` 이 디스플레이 리프레시에 맞춰 시각을 주면
/// 그 시각에 해당하는 프레임을 골라 그린다.
///
/// 재생 위치를 상태로 들고 있지 않다는 게 핵심이다. 프레임 인덱스를 `@State` 로 두고
/// 타이머로 올리면 뷰가 매 프레임 다시 그려지면서 SwiftUI 뷰 그래프 전체가 무효화된다.
/// 시각 → 프레임을 매번 계산하면 상태 변경이 아예 없다.
struct AnimatedSprite: View {

    let sprite: AnimatedSpriteSource?
    /// 이 뷰들이 같은 시각을 공유하도록 기준점을 밖에서 받는다.
    /// 펫 여러 마리가 각자 다른 위상으로 흔들리는 걸 막는다.
    var startDate: Date = .distantPast
    /// Compose 의 `ContentScale` 대응.
    /// - `.fit` — 아이콘/스프라이트 (기본)
    /// - `.fill` — 화면을 덮는 배경 (`ContentScale.Crop`)
    /// - `nil` — **비율을 무시하고 프레임에 맞춰 늘린다** (`ContentScale.FillBounds`).
    ///   정사각형 이펙트를 세로로 긴 화면에 씌울 때 `.fill` 로 자르면 터지는 중심이 밀린다.
    var contentMode: ContentMode? = .fit

    var body: some View {
        if let sprite, !sprite.isEmpty {
            TimelineView(.animation) { context in
                let elapsed = context.date.timeIntervalSince(startDate)
                if let frame = sprite.frame(atElapsed: elapsed) {
                    let image = Image(decorative: frame, scale: 1)
                        .interpolation(.none)   // 픽셀아트라 보간하면 뭉개진다
                        .resizable()

                    if let contentMode {
                        image.aspectRatio(contentMode: contentMode)
                    } else {
                        // resizable 만 걸면 프레임에 그대로 늘어난다 = FillBounds
                        image
                    }
                }
            }
        } else {
            // 스프라이트를 못 찾은 경우. 레이아웃이 무너지지 않게 자리만 차지한다.
            Color.clear
        }
    }
}
