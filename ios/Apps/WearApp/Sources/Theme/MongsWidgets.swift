import SwiftUI

/// 페이포인트 표시
///
/// Android `component/common/textbox/PayPointBox.kt` 이식.
/// 배경 이미지 위에 코인 아이콘 + 숫자를 2:8 비율로 얹는다.
struct PayPointBox: View {

    let payPoint: Int
    var width: CGFloat = 80
    var height: CGFloat = 30

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        ZStack {
            AnimatedSprite(sprite: loader.sprite(named: "point_bg"))
                .frame(width: width, height: height)

            HStack(spacing: 0) {
                AnimatedSprite(sprite: loader.sprite(named: "point_icon_pay"))
                    .frame(width: 12, height: 12)
                    .frame(width: (width - 20) * 0.2)
                Text("\(payPoint)")
                    .mongsFont(14)
                    .foregroundStyle(MongsColor.navy)
                    .lineLimit(1)
                    .frame(width: (width - 20) * 0.8)
            }
            .padding(.horizontal, 10)
        }
        .frame(width: width, height: height)
    }
}

/// 컨디션 게이지
///
/// Android `component/pages/main/condition/ConditionSection.kt` 이식.
/// 아이콘을 원형 진행바가 감싼다.
struct ConditionGauge: View {

    let iconName: String
    /// 0~100
    let progress: Double
    let color: Color
    var size: CGFloat = 60

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        ZStack {
            Circle()
                .stroke(MongsColor.white.opacity(0.2), lineWidth: 4)
            Circle()
                .trim(from: 0, to: min(max(progress, 0), 100) / 100)
                .stroke(color, style: StrokeStyle(lineWidth: 4, lineCap: .round))
                // 12시 방향에서 시작한다. SwiftUI 의 trim 은 3시에서 시작하므로 돌려준다.
                .rotationEffect(.degrees(-90))

            AnimatedSprite(sprite: loader.sprite(named: iconName))
                .frame(width: 25, height: 25)
        }
        .frame(width: size, height: size)
        .padding(6)
    }
}

/// 페이지 인디케이터
///
/// Android `component/common/pagenation/PageIndicator.kt` 이식.
/// Wear 는 `HorizontalPageIndicator` 를 쓰지만 watchOS 엔 대응물이 없어 점을 직접 그린다.
struct PageIndicator: View {

    let pageCount: Int
    let currentPage: Int
    var indicatorSize: CGFloat = 6
    var spacing: CGFloat = 4

    var body: some View {
        HStack(spacing: spacing) {
            ForEach(0 ..< pageCount, id: \.self) { index in
                Circle()
                    .fill(index == currentPage ? MongsColor.navy : MongsColor.white)
                    .frame(width: indicatorSize, height: indicatorSize)
            }
        }
    }
}

/// 화면을 감싸는 진행 링
///
/// Android `component/common/bar/ProgressIndicator.kt` 이식 —
/// `CircularProgressIndicator(fillMaxSize(), strokeWidth = 4.dp)` 다.
///
/// ⚠️ Wear 는 화면이 원형이라 원이 베젤을 그대로 따라 돈다. watchOS 는 둥근 사각형이라
/// 원을 그리면 위아래에 큰 여백이 남는다. 그래서 **화면 모양을 따라가는 둥근 사각형**으로 그린다.
/// 원본의 "테두리를 따라 도는 게이지" 라는 의도를 살리는 쪽을 택했다.
struct EdgeProgressRing: View {

    /// 0~100
    let progress: Double
    var color: Color = MongsColor.purple
    var lineWidth: CGFloat = 4
    /// 화면 가장자리에서 띄울 거리.
    ///
    /// Wear 는 화면이 평평한 원이라 링을 베젤에 딱 붙여도 다 보인다.
    /// 애플워치는 유리가 가장자리에서 휘어 있어 끝에 붙이면 **실기기에서 잘려 보인다.**
    /// 스트로크 두께의 절반은 경로 바깥으로 나가므로 그만큼 더 들여야 한다.
    var inset: CGFloat = 6

    var body: some View {
        EdgeRingShape()
            .trim(from: 0, to: min(max(progress, 0), 100) / 100)
            .stroke(color, style: StrokeStyle(lineWidth: lineWidth, lineCap: .round))
            .padding(inset + lineWidth / 2)
    }
}

/// 화면 테두리를 따라가는 둥근 사각형 경로. **12시 방향에서 시작해 시계방향**으로 돈다.
///
/// `RoundedRectangle` + `rotationEffect(-90)` 로는 안 된다 —
/// 회전 모디파이어는 도형 자체를 돌려서, 정사각형이 아닌 프레임에서는 가로세로가 뒤바뀐다
/// (208×248 이 248×208 로 그려져 좌우가 잘리고 위아래에 틈이 생긴다).
/// 그래서 시작점부터 직접 그린다.
struct EdgeRingShape: Shape {

    /// 짧은 변 기준 모서리 곡률 비율.
    ///
    /// 애플워치 화면 곡률보다 **크게** 잡는다. 작게 잡으면 링의 모서리가 화면 모서리
    /// 바깥으로 삐져나가 잘린다. 크게 잡으면 안쪽으로 들어와 안전하다.
    var cornerRatio: CGFloat = 0.28

    func path(in rect: CGRect) -> Path {
        let radius = min(rect.width, rect.height) * cornerRatio
        var path = Path()

        path.move(to: CGPoint(x: rect.midX, y: rect.minY))
        path.addArc(tangent1End: CGPoint(x: rect.maxX, y: rect.minY),
                    tangent2End: CGPoint(x: rect.maxX, y: rect.maxY), radius: radius)
        path.addArc(tangent1End: CGPoint(x: rect.maxX, y: rect.maxY),
                    tangent2End: CGPoint(x: rect.minX, y: rect.maxY), radius: radius)
        path.addArc(tangent1End: CGPoint(x: rect.minX, y: rect.maxY),
                    tangent2End: CGPoint(x: rect.minX, y: rect.minY), radius: radius)
        path.addArc(tangent1End: CGPoint(x: rect.minX, y: rect.minY),
                    tangent2End: CGPoint(x: rect.maxX, y: rect.minY), radius: radius)
        path.addLine(to: CGPoint(x: rect.midX, y: rect.minY))

        return path
    }
}

/// 로딩 표시
///
/// Android `component/common/bar/LoadingBar.kt` 이식 — 애니메이션 GIF 스피너다.
struct LoadingBar: View {

    var size: CGFloat = 40

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        AnimatedSprite(sprite: loader.sprite(named: "icon_loading"))
            .frame(width: size, height: size)
    }
}

/// 별가루 표시
///
/// Android `component/common/textbox/StarPointBox.kt` 이식.
/// `PayPointBox` 와 같은 배경에 아이콘과 글자색만 다르다.
struct StarPointBox: View {

    let starPoint: Int
    var width: CGFloat = 80
    var height: CGFloat = 30

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        ZStack {
            AnimatedSprite(sprite: loader.sprite(named: "point_bg"))
                .frame(width: width, height: height)

            HStack(spacing: 0) {
                AnimatedSprite(sprite: loader.sprite(named: "point_icon_star"))
                    .frame(width: 12, height: 12)
                    .frame(width: (width - 20) * 0.2)
                Text("\(starPoint)")
                    .mongsFont(14)
                    .foregroundStyle(MongsColor.darkBrown)
                    .lineLimit(1)
                    .frame(width: (width - 20) * 0.8)
            }
            .padding(.horizontal, 10)
        }
        .frame(width: width, height: height)
    }
}

/// 좌우 이동 버튼
///
/// Android `component/common/button/SelectButton.kt` + `LeftButton` / `RightButton` 이식.
/// 화면 양끝에 화살표를 두고 가운데는 비운다 — 끝에 닿으면 그쪽 화살표가 사라진다.
struct SelectButton: View {

    var canGoPrevious: Bool
    var canGoNext: Bool
    let onPrevious: () -> Void
    let onNext: () -> Void

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        HStack(spacing: 0) {
            arrow("btn_icon_left", isVisible: canGoPrevious, action: onPrevious)
            Spacer(minLength: 0)
            arrow("btn_icon_right", isVisible: canGoNext, action: onNext)
        }
        .padding(.horizontal, 15)
    }

    @ViewBuilder
    private func arrow(_ name: String, isVisible: Bool, action: @escaping () -> Void) -> some View {
        // 원본은 끝에 닿으면 버튼을 **그리지 않는다**(투명도를 낮추는 게 아니다).
        // 자리는 유지해야 가운데 내용이 흔들리지 않으므로 크기는 남긴다.
        Button(action: action) {
            AnimatedSprite(sprite: isVisible ? loader.sprite(named: name) : nil)
                .frame(width: 18, height: 35)
        }
        .buttonStyle(.plain)
        .disabled(!isVisible)
    }
}

/// 로고
///
/// Android `component/common/logo/Logo.kt` 이식.
///
/// 알이 부화하는 연출이다:
/// - `isOpen == false` → 닫힌 알 (`icon_logo_not_open`). 문구가 없다. **로그인 버튼이 보일 때**
/// - `isOpen == true` → 깨진 알 사이로 "mongs" 가 드러난다 (`icon_logo_open`). **로그인 진행 중**
///
/// 원본이 `Logo(isOpen = !signInButton)` 로 넘기는 것과 같다 — 버튼이 사라지고 로딩이 도는 동안
/// 알이 열린다. 이름만 보고 반대로 걸기 쉬우니 주의.
struct MongsLogo: View {

    /// 원본 크기도 다르다 — 열린 쪽 75×90, 닫힌 쪽 80×95.
    var isOpen: Bool = false

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        AnimatedSprite(sprite: loader.sprite(named: isOpen ? "icon_logo_open" : "icon_logo_not_open"))
            .frame(width: isOpen ? 75 : 80, height: isOpen ? 90 : 95)
    }
}
