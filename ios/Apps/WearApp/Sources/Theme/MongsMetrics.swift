import SwiftUI
import WatchKit

/// 화면 크기 보정
///
/// Android 원본은 원형 **192~227dp 한 종류**에 맞춰 `.dp` 리터럴 538개를 하드코딩했다.
/// 중앙 상수 파일이 없어서 그 값들을 그대로 옮겼고, 기준이 된 화면은 46mm 다.
///
/// watchOS 는 폭이 훨씬 넓게 갈린다:
///
/// | 기기 | pt | 배율 |
/// |---|---|---|
/// | SE 3 40mm | 162×197 | 0.78 |
/// | SE 3 44mm | 184×224 | 0.88 |
/// | Series 11 42mm | 187×223 | 0.90 |
/// | **Series 11 46mm (기준)** | **208×248** | **1.00** |
/// | Ultra 3 49mm | 211×257 | 1.01 |
///
/// 40mm 에서는 원형 버튼 3개 줄(54×3 + 8×2 = 178)이 화면(162)을 넘어 **양쪽이 잘려 나갔다.**
/// 세로는 이미 비율(`available * 0.2` 등)로 잡아 뒀으므로 **가로 비율만** 보정한다.
/// 세로까지 따로 보정하면 원본의 세로 비중이 어긋난다.
enum MongsMetrics {

    /// 이식 기준 화면 폭 — Apple Watch Series 11 46mm.
    static let referenceWidth: CGFloat = 208

    /// 기준 대비 현재 화면 배율.
    ///
    /// `screenBounds` 는 `@MainActor` 가 아니지만 기기 정보라 프로세스 수명 동안 바뀌지 않는다.
    /// 매 프레임 부르지 않도록 한 번만 계산해 둔다.
    static let scale: CGFloat = {
        let width = WKInterfaceDevice.current().screenBounds.width
        guard width > 0 else { return 1 }
        return width / referenceWidth
    }()
}

extension CGFloat {
    /// 기준 화면(46mm) 기준 치수를 현재 화면에 맞춘다.
    ///
    /// **규칙: Android dp 리터럴에서 온 숫자는 SwiftUI 치수가 되는 지점에서 딱 한 번 보정한다.**
    /// - 공용 컴포넌트(`MongsButton`, `MongsCircleButton`, 위젯들)의 파라미터는 **컴포넌트 안에서** 보정한다.
    ///   → 호출부는 원본 dp 를 그대로 넘긴다.
    /// - 화면 파일의 날 것 `frame` / `padding` / `spacing` 은 **호출부에서** `.ms` 를 붙인다.
    ///
    /// 두 번 붙이면 40mm 에서 0.61 배가 되어 눈에 띄게 작아진다.
    var ms: CGFloat { self * MongsMetrics.scale }
}

extension Int {
    var ms: CGFloat { CGFloat(self) * MongsMetrics.scale }
}

extension Double {
    var ms: CGFloat { CGFloat(self) * MongsMetrics.scale }
}
