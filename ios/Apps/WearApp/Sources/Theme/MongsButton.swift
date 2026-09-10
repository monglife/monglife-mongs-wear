import SwiftUI

/// 이미지 기반 버튼
///
/// Android `component/common/button/{YellowButton,BlueButton}.kt` 이식.
/// 배경 PNG 를 깔고 그 위에 픽셀 폰트 텍스트를 얹는다 — 시스템 버튼을 쓰지 않는다.
/// 비활성은 투명도가 아니라 **다른 이미지**(`btn_bg_disable`)로 갈아끼운다.
struct MongsButton: View {

    enum Style {
        case yellow
        case blue

        /// `bnt_bg_blue` 는 Android 쪽 오타가 그대로 파일명이 된 것이다.
        var spriteName: String {
            switch self {
            case .yellow: "btn_bg_yellow"
            case .blue: "bnt_bg_blue"
            }
        }

        /// 글자색도 다르다 — `YellowButton` 은 MongsDarkBrown, `BlueButton` 은 MongsNavy.
        var titleColor: Color {
            switch self {
            case .yellow: MongsColor.darkBrown
            case .blue: MongsColor.navy
            }
        }
    }

    let title: String
    var style: Style = .yellow
    var width: CGFloat = 63
    var height: CGFloat = 30
    var fontSize: CGFloat = 12
    var isEnabled: Bool = true
    let action: () -> Void

    @Environment(SpriteLoader.self) private var loader

    // 파라미터는 기준 화면(46mm) 기준 dp 다. 화면 배율은 여기서 한 번만 곱한다.
    private var w: CGFloat { width.ms }
    private var h: CGFloat { height.ms }

    var body: some View {
        Button(action: { if isEnabled { action() } }) {
            ZStack {
                AnimatedSprite(sprite: loader.sprite(named: isEnabled ? style.spriteName : "btn_bg_disable"))
                    .frame(width: w, height: h)
                Text(title)
                    .mongsFont(fontSize)
                    .foregroundStyle(style.titleColor)
                    .lineLimit(1)
                    .padding(.horizontal, 10.ms)
            }
            .frame(width: w, height: h)
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled)
    }
}

/// 원형 아이콘 버튼
///
/// Android `component/common/button/CircleImageButton.kt` 이식.
/// z 순서가 정해져 있다: 반투명 원 배경(0.55) → 아이콘 → (잠김이면) 자물쇠 → 테두리.
/// 비활성이면 아이콘 투명도를 0.4 로 낮추고 자물쇠를 얹는다.
struct MongsCircleButton: View {

    let iconName: String
    /// 테두리 스프라이트. 버튼마다 색이 다르다 (`btn_border_orange` 등).
    let borderName: String
    var size: CGFloat = 54
    /// 원본은 기본값이 size/2 이고, 아이콘마다 30~34 로 따로 준다.
    var iconSize: CGFloat?
    var isEnabled: Bool = true
    let action: () -> Void

    @Environment(SpriteLoader.self) private var loader

    // 파라미터는 기준 화면(46mm) 기준 dp 다. 화면 배율은 여기서 한 번만 곱한다.
    private var s: CGFloat { size.ms }
    private var resolvedIconSize: CGFloat { (iconSize ?? size / 2).ms }

    var body: some View {
        Button(action: { if isEnabled { action() } }) {
            ZStack {
                AnimatedSprite(sprite: loader.sprite(named: "btn_bg_circle"))
                    .frame(width: s, height: s)
                    .opacity(0.55)

                // 원본은 비활성일 때 아이콘의 zIndex 를 -1 로 내려 **배경 뒤에 숨긴다**
                // (`zIndex(if (disable) -1f else 1f)`). 투명도를 낮추는 게 아니라 아예 안 보인다.
                // 대신 자물쇠가 그 자리를 차지한다.
                if isEnabled {
                    AnimatedSprite(sprite: loader.sprite(named: iconName))
                        .frame(width: resolvedIconSize, height: resolvedIconSize)
                } else {
                    AnimatedSprite(sprite: loader.sprite(named: "btn_icon_locker"))
                        .frame(width: s / 2, height: s / 2)
                }

                AnimatedSprite(sprite: loader.sprite(named: borderName))
                    .frame(width: s, height: s)
            }
            .frame(width: s, height: s)
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled)
    }
}

/// 원형 글자 버튼
///
/// Android `component/common/button/CircleTextButton.kt` 이식. 도움말("i") 버튼이 쓴다.
struct MongsCircleTextButton: View {

    let title: String
    let borderName: String
    var size: CGFloat = 54
    let action: () -> Void

    @Environment(SpriteLoader.self) private var loader

    private var s: CGFloat { size.ms }

    var body: some View {
        Button(action: action) {
            ZStack {
                AnimatedSprite(sprite: loader.sprite(named: "btn_bg_circle"))
                    .frame(width: s, height: s)
                    .opacity(0.6)
                Text(title)
                    .mongsFont(23)
                    .foregroundStyle(MongsColor.white)
                AnimatedSprite(sprite: loader.sprite(named: borderName))
                    .frame(width: s, height: s)
            }
            .frame(width: s, height: s)
        }
        .buttonStyle(.plain)
    }
}
