import SwiftUI

/// 디자인 토큰
///
/// Android `wear-view-presentation/.../assets/Theme.kt` 이식.
/// 원본은 Wear Material 팔레트를 만들지 않고 **평평한 Color 상수 15개**를 각 컴포넌트가
/// 직접 import 해서 쓴다. 그 구조를 그대로 옮겼다 — 의미 기반 토큰(primary/surface)으로
/// 재해석하면 원본과 색이 미묘하게 어긋난다.
enum MongsColor {
    static let red = Color(hex: 0xE90B0B)
    static let pink = Color(hex: 0xFF6B6B)
    static let pink200 = Color(hex: 0xFED9D9)
    static let green = Color(hex: 0x3BE368)
    static let darkGreen = Color(hex: 0x179B3D)
    static let yellow = Color(hex: 0xFFDA2D)
    static let darkYellow = Color(hex: 0xFFEB3B)
    static let blue = Color(hex: 0x8DCEFE)
    static let purple = Color(hex: 0xCCA2FE)
    static let darkPurple = Color(hex: 0x8F47E6)
    static let navy = Color(hex: 0x0C4DA2)
    static let white = Color(hex: 0xFFFFFF)
    static let lightGray = Color(hex: 0xF0F0F0)
    static let darkGray = Color(hex: 0x737373)
    static let darkBrown = Color(hex: 0x5D4037)
}

extension Color {
    init(hex: UInt32) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: 1
        )
    }
}

/// 픽셀 폰트
///
/// Android 는 `Typography(defaultFontFamily = DAL_MU_RI)` 로 **앱 전체 기본 폰트**를 바꾼다.
/// SwiftUI 에는 그런 전역 스위치가 없어서, `.mongsFont(...)` 를 쓰는 쪽에서 명시한다.
///
/// 번들에 넣으려면 `Info.plist` 의 `UIAppFonts` 에도 파일 이름을 적어야 한다.
enum MongsFont {
    /// 폰트 파일의 PostScript 이름
    static let name = "dalmoori"

    static func regular(_ size: CGFloat) -> Font {
        .custom(name, size: size)
    }
}

extension View {
    /// 픽셀 폰트를 적용한다.
    ///
    /// 폰트를 못 찾으면 SwiftUI 가 시스템 폰트로 조용히 떨어진다 —
    /// 화면이 깨지지는 않지만 분위기가 완전히 달라지므로, 폰트가 안 보이면
    /// `UIAppFonts` 등록과 번들 포함 여부를 먼저 확인한다.
    func mongsFont(_ size: CGFloat) -> some View {
        font(MongsFont.regular(size))
    }
}
