import Foundation

/// 몽 관리 API 응답
///
/// Android `data/mong-data/.../web/client/response/ManagementResponseDto.kt` 이식.
///
/// **응답마다 실려 오는 필드가 다르다.** 전체 몽을 주는 것은 조회·생성뿐이고,
/// 나머지는 자기가 바꾼 필드만 준다. 그래서 각 응답이 캐시된 몽에 **자기 필드만 병합**한다
/// (Android `Mong.kt` 의 stroke/sleeping/poopClean/evolution 메서드가 하던 일).
public enum MongResponse {}

extension MongResponse {

    /// 몽 전체 (조회 / 생성 공통)
    public struct Full: Decodable, Sendable {
        public let mongId: Int64
        public let name: String
        public let mongCode: String
        public let mongName: String
        public let stateCode: MongStateCode
        public let statusCode: MongStatusCode
        public let level: Int
        public let sleepAt: Date
        public let wakeupAt: Date
        public let payPoint: Int
        public let isSleep: Bool
        public let strengthRatio: Double
        public let healthyRatio: Double
        public let satietyRatio: Double
        public let fatigueRatio: Double
        public let expRatio: Double
        public let weight: Double
        public let poopCount: Int
        public let randomDrawTicketCount: Int
        public let createdAt: Date
        public let updatedAt: Date

        public var mong: Mong {
            Mong(
                mongId: mongId, name: name, mongCode: mongCode, mongName: mongName,
                stateCode: stateCode, statusCode: statusCode, level: level,
                sleepAt: sleepAt, wakeupAt: wakeupAt, payPoint: payPoint, isSleep: isSleep,
                strengthRatio: strengthRatio, healthyRatio: healthyRatio,
                satietyRatio: satietyRatio, fatigueRatio: fatigueRatio,
                expRatio: expRatio, weight: weight, poopCount: poopCount,
                randomDrawTicketCount: randomDrawTicketCount,
                createdAt: createdAt, updatedAt: updatedAt
            )
        }
    }

    /// `mongId` 만 돌려주는 응답 (삭제 / 졸업)
    public struct IdOnly: Decodable, Sendable {
        public let mongId: Int64
    }

    /// 쓰다듬기
    public struct Stroke: Decodable, Sendable {
        public let mongId: Int64
        public let expRatio: Double
        public let createdAt: Date
        public let updatedAt: Date
    }

    /// 수면 / 기상
    public struct Sleep: Decodable, Sendable {
        public let mongId: Int64
        public let isSleep: Bool
        public let createdAt: Date
        public let updatedAt: Date
    }

    /// 배변 처리
    public struct PoopClean: Decodable, Sendable {
        public let mongId: Int64
        public let expRatio: Double
        public let poopCount: Int
        public let createdAt: Date
        public let updatedAt: Date
    }

    /// 진화
    public struct Evolution: Decodable, Sendable {
        public let mongId: Int64
        public let mongCode: String
        public let level: Int
        public let expRatio: Double
        public let strengthRatio: Double
        public let healthyRatio: Double
        public let satietyRatio: Double
        public let fatigueRatio: Double
        public let stateCode: MongStateCode
        public let statusCode: MongStatusCode
        public let createdAt: Date
        public let updatedAt: Date
    }
}

// MARK: - 부분 병합
//
// Android `Mong.kt` 의 상태변경 메서드들에 대응한다. 원본이 대입하던 필드 집합을 그대로 옮겼다 —
// 서버가 주지 않은 필드는 **건드리지 않는다**. 예를 들어 쓰다듬기는 경험치와 시각만 바꾸고
// 컨디션이나 몸무게는 그대로 둔다.

extension Mong {

    /// Android `Mong.stroke(...)`
    public func applying(_ response: MongResponse.Stroke) -> Mong {
        var next = self
        next.mongId = response.mongId
        next.expRatio = response.expRatio
        next.createdAt = response.createdAt
        next.updatedAt = response.updatedAt
        return next
    }

    /// Android `Mong.sleeping(...)`
    public func applying(_ response: MongResponse.Sleep) -> Mong {
        var next = self
        next.mongId = response.mongId
        next.isSleep = response.isSleep
        next.createdAt = response.createdAt
        next.updatedAt = response.updatedAt
        return next
    }

    /// Android `Mong.poopClean(...)`
    public func applying(_ response: MongResponse.PoopClean) -> Mong {
        var next = self
        next.mongId = response.mongId
        next.expRatio = response.expRatio
        next.poopCount = response.poopCount
        next.createdAt = response.createdAt
        next.updatedAt = response.updatedAt
        return next
    }

    /// Android `Mong.evolution(...)`
    ///
    /// 진화는 `mongCode` 가 바뀐다 — 스프라이트가 통째로 달라지는 지점이다.
    public func applying(_ response: MongResponse.Evolution) -> Mong {
        var next = self
        next.mongCode = response.mongCode
        next.level = response.level
        next.expRatio = response.expRatio
        next.strengthRatio = response.strengthRatio
        next.healthyRatio = response.healthyRatio
        next.satietyRatio = response.satietyRatio
        next.fatigueRatio = response.fatigueRatio
        next.stateCode = response.stateCode
        next.statusCode = response.statusCode
        next.createdAt = response.createdAt
        next.updatedAt = response.updatedAt
        return next
    }
}

/// 몽 생성 요청
///
/// Android `CreateMongRequestDto` 이식.
public struct CreateMongRequest: Encodable, Sendable {
    public let name: String
    public let sleepAt: Date
    public let wakeupAt: Date

    public init(name: String, sleepAt: Date, wakeupAt: Date) {
        self.name = name
        self.sleepAt = sleepAt
        self.wakeupAt = wakeupAt
    }
}
