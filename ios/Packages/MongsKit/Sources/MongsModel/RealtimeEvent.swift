import Foundation

/// MQTT 로 밀려 들어오는 이벤트들
///
/// Android `data/*/persistence/dto/*EventDto.kt` 이식.
/// 페이로드는 HTTP 와 같은 봉투(`ResponseDto`)에 담겨 온다 — `APIResponse` 를 그대로 쓴다.
public enum RealtimeEvent {

    /// 펫 상태 갱신
    ///
    /// Android `ManagementEventDto`. **`level` / `sleepAt` / `wakeupAt` 이 없다** —
    /// 원본도 그 세 값은 기존 행에서 가져와 유지한다.
    public struct Management: Decodable, Sendable {
        public let mongId: Int64
        public let name: String
        public let mongCode: String
        public let mongName: String
        public let payPoint: Int
        public let stateCode: MongStateCode
        public let statusCode: MongStatusCode
        public let isSleep: Bool
        public let weight: Double
        public let expRatio: Double
        public let strengthRatio: Double
        public let satietyRatio: Double
        public let healthyRatio: Double
        public let fatigueRatio: Double
        public let poopCount: Int
        public let createdAt: Date
        public let updatedAt: Date
    }

    /// 별가루 잔액 변경. Android `PlayerStarPointEventDto`.
    public struct StarPoint: Decodable, Sendable {
        public let accountId: Int64
        public let starPoint: Int
    }

    /// 슬롯 수 변경. Android `PlayerSlotCountEventDto`.
    public struct SlotCount: Decodable, Sendable {
        public let accountId: Int64
        public let slotCount: Int
    }

    /// 걸음 복구
    ///
    /// Android `StepRestoreEventDto`. 환전 요청은 성공했는데 그 뒤 페이포인트 지급이
    /// 실패했을 때 온다. 잔액은 기기에만 있으므로 서버는 되돌릴 금액만 알려 줄 수 있다.
    /// `eventId` 는 서버 롤백의 transactionId 라 재전달되어도 값이 같다 — 중복 제거의 키다.
    public struct StepRestore: Decodable, Sendable {
        public let deviceId: String
        public let restoreWalkingCount: Int
        public let eventId: String
    }
}

extension Mong {

    /// MQTT 푸시를 반영한다.
    ///
    /// 이벤트에 없는 `level` / `sleepAt` / `wakeupAt` / `randomDrawTicketCount` 는
    /// 기존 값을 유지한다 — Android `ManagementPersistenceAdapter` 도 같다.
    public func applying(_ event: RealtimeEvent.Management) -> Mong {
        var next = self
        next.name = event.name
        next.mongCode = event.mongCode
        next.mongName = event.mongName
        next.payPoint = event.payPoint
        next.stateCode = event.stateCode
        next.statusCode = event.statusCode
        next.isSleep = event.isSleep
        next.weight = event.weight
        next.expRatio = event.expRatio
        next.strengthRatio = event.strengthRatio
        next.satietyRatio = event.satietyRatio
        next.healthyRatio = event.healthyRatio
        next.fatigueRatio = event.fatigueRatio
        next.poopCount = event.poopCount
        next.updatedAt = event.updatedAt
        return next
    }
}
