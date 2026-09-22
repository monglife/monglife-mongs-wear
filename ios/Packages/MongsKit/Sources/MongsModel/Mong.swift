import Foundation

/// 몽 (펫)
///
/// Android `domain/mong-domain/.../model/Mong.kt` 이식.
///
/// 원본은 `private set` 프로퍼티 21개와 상태변경 메서드 9개(`stroke`, `feed`, `evolution` …)를
/// 가진 클래스지만, **그 메서드들에 게임 로직이 하나도 없다** — 전부 서버가 계산해 내려준 값을
/// 대입하는 setter 다. 서버 응답의 일부 필드만 갱신하는 "부분 병합"이 그 메서드들의 실체다.
///
/// 그래서 Swift 에서는 값 타입으로 두고, 각 API 응답이 자기가 아는 필드만 병합하도록 한다
/// (`applying(_:)`). 클래스 + private setter 를 흉내낼 이유가 없다.
public struct Mong: Sendable, Equatable, Codable, Identifiable {

    public var id: Int64 { mongId }

    public var mongId: Int64
    public var name: String
    /// 스프라이트 코드 (`CH100` 등). `MongResourceCode.resolve` 로 이미지에 매핑한다.
    public var mongCode: String
    public var mongName: String
    public var stateCode: MongStateCode
    public var statusCode: MongStatusCode
    public var level: Int
    public var sleepAt: Date
    public var wakeupAt: Date
    public var payPoint: Int
    public var isSleep: Bool
    public var strengthRatio: Double
    public var healthyRatio: Double
    public var satietyRatio: Double
    public var fatigueRatio: Double
    public var expRatio: Double
    public var weight: Double
    public var poopCount: Int
    public var randomDrawTicketCount: Int
    public var createdAt: Date
    public var updatedAt: Date

    public init(
        mongId: Int64,
        name: String,
        mongCode: String,
        mongName: String,
        stateCode: MongStateCode,
        statusCode: MongStatusCode,
        level: Int,
        sleepAt: Date,
        wakeupAt: Date,
        payPoint: Int,
        isSleep: Bool,
        strengthRatio: Double,
        healthyRatio: Double,
        satietyRatio: Double,
        fatigueRatio: Double,
        expRatio: Double,
        weight: Double,
        poopCount: Int,
        randomDrawTicketCount: Int,
        createdAt: Date,
        updatedAt: Date
    ) {
        self.mongId = mongId
        self.name = name
        self.mongCode = mongCode
        self.mongName = mongName
        self.stateCode = stateCode
        self.statusCode = statusCode
        self.level = level
        self.sleepAt = sleepAt
        self.wakeupAt = wakeupAt
        self.payPoint = payPoint
        self.isSleep = isSleep
        self.strengthRatio = strengthRatio
        self.healthyRatio = healthyRatio
        self.satietyRatio = satietyRatio
        self.fatigueRatio = fatigueRatio
        self.expRatio = expRatio
        self.weight = weight
        self.poopCount = poopCount
        self.randomDrawTicketCount = randomDrawTicketCount
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }

    // MARK: - 화면이 묻는 것들

    /// 스프라이트 리소스
    public var resource: MongResourceCode { .resolve(mongCode) }

    /// 표정. Android `component/common/charactor/Mong.kt` 의 분기 이식.
    ///
    /// 두 단계로 갈린다:
    /// 1. 컨디션(아픔/졸림/배고픔)이 **가장 우선**한다 — 아픈 몽은 밥을 먹어도 아픈 표정이다.
    /// 2. 정상 컨디션이면 기쁨 > 먹는 중 > 자는 중 > 기본 순서다.
    ///
    /// ⚠️ 순서가 중요하다. 자는 몽을 쓰다듬으면 **기쁜 표정**이 나온다 —
    /// 자는 표정을 먼저 보면 쓰다듬기 반응이 묻힌다.
    public func expression(isEating: Bool = false, isHappy: Bool = false) -> MongExpression {
        switch statusCode {
        case .sick: .sad
        case .somnolence: .depressed
        case .hungry: .sulky
        case .normal:
            if isHappy { .happy }
            else if isEating { .eating }
            else if isSleep { .sleeping }
            else { .smile }
        }
    }

    /// 상호작용을 받을 수 있는 상태인지
    ///
    /// Android `InteractionContent.kt` 가 DEAD/DELETE 에서 버튼을 전부 비활성화한다.
    public var isInteractable: Bool {
        stateCode != .dead && stateCode != .delete
    }

    /// 진화 가능
    public var canEvolve: Bool { stateCode == .evolutionReady }

    /// 졸업 가능
    public var canGraduate: Bool { stateCode == .graduateReady }
}

/// 몽 생애 상태
///
/// Android `domain/mong-domain/.../enums/MongStateCode.kt` 이식.
public enum MongStateCode: String, Sendable, Codable, CaseIterable {
    case normal = "NORMAL"
    case graduateReady = "GRADUATE_READY"
    case evolutionReady = "EVOLUTION_READY"
    case dead = "DEAD"
    case graduate = "GRADUATE"
    case delete = "DELETE"

    public var message: String {
        switch self {
        case .normal: "정상 상태"
        case .graduateReady: "졸업 대기 상태"
        case .evolutionReady: "진화 대기 상태"
        case .dead: "사망"
        case .graduate: "졸업"
        case .delete: "삭제"
        }
    }
}

/// 몽 컨디션
///
/// Android `domain/mong-domain/.../enums/MongStatusCode.kt` 이식.
public enum MongStatusCode: String, Sendable, Codable, CaseIterable {
    case normal = "NORMAL"
    case somnolence = "SOMNOLENCE"
    case hungry = "HUNGRY"
    case sick = "SICK"

    public var message: String {
        switch self {
        case .normal: "정상 컨디션"
        case .somnolence: "졸림"
        case .hungry: "배고픔"
        case .sick: "아픔"
        }
    }
}
