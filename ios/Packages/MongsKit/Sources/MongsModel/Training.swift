import Foundation

/// 훈련 종류
///
/// Android `domain/mong-domain/.../TrainingType.kt` + `GetTrainingResponseDto` 이식.
/// 값은 전부 서버가 준다 — 보상도 소모도 클라이언트가 정하지 않는다.
public struct TrainingType: Decodable, Sendable, Equatable, Identifiable {

    public var id: String { trainingCode }

    public let trainingTypeId: Int64
    public let trainingCode: String
    public let trainingName: String
    /// 참가비
    public let payPoint: Int
    /// 보상을 받는 기준 점수
    public let score: Int
    /// 제한 시간(초). **0 이면 무제한**이다 — 달리기가 그렇다(죽을 때까지).
    public let timeout: Int
    public let exp: Double
    public let strength: Double
    public let weight: Double
    public let satiety: Double
    public let fatigue: Double

    /// 어느 미니게임인지. 서버 코드로 가른다.
    ///
    /// 원본에는 축구(`TR003`)와 참참참(`TR004`)도 enum 에 있지만
    /// **화면이 `// TODO: 플레이 섹션` 스텁**이고 서버 목록에도 없다. 그래서 옮기지 않았다.
    public var game: Game? { Game(rawValue: trainingCode) }

    public enum Game: String, Sendable, Equatable {
        case runner = "TR000"
        case basketball = "TR001"
        case rockPaperScissors = "TR002"
    }
}

/// 훈련 결과
///
/// Android `TrainingEndResponseDto` 이식.
public struct TrainingResult: Decodable, Sendable, Equatable {
    public let mongId: Int64
    public let isSuccess: Bool
    public let rewardPayPoint: Int
    public let score: Int
    public let payPoint: Int
    public let expRatio: Double
    public let strengthRatio: Double
    public let healthyRatio: Double
    public let satietyRatio: Double
    public let fatigueRatio: Double
    public let weight: Double
    public let stateCode: MongStateCode
}

extension Mong {

    /// 훈련이 끝나면 스탯이 통째로 갱신된다.
    /// `statusCode` 는 응답에 없으므로 기존 값을 유지한다.
    public func applying(_ result: TrainingResult) -> Mong {
        var next = self
        next.payPoint = result.payPoint
        next.expRatio = result.expRatio
        next.strengthRatio = result.strengthRatio
        next.healthyRatio = result.healthyRatio
        next.satietyRatio = result.satietyRatio
        next.fatigueRatio = result.fatigueRatio
        next.weight = result.weight
        next.stateCode = result.stateCode
        return next
    }
}

/// 가위바위보 손
///
/// Android `TrainingRockPaperScissorsViewModel` 안에 인라인으로 있던 것을 꺼냈다 —
/// 승패 규칙은 화면이 아니라 도메인이고, 그래야 테스트할 수 있다.
public enum RockPaperScissors: String, Sendable, CaseIterable, Identifiable {
    case rock, paper, scissors

    public var id: String { rawValue }

    public var title: String {
        switch self {
        case .rock: "바위"
        case .paper: "보"
        case .scissors: "가위"
        }
    }

    public enum Outcome: Sendable, Equatable {
        case win, lose, draw
    }

    /// 상대 손에 대한 결과.
    public func result(against other: RockPaperScissors) -> Outcome {
        if self == other { return .draw }
        return switch (self, other) {
        case (.rock, .scissors), (.paper, .rock), (.scissors, .paper): .win
        default: .lose
        }
    }

    public static func random() -> RockPaperScissors {
        allCases.randomElement() ?? .rock
    }
}
