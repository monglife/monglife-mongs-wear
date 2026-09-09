import Foundation

/// 펫 스프라이트 리소스
///
/// Android `presentation/wear-view-presentation/.../assets/MongResourceCode.kt` 에서 생성.
/// 원본은 `R.drawable` int 를 들고 있지만 여기서는 번들 파일 이름(확장자 없음)을 쓴다.
///
/// 표정 GIF 7종은 표정을 가진 캐릭터가 전부 같은 파일을 공유하므로 상수로 뺐다.
/// 원본은 캐릭터마다 같은 값을 7번씩 반복해 적고 있었다.
///
/// `xOffset`/`yOffset` 은 몸통 위에 표정을 얹는 위치다 — 캐릭터마다 다르고, 눈으로
/// 맞춘 값이라 그대로 가져와야 한다.
public enum MongResourceCode: String, Sendable, CaseIterable {

    case ch000 = "CH000"
    case ch001 = "CH001"
    case ch002 = "CH002"
    case ch003 = "CH003"
    case ch004 = "CH004"
    case ch005 = "CH005"
    case ch100 = "CH100"
    case ch101 = "CH101"
    case ch102 = "CH102"
    case ch104 = "CH104"
    case ch105 = "CH105"
    case ch200 = "CH200"
    case ch201 = "CH201"
    case ch202 = "CH202"
    case ch203 = "CH203"
    case ch204 = "CH204"
    case ch205 = "CH205"
    case ch210 = "CH210"
    case ch211 = "CH211"
    case ch212 = "CH212"
    case ch214 = "CH214"
    case ch215 = "CH215"
    case ch220 = "CH220"
    case ch221 = "CH221"
    case ch222 = "CH222"
    case ch230 = "CH230"
    case ch231 = "CH231"
    case ch232 = "CH232"
    case ch300 = "CH300"
    case ch301 = "CH301"
    case ch302 = "CH302"
    case ch303 = "CH303"
    case ch304 = "CH304"
    case ch305 = "CH305"
    case ch310 = "CH310"
    case ch311 = "CH311"
    case ch312 = "CH312"
    case ch314 = "CH314"
    case ch315 = "CH315"
    case ch320 = "CH320"
    case ch321 = "CH321"
    case ch322 = "CH322"
    case ch324 = "CH324"
    case ch325 = "CH325"
    case ch330 = "CH330"
    case ch331 = "CH331"
    case ch332 = "CH332"
    case ch334 = "CH334"
    case ch335 = "CH335"
    case ch444 = "CH444"

    /// 서버가 모르는 코드를 내려보내도 앱이 죽지 않도록 기본값으로 떨어진다.
    /// Android `ResourceCodeResolver.resolveResourceCode()` 대응 — 거기서는 R8 이
    /// enum 상수 이름을 지우지 않도록 keep 룰이 필요했지만 Swift 는 그런 걱정이 없다.
    public static func resolve(_ code: String) -> MongResourceCode {
        MongResourceCode(rawValue: code) ?? .ch444
    }

    /// 정적 이미지 파일 이름 (확장자 없음)
    public var pngName: String {
        switch self {

        case .ch000: "mong_body_ch000"
        case .ch001: "mong_body_ch001"
        case .ch002: "mong_body_ch002"
        case .ch003: "mong_body_ch003"
        case .ch004: "mong_body_ch004"
        case .ch005: "mong_body_ch005"
        case .ch100: "mong_body_ch100"
        case .ch101: "mong_body_ch101"
        case .ch102: "mong_body_ch102"
        case .ch104: "mong_body_ch104"
        case .ch105: "mong_body_ch105"
        case .ch200: "mong_body_ch200"
        case .ch201: "mong_body_ch201"
        case .ch202: "mong_body_ch202"
        case .ch203: "mong_body_ch203"
        case .ch204: "mong_body_ch204"
        case .ch205: "mong_body_ch205"
        case .ch210: "mong_body_ch210"
        case .ch211: "mong_body_ch211"
        case .ch212: "mong_body_ch212"
        case .ch214: "mong_body_ch214"
        case .ch215: "mong_body_ch215"
        case .ch220: "mong_body_ch220"
        case .ch221: "mong_body_ch221"
        case .ch222: "mong_body_ch222"
        case .ch230: "mong_body_ch230"
        case .ch231: "mong_body_ch231"
        case .ch232: "mong_body_ch232"
        case .ch300: "mong_body_ch300"
        case .ch301: "mong_body_ch301"
        case .ch302: "mong_body_ch302"
        case .ch303: "mong_body_ch303"
        case .ch304: "mong_body_ch304"
        case .ch305: "mong_body_ch305"
        case .ch310: "mong_body_ch310"
        case .ch311: "mong_body_ch311"
        case .ch312: "mong_body_ch312"
        case .ch314: "mong_body_ch314"
        case .ch315: "mong_body_ch315"
        case .ch320: "mong_body_ch320"
        case .ch321: "mong_body_ch321"
        case .ch322: "mong_body_ch322"
        case .ch324: "mong_body_ch324"
        case .ch325: "mong_body_ch325"
        case .ch330: "mong_body_ch330"
        case .ch331: "mong_body_ch331"
        case .ch332: "mong_body_ch332"
        case .ch334: "mong_body_ch334"
        case .ch335: "mong_body_ch335"
        case .ch444: "mong_none"
        }
    }

    /// 애니메이션 파일 이름. GIF 가 없는 캐릭터는 정적 이미지와 같은 이름이다.
    public var animationName: String {
        switch self {

        case .ch000: "mong_body_ch000_gif"
        case .ch001: "mong_body_ch001_gif"
        case .ch002: "mong_body_ch002_gif"
        case .ch003: "mong_body_ch003_gif"
        case .ch004: "mong_body_ch004_gif"
        case .ch005: "mong_body_ch005_gif"
        case .ch100: "mong_body_ch100_gif"
        case .ch101: "mong_body_ch101_gif"
        case .ch102: "mong_body_ch102_gif"
        case .ch104: "mong_body_ch104"
        case .ch105: "mong_body_ch105"
        case .ch200: "mong_body_ch200_gif"
        case .ch201: "mong_body_ch201_gif"
        case .ch202: "mong_body_ch202_gif"
        case .ch203: "mong_body_ch203"
        case .ch204: "mong_body_ch204"
        case .ch205: "mong_body_ch205"
        case .ch210: "mong_body_ch210_gif"
        case .ch211: "mong_body_ch211_gif"
        case .ch212: "mong_body_ch212_gif"
        case .ch214: "mong_body_ch214"
        case .ch215: "mong_body_ch215"
        case .ch220: "mong_body_ch220_gif"
        case .ch221: "mong_body_ch221_gif"
        case .ch222: "mong_body_ch222_gif"
        case .ch230: "mong_body_ch230_gif"
        case .ch231: "mong_body_ch231_gif"
        case .ch232: "mong_body_ch232_gif"
        case .ch300: "mong_body_ch300_gif"
        case .ch301: "mong_body_ch301_gif"
        case .ch302: "mong_body_ch302_gif"
        case .ch303: "mong_body_ch303"
        case .ch304: "mong_body_ch304"
        case .ch305: "mong_body_ch305"
        case .ch310: "mong_body_ch310_gif"
        case .ch311: "mong_body_ch311_gif"
        case .ch312: "mong_body_ch312_gif"
        case .ch314: "mong_body_ch314"
        case .ch315: "mong_body_ch315"
        case .ch320: "mong_body_ch320_gif"
        case .ch321: "mong_body_ch321_gif"
        case .ch322: "mong_body_ch322_gif"
        case .ch324: "mong_body_ch324"
        case .ch325: "mong_body_ch325"
        case .ch330: "mong_body_ch330_gif"
        case .ch331: "mong_body_ch331_gif"
        case .ch332: "mong_body_ch332_gif"
        case .ch334: "mong_body_ch334"
        case .ch335: "mong_body_ch335"
        case .ch444: "mong_none"
        }
    }

    /// 표정 레이어를 얹는 캐릭터인지
    public var hasExpression: Bool {
        switch self {

        case .ch100, .ch101, .ch102, .ch200, .ch201, .ch202, .ch210, .ch211, .ch212, .ch220, .ch221, .ch222, .ch230, .ch231, .ch232, .ch300, .ch301, .ch302, .ch310, .ch311, .ch312, .ch320, .ch321, .ch322, .ch330, .ch331, .ch332: true
        default: false
        }
    }

    /// 표정 레이어 오프셋 (몸통 기준, 포인트)
    public var expressionOffset: CGPoint {
        switch self {

        case .ch000, .ch001, .ch002, .ch003, .ch004, .ch005, .ch104, .ch105, .ch203, .ch204, .ch205, .ch214, .ch215, .ch303, .ch304, .ch305, .ch314, .ch315, .ch324, .ch325, .ch334, .ch335, .ch444: CGPoint(x: 0, y: 0)
        case .ch100, .ch102: CGPoint(x: 0, y: -4)
        case .ch101: CGPoint(x: 0, y: -2)
        case .ch200, .ch202, .ch210, .ch212, .ch220, .ch222, .ch230, .ch232: CGPoint(x: 0, y: -16)
        case .ch201, .ch211, .ch221, .ch231: CGPoint(x: -11, y: -13)
        case .ch300, .ch310, .ch320, .ch330: CGPoint(x: 0, y: -15)
        case .ch301, .ch311, .ch321, .ch331: CGPoint(x: -23, y: -13)
        case .ch302, .ch312, .ch322, .ch332: CGPoint(x: 0, y: -8)
        }
    }
}

/// 표정 스프라이트 — 표정을 가진 캐릭터가 모두 공유한다.
public enum MongExpression: String, Sendable, CaseIterable {
    case normal, smile, happy, sad, sulky, depressed, sleeping, eating

    public var spriteName: String { "mong_face_\(rawValue)" }
}
