import Testing
@testable import MongsModel

/// 가위바위보 규칙
///
/// 원본은 이 판정을 ViewModel 안에 인라인으로 두어 테스트할 수 없었다.
@Suite("가위바위보")
struct RockPaperScissorsTests {

    @Test("같은 손이면 비긴다", arguments: RockPaperScissors.allCases)
    func drawOnSameHand(hand: RockPaperScissors) {
        #expect(hand.result(against: hand) == .draw)
    }

    @Test("이기는 조합")
    func winningPairs() {
        #expect(RockPaperScissors.rock.result(against: .scissors) == .win)
        #expect(RockPaperScissors.paper.result(against: .rock) == .win)
        #expect(RockPaperScissors.scissors.result(against: .paper) == .win)
    }

    @Test("지는 조합")
    func losingPairs() {
        #expect(RockPaperScissors.scissors.result(against: .rock) == .lose)
        #expect(RockPaperScissors.rock.result(against: .paper) == .lose)
        #expect(RockPaperScissors.paper.result(against: .scissors) == .lose)
    }

    @Test("어느 손이든 이기는 상대와 지는 상대가 하나씩 있다", arguments: RockPaperScissors.allCases)
    func exactlyOneWinAndOneLoss(hand: RockPaperScissors) {
        let results = RockPaperScissors.allCases.map { hand.result(against: $0) }
        #expect(results.filter { $0 == .win }.count == 1)
        #expect(results.filter { $0 == .lose }.count == 1)
        #expect(results.filter { $0 == .draw }.count == 1)
    }
}
