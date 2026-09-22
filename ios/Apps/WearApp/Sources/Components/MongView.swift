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
    /// 애니메이션 여부. 원본 `isPng` 의 반대다.
    ///
    /// 정적으로 그릴 때는 **표정도 그리지 않는다** — 원본이 `if (!isPng && hasExpression)` 로
    /// 묶어 둔 것과 같다. 슬롯 목록처럼 여러 마리를 작게 늘어놓는 자리에서 쓴다.
    var isAnimated: Bool = true

    @Environment(SpriteLoader.self) private var loader

    /// 화면 배율까지 반영한 실제 몸통 크기.
    /// `bodySize` 는 기준 화면(46mm) 기준 dp 라 호출부는 Android 원본 값을 그대로 적는다.
    private var scaledBody: CGFloat { bodySize.ms }

    /// Android 원본의 몸통 120 : 표정 35 비율
    private var expressionSize: CGFloat { scaledBody * (35.0 / 120.0) }

    /// 몸통 위에 표정을 얹는 위치. 원본 오프셋도 120 기준이라 몸통과 같은 비율로 따라간다.
    private var expressionOffset: CGSize {
        let ratio = scaledBody / 120
        return CGSize(
            width: code.expressionOffset.x * ratio,
            height: code.expressionOffset.y * ratio
        )
    }

    /// 표정 레이어
    ///
    /// ⚠️ `body` 안에 인라인으로 두면 **Release 최적화에서 컴파일러가 죽는다**
    /// (`SimplifyCFG` 패스, Xcode 26.6). 뷰를 따로 빼면 통과한다.
    /// Debug 는 최적화를 안 돌려서 드러나지 않으니 Release 빌드로 확인해야 한다.
    @ViewBuilder
    private var expressionLayer: some View {
        AnimatedSprite(sprite: loader.sprite(named: expression.spriteName))
            .frame(width: expressionSize, height: expressionSize)
            .offset(x: expressionOffset.width, y: expressionOffset.height)
    }

    private var bodyLayer: some View {
        AnimatedSprite(sprite: loader.sprite(named: isAnimated ? code.animationName : code.pngName))
            .frame(width: scaledBody, height: scaledBody)
    }

    var body: some View {
        ZStack {
            bodyLayer

            if isAnimated, code.hasExpression {
                expressionLayer
            }
        }
        .frame(width: scaledBody, height: scaledBody)
    }
}
