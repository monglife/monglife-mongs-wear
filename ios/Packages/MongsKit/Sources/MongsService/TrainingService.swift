import Foundation
import MongsModel

/// 훈련
///
/// Android `data/mong-data/.../web/adapter/ActivityWebAdapter.kt` 이식.
public actor TrainingService {

    private let api: APIClient
    private let mongService: MongService

    public init(api: APIClient, mongService: MongService) {
        self.api = api
        self.mongService = mongService
    }

    /// 훈련 종류 목록.
    ///
    /// **화면이 없는 종류는 걸러낸다** — 서버가 나중에 축구·참참참을 켜도
    /// 앱이 빈 화면으로 들어가지 않게 한다.
    public func types() async throws -> [TrainingType] {
        let all: [TrainingType] = try await api.request(
            Endpoint(host: .gateway, method: .get, path: "character/activity/training")
        )
        return all.filter { $0.game != nil }
    }

    /// 훈련 종료. 점수를 보내면 서버가 보상과 갱신된 스탯을 돌려준다.
    ///
    /// **성패도 보상도 서버가 정한다.** 클라이언트는 점수만 보고한다.
    @discardableResult
    public func end(code: String, score: Int) async throws -> TrainingResult {
        guard let mong = await mongService.currentMong() else { throw MongError.noMong }

        struct Request: Encodable {
            let trainingCode: String
            let mongId: Int64
            let score: Int
        }

        let result: TrainingResult = try await api.request(try Endpoint.json(
            host: .gateway,
            method: .post,
            path: "character/activity/training",
            body: Request(trainingCode: code, mongId: mong.mongId, score: score)
        ))

        await mongService.apply(trainingResult: result)
        return result
    }
}
