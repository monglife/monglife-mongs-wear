import Foundation
import MongsModel

/// 걸음 환전의 서버 왕복
///
/// Android `DeviceWebClient.exchangeCurrentWalkingCount` 이식.
///
/// **응답 본문을 쓰지 않는다** — 지급된 페이포인트는 몽 정보로 따로 내려오고,
/// 걸음 잔액은 기기 로컬에만 있어 서버가 돌려줄 것이 없다 (원본 주석 그대로).
///
/// `StepService` 구현들이 이 클로저를 받아 쓰기 때문에 타입이 아니라 팩토리로 둔다 —
/// `MongsService` 안에서 `MongService` 를 참조하는 순환을 만들지 않기 위해서다.
public enum StepExchangeClient {

    /// - Returns: 차감한 걸음 수를 받아 서버에 알리는 클로저.
    ///   현재 몽이 없으면 아무것도 하지 않는다 (환전 화면은 몽이 있어야 열린다).
    public static func make(
        api: APIClient,
        currentMongId: @escaping @Sendable () async -> Int64?
    ) -> @Sendable (Int) async throws -> Void {
        { walkingCount in
            guard let mongId = await currentMongId() else { return }

            struct Request: Encodable {
                let mongId: Int64
                let walkingCount: Int
            }
            try await api.send(try Endpoint.json(
                host: .gateway,
                method: .post,
                path: "user/step/exchange",
                body: Request(mongId: mongId, walkingCount: walkingCount)
            ))
        }
    }
}
