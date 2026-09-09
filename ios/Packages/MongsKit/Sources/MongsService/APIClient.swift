import Foundation
import MongsModel

/// 서버 클라이언트
///
/// Android `core/data-core/.../global/Module.kt` 가 OkHttp 클라이언트 2개 +
/// Retrofit 2개로 나눠 두던 것을 하나로 합쳤다. 요청이 `Endpoint.host` 로
/// 어느 서버인지, `requiresAuthorization` 으로 토큰이 필요한지 말한다.
public final class APIClient: Sendable {

    private let config: AppConfig
    private let transport: any HTTPTransport
    private let tokenStore: TokenStore
    private let refresher: SessionRefresher

    public init(
        config: AppConfig,
        tokenStore: TokenStore,
        transport: (any HTTPTransport)? = nil
    ) {
        self.config = config
        self.tokenStore = tokenStore
        self.transport = transport ?? URLSessionTransport(
            connectTimeout: config.connectTimeout,
            readTimeout: config.readTimeout
        )

        // 재발급 자체는 토큰 없이 discovery 로 보낸다. 그래서 재귀가 아니라
        // 별도의 최소 경로로 처리한다.
        let transport = self.transport
        let baseURL = config.discoveryAPIURL
        self.refresher = SessionRefresher(tokenStore: tokenStore) { session in
            await Self.reissue(session: session, baseURL: baseURL, transport: transport)
        }
    }

    // MARK: - 요청

    /// 봉투를 벗겨 `result` 만 돌려준다.
    public func request<T: Decodable & Sendable>(
        _ endpoint: Endpoint,
        as type: T.Type = T.self
    ) async throws -> T {
        let data = try await requestData(endpoint)
        do {
            return try JSONDecoder.mongs().decode(APIResponse<T>.self, from: data).result
        } catch {
            throw APIError.decoding(String(describing: error))
        }
    }

    /// 페이지 응답
    public func requestPage<Element: Decodable & Sendable>(
        _ endpoint: Endpoint,
        of type: Element.Type = Element.self
    ) async throws -> APIPageResponse<Element> {
        let data = try await requestData(endpoint)
        do {
            return try JSONDecoder.mongs().decode(APIPageResponse<Element>.self, from: data)
        } catch {
            throw APIError.decoding(String(describing: error))
        }
    }

    /// 응답 본문이 필요 없는 요청
    public func send(_ endpoint: Endpoint) async throws {
        _ = try await requestData(endpoint)
    }

    // MARK: - 내부

    private func requestData(_ endpoint: Endpoint) async throws -> Data {
        let session = endpoint.requiresAuthorization ? await tokenStore.session() : nil
        let request = try makeRequest(endpoint, session: session)

        let (data, response) = try await transport.send(request)

        guard response.statusCode == 401 else {
            return try validate(data: data, response: response)
        }

        // 401 → 재발급 후 한 번만 재시도한다.
        // 재시도한 요청이 또 401 이면 그건 세션 문제가 아니라 서버 문제다.
        guard let session, let renewed = await refresher.refresh(staleSession: session) else {
            throw APIError.unauthorized
        }

        let retried = try makeRequest(endpoint, session: renewed)
        let (retryData, retryResponse) = try await transport.send(retried)

        guard retryResponse.statusCode != 401 else { throw APIError.unauthorized }
        return try validate(data: retryData, response: retryResponse)
    }

    private func makeRequest(_ endpoint: Endpoint, session: Session?) throws -> URLRequest {
        let baseURL = switch endpoint.host {
        case .discovery: config.discoveryAPIURL
        case .gateway: config.gatewayAPIURL
        }

        guard var components = URLComponents(
            url: baseURL.appendingPathComponent(endpoint.path),
            resolvingAgainstBaseURL: false
        ) else {
            throw APIError.transport("URL 을 만들 수 없다: \(endpoint.path)")
        }

        if !endpoint.query.isEmpty {
            components.queryItems = endpoint.query
                .sorted { $0.key < $1.key }
                .map { URLQueryItem(name: $0.key, value: $0.value) }
        }

        guard let url = components.url else {
            throw APIError.transport("URL 을 만들 수 없다: \(endpoint.path)")
        }

        var request = URLRequest(url: url)
        request.httpMethod = endpoint.method.rawValue
        request.httpBody = endpoint.body
        if endpoint.body != nil {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }
        if let session {
            request.setValue("Bearer \(session.accessToken)", forHTTPHeaderField: "Authorization")
        }
        return request
    }

    /// 상태 코드와 오류 봉투를 검사한다.
    ///
    /// Android `HttpConst.kt` 규약: 401 = 인증 실패(재발급 대상),
    /// 403 = 인가 실패(세션 종료), 500 = 서버 연결 실패.
    private func validate(data: Data, response: HTTPURLResponse) throws -> Data {
        switch response.statusCode {
        case 200 ..< 300:
            return data
        case 401:
            throw APIError.unauthenticated
        case 403:
            throw APIError.unauthorized
        case 500...:
            throw APIError.serverUnreachable(statusCode: response.statusCode)
        default:
            // 서버가 오류 봉투를 실어 보냈으면 코드를 꺼낸다.
            // `DISCOVERY-ACCOUNT-101`(미가입) 같은 분기가 여기에 걸린다.
            if let envelope = try? JSONDecoder.mongs().decode(ErrorEnvelope.self, from: data) {
                throw APIError.server(
                    code: envelope.code ?? "-",
                    message: envelope.message,
                    httpStatus: envelope.httpStatus ?? response.statusCode
                )
            }
            throw APIError.server(code: "-", message: nil, httpStatus: response.statusCode)
        }
    }

    private struct ErrorEnvelope: Decodable {
        let code: String?
        let message: String?
        let httpStatus: Int?
    }

    // MARK: - 재발급

    private struct ReissueRequest: Encodable {
        let accessToken: String
        let refreshToken: String
    }

    private struct ReissueResult: Decodable {
        let accessToken: String
        let refreshToken: String
    }

    /// `POST public/auth/reissue`
    ///
    /// 토큰을 붙이지 않고 discovery 로 직접 보낸다 — 일반 요청 경로를 타면
    /// 401 처리가 다시 재발급을 부르는 무한 재귀가 된다.
    private static func reissue(
        session: Session,
        baseURL: URL,
        transport: any HTTPTransport
    ) async -> (accessToken: String, refreshToken: String)? {
        var request = URLRequest(url: baseURL.appendingPathComponent("public/auth/reissue"))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try? JSONEncoder.mongs().encode(
            ReissueRequest(accessToken: session.accessToken, refreshToken: session.refreshToken)
        )

        guard
            let (data, response) = try? await transport.send(request),
            (200 ..< 300).contains(response.statusCode),
            let envelope = try? JSONDecoder.mongs().decode(APIResponse<ReissueResult>.self, from: data)
        else {
            return nil
        }

        return (envelope.result.accessToken, envelope.result.refreshToken)
    }
}
