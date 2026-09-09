import Foundation
import MongsModel

/// 서버 요청 정의
///
/// Android 는 Retrofit 인터페이스 12개로 나뉘어 있지만, 여기서는 값 타입 하나로 표현하고
/// 서비스가 그때그때 만들어 쓴다.
public struct Endpoint: Sendable {

    /// 어느 서버로 보낼지
    ///
    /// Android 는 Retrofit 인스턴스를 두 개 두고 `@Named` 로 구분한다:
    /// - discovery: 인증 전용. 토큰을 붙이지 않는다.
    /// - gateway: 그 외 전부. `Authorization: Bearer` 가 필요하다.
    public enum Host: Sendable {
        case discovery
        case gateway
    }

    public enum Method: String, Sendable {
        case get = "GET"
        case post = "POST"
        case put = "PUT"
        case patch = "PATCH"
        case delete = "DELETE"
    }

    public let host: Host
    public let method: Method
    /// 베이스 URL 뒤에 붙는 경로. 앞에 `/` 를 붙이지 않는다 (`public/auth/login`).
    public let path: String
    public let query: [String: String]
    public let body: Data?
    /// 토큰을 붙일지. discovery 호출은 대부분 false 다.
    public let requiresAuthorization: Bool

    public init(
        host: Host,
        method: Method,
        path: String,
        query: [String: String] = [:],
        body: Data? = nil,
        requiresAuthorization: Bool = true
    ) {
        self.host = host
        self.method = method
        self.path = path
        self.query = query
        self.body = body
        self.requiresAuthorization = requiresAuthorization
    }

    /// JSON 본문을 인코딩해 넣는다.
    public static func json(
        host: Host,
        method: Method,
        path: String,
        query: [String: String] = [:],
        body: some Encodable,
        requiresAuthorization: Bool = true
    ) throws -> Endpoint {
        Endpoint(
            host: host,
            method: method,
            path: path,
            query: query,
            body: try JSONEncoder.mongs().encode(body),
            requiresAuthorization: requiresAuthorization
        )
    }
}
