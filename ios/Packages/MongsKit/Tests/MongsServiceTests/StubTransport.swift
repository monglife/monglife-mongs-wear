import Foundation
@testable import MongsService

/// 테스트용 HTTP 스텁
///
/// 요청을 순서대로 기록하고, 경로별로 미리 정해 둔 응답을 돌려준다.
/// 401 → 재발급 → 재시도 흐름을 네트워크 없이 재현하기 위한 것이다.
final class StubTransport: HTTPTransport, @unchecked Sendable {

    struct Reply {
        let status: Int
        let body: Data

        static func ok(_ json: String) -> Reply { Reply(status: 200, body: Data(json.utf8)) }
        static func status(_ code: Int, _ json: String = "{}") -> Reply {
            Reply(status: code, body: Data(json.utf8))
        }
    }

    private let lock = NSLock()
    private var _requests: [URLRequest] = []
    /// 경로 조각 → 그 경로가 호출될 때마다 순서대로 꺼내 쓸 응답들.
    /// 다 떨어지면 마지막 응답을 계속 재사용한다.
    private var replies: [String: [Reply]] = [:]
    private var _hits: [String: Int] = [:]

    var requests: [URLRequest] { lock.withLock { _requests } }

    /// 경로 조각이 몇 번 호출됐는지
    func hits(_ pathFragment: String) -> Int { lock.withLock { _hits[pathFragment] ?? 0 } }

    func stub(_ pathFragment: String, _ replies: [Reply]) {
        lock.withLock { self.replies[pathFragment] = replies }
    }

    func send(_ request: URLRequest) async throws -> (Data, HTTPURLResponse) {
        let path = request.url?.path ?? ""

        let reply: Reply? = lock.withLock {
            _requests.append(request)
            guard let key = replies.keys.first(where: { path.contains($0) }) else { return nil }

            let index = _hits[key] ?? 0
            _hits[key] = index + 1

            let list = replies[key]!
            return list[min(index, list.count - 1)]
        }

        guard let reply else {
            return (Data("{}".utf8), HTTPURLResponse(
                url: request.url!, statusCode: 404, httpVersion: nil, headerFields: nil
            )!)
        }

        let response = HTTPURLResponse(
            url: request.url!, statusCode: reply.status, httpVersion: nil, headerFields: nil
        )!
        return (reply.body, response)
    }
}

extension URLRequest {
    var authorizationHeader: String? { value(forHTTPHeaderField: "Authorization") }
}
