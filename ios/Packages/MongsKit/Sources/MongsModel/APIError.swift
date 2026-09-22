import Foundation

/// 서버/전송 오류
///
/// Android `core/data-core/.../web/constant/HttpConst.kt` 의 상태 코드 규약을 따른다.
/// 특히 **401 과 403 의 의미가 갈린다**:
/// - 401 = 인증 실패 → 토큰 재발급을 시도해 볼 수 있다
/// - 403 = 인가 실패 → 재발급까지 실패해 세션이 죽었다. 로그인 화면으로 보낸다
public enum APIError: Error, Equatable, Sendable {

    /// 서버가 오류 코드를 담아 응답했다 (`{code, message, httpStatus}`)
    case server(code: String, message: String?, httpStatus: Int)

    /// 인증 실패 (401). 재발급 대상이다.
    case unauthenticated

    /// 인가 실패 (403). 세션이 끝났다.
    case unauthorized

    /// 서버 연결 실패 (5xx)
    case serverUnreachable(statusCode: Int)

    /// 네트워크 자체가 실패 (오프라인, 타임아웃 등)
    case transport(String)

    /// 응답을 해석할 수 없다
    case decoding(String)

    public var message: String {
        switch self {
        case let .server(_, message, _): message ?? "요청을 처리하지 못했습니다."
        case .unauthenticated: "권한 인증 실패"
        case .unauthorized: "권한 인가 실패"
        case .serverUnreachable: "서버 연결 실패"
        case .transport: "네트워크에 연결할 수 없습니다."
        case .decoding: "응답을 해석하지 못했습니다."
        }
    }

    /// 서버가 내려준 오류 코드 (`DISCOVERY-ACCOUNT-101` 같은 것)
    public var serverCode: String? {
        if case let .server(code, _, _) = self { return code }
        return nil
    }

    /// 사용자에게 배너로 보여줄 오류인지.
    /// Android `ErrorCode.isMessageShow()` 대응.
    public var isMessageShown: Bool {
        switch self {
        case .unauthenticated, .unauthorized: false   // 화면 전환으로 처리한다
        default: true
        }
    }
}

extension APIError {
    /// 미가입 계정. 서버가 이 코드를 주면 join 후 login 을 다시 시도한다.
    /// Android `AuthWebAdapter` 의 `NeedJoinException` 분기와 같은 코드다.
    public static let needJoinCode = "DISCOVERY-ACCOUNT-101"

    public var isNeedJoin: Bool { serverCode == Self.needJoinCode }
}
