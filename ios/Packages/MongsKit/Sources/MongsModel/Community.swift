import Foundation

/// 공지사항
///
/// Android `GetNoticeResponseDto` 이식.
public struct Notice: Decodable, Sendable, Equatable, Identifiable {

    public init(
        noticeId: Int64, title: String, content: String,
        writerName: String, createdAt: Date, updatedAt: Date
    ) {
        self.noticeId = noticeId
        self.title = title
        self.content = content
        self.writerName = writerName
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }

    public var id: Int64 { noticeId }

    public let noticeId: Int64
    public let title: String
    public let content: String
    public let writerName: String
    public let createdAt: Date
    public let updatedAt: Date
}

/// 랜덤 뽑기 결과
///
/// Android `RandomDrawResponseDto` 이식.
/// 뽑힌 것이 먹이/간식/맵 중 무엇인지는 `inventoryTypeCode` 가 알려준다.
public struct RandomDrawResult: Decodable, Sendable, Equatable {

    public init(randomDrawCode: String, randomDrawName: String, inventoryTypeCode: InventoryTypeCode) {
        self.randomDrawCode = randomDrawCode
        self.randomDrawName = randomDrawName
        self.inventoryTypeCode = inventoryTypeCode
    }

    public let randomDrawCode: String
    public let randomDrawName: String
    public let inventoryTypeCode: InventoryTypeCode
}

extension MongResponse {

    /// 뽑기권 구매 결과. Android `BuyRandomDrawTicketResponseDto`.
    public struct RandomDrawTicket: Decodable, Sendable {
        public let mongId: Int64
        public let payPoint: Int
        public let randomDrawTicketCount: Int
    }
}

extension Mong {

    /// 뽑기권을 사면 페이포인트와 티켓 수만 바뀐다.
    public func applying(_ response: MongResponse.RandomDrawTicket) -> Mong {
        var next = self
        next.payPoint = response.payPoint
        next.randomDrawTicketCount = response.randomDrawTicketCount
        return next
    }
}
