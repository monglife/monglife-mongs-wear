import Foundation
import MongsModel

/// 도감 / 맵 탐색
///
/// Android `data/member-data/.../collection` 이식.
public actor CollectionService {

    private let api: APIClient
    private let location: LocationClient

    public init(api: APIClient, location: LocationClient) {
        self.api = api
        self.location = location
    }

    /// 도감 목록. 몽과 맵이 경로만 다르고 모양이 같다.
    public func items(kind: CollectionItem.Kind) async throws -> [CollectionItem] {
        let endpoint = Endpoint(host: .gateway, method: .get, path: "user/collection/\(kind.path)")
        switch kind {
        case .mong:
            let list: [CollectionResponse.Mong] = try await api.request(endpoint)
            return list.map(\.item)
        case .map:
            let list: [CollectionResponse.Map] = try await api.request(endpoint)
            return list.map(\.item)
        }
    }

    /// 현재 위치로 맵을 탐색한다.
    ///
    /// 좌표는 **탐색을 누른 순간 한 번만** 읽는다 (원본과 같다).
    public func searchMap() async throws -> MapSearchResult {
        let coordinate = try await location.current()

        struct Request: Encodable {
            let latitude: Double
            let longitude: Double
        }

        let response: CollectionResponse.Search = try await api.request(try Endpoint.json(
            host: .gateway,
            method: .post,
            path: "user/collection/map",
            body: Request(latitude: coordinate.latitude, longitude: coordinate.longitude)
        ))
        return response.result
    }
}
