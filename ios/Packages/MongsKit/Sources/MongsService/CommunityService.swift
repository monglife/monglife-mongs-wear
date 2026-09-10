import Foundation
import MongsModel

/// 공지사항 / 오류 신고
///
/// Android `data/member-data/.../notice` + `.../feedback` 이식.
/// 둘 다 화면 하나에 엔드포인트 하나뿐이라 한 서비스로 묶었다.
public actor CommunityService {

    private let api: APIClient
    private let identity: ClientIdentity

    public init(api: APIClient, identity: ClientIdentity) {
        self.api = api
        self.identity = identity
    }

    /// 공지 목록. 서버가 페이징으로 준다.
    public func notices(page: Int = 1, size: Int = 10) async throws -> [Notice] {
        let response: APIPageResponse<Notice> = try await api.requestPage(
            Endpoint(
                host: .gateway,
                method: .get,
                path: "user/notice",
                query: ["page": "\(page)", "size": "\(size)"]
            )
        )
        return response.result
    }

    /// 오류 신고 등록.
    ///
    /// `deviceName` 은 서버가 재현 환경을 알 수 있게 함께 보낸다 (Android 도 같다).
    public func submitFeedback(title: String, content: String) async throws {
        struct Request: Encodable {
            let deviceName: String
            let title: String
            let content: String
        }
        try await api.send(try Endpoint.json(
            host: .gateway,
            method: .post,
            path: "user/feedback",
            body: Request(deviceName: identity.deviceName, title: title, content: content)
        ))
    }
}
