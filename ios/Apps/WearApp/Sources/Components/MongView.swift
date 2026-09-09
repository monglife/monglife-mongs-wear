import MongsModel
import MongsService
import SwiftUI

/// 펫 렌더러
///
/// Android `component/common/charactor/Mong.kt` 이식.
/// 애니메이션 두 장을 겹쳐 그린다 — 몸통 + (표정을 가진 캐릭터면) 표정 레이어.
/// 표정은 캐릭터마다 다른 오프셋으로 얹힌다.
struct MongView: View {

    let code: MongResourceCode
    var expression: MongExpression = .normal
    /// Android 의 `(120 * ratio).dp` 에 해당. 표정은 그 비율대로 따라간다.
    var bodySize: CGFloat = 120

    @Environment(SpriteLoader.self) private var loader

    /// Android 원본의 몸통 120 : 표정 35 비율
    private var expressionSize: CGFloat { bodySize * (35.0 / 120.0) }

    var body: some View {
        ZStack {
            AnimatedSprite(sprite: loader.sprite(named: code.animationName))
                .frame(width: bodySize, height: bodySize)

            if code.hasExpression {
                AnimatedSprite(sprite: loader.sprite(named: expression.spriteName))
                    .frame(width: expressionSize, height: expressionSize)
                    .offset(
                        x: code.expressionOffset.x * bodySize / 120,
                        y: code.expressionOffset.y * bodySize / 120
                    )
            }
        }
        .frame(width: bodySize, height: bodySize)
    }
}
