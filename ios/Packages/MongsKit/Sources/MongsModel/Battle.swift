import Foundation

/// 매치에서 고를 수 있는 행동
///
/// Android `domain/battle-domain/.../MatchPickCode.kt` 이식.
public enum MatchPickCode: String, Sendable, CaseIterable, Codable, Identifiable {
    case attack = "MATCH_PICK_ATTACK"
    case defence = "MATCH_PICK_DEFENCE"
    case heal = "MATCH_PICK_HEAL"

    public var id: String { rawValue }

    public var title: String {
        switch self {
        case .attack: "공격"
        case .defence: "방어"
        case .heal: "회복"
        }
    }
}

/// 라운드 결과로 각 플레이어에게 일어난 일
///
/// Android `MatchRoundCode.kt` 이식.
public enum MatchRoundCode: String, Sendable, Codable {
    case none = "NONE"
    case defence = "MATCH_DEFENCE"
    case attacked = "MATCH_ATTACKED"
    case heal = "MATCH_HEAL"
    case attackedHeal = "MATCH_ATTACKED_HEAL"

    public var message: String {
        switch self {
        case .none: ""
        case .defence: "방어"
        case .attacked: "피해"
        case .heal: "회복"
        case .attackedHeal: "피해 & 회복"
        }
    }
}

/// 진행 중인 매치
///
/// Android `MatchEventDto` + `MatchVo` 이식. MQTT 로 라운드마다 통째로 밀려 들어온다.
public struct Match: Decodable, Sendable, Equatable {

    public struct Player: Decodable, Sendable, Equatable, Identifiable {
        public var id: String { playerId }

        public let playerId: String
        public let deviceId: String
        public let mongId: Int64
        public let mongCode: String
        public let mongName: String
        public let name: String
        public let hp: Double
        public let roundCode: MatchRoundCode

        public var resource: MongResourceCode { .resolve(mongCode) }
    }

    public let matchId: Int64
    public let round: Int
    public let isLastRound: Bool
    public let matchPlayers: [Player]

    /// 내 플레이어. `deviceId` 로 가른다 — 서버가 누가 나인지 따로 알려주지 않는다.
    public func me(deviceId: String) -> Player? {
        matchPlayers.first { $0.deviceId == deviceId }
    }

    public func opponent(deviceId: String) -> Player? {
        matchPlayers.first { $0.deviceId != deviceId }
    }
}

/// 매칭 대기열에서 상대를 찾았을 때 오는 것
///
/// Android `MatchQueueEventDto` 이식.
public struct MatchQueue: Decodable, Sendable, Equatable, Identifiable {

    public var id: Int64 { matchId }

    public struct Player: Decodable, Sendable, Equatable, Identifiable {
        public var id: String { playerId }

        public let playerId: String
        public let deviceId: String
        public let mongCode: String
        public let mongName: String
        public let name: String
    }

    public let matchId: Int64
    public let matchPlayers: [Player]

    public func me(deviceId: String) -> Player? {
        matchPlayers.first { $0.deviceId == deviceId }
    }
}

/// 배틀 보상 정보. Android `GetMatchOutcomeResponseDto`.
public struct MatchOutcome: Decodable, Sendable, Equatable {
    public let rewardPayPoint: Int
    public let battingPayPoint: Int
}

/// 매치 승자. Android `GetOverMatchResponseDto`.
public struct MatchWinner: Decodable, Sendable, Equatable {
    public let playerId: String
    public let mongCode: String
    public let mongName: String
    public let name: String
    public let rewardPayPoint: Int

    public var resource: MongResourceCode { .resolve(mongCode) }
}
