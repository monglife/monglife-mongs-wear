import Foundation
import Testing
@testable import MongsModel

/// 몽 모델 테스트
///
/// Android `Mong.kt` 는 **테스트가 하나도 없다.** 상태변경 메서드 9개가 전부 setter 라
/// 로직 자체는 없지만, "어떤 응답이 어떤 필드를 건드리는가"는 여전히 틀릴 수 있는 부분이라
/// 여기서 고정한다.
@Suite("몽 모델")
struct MongTests {

    private func makeMong(
        mongCode: String = "CH100",
        stateCode: MongStateCode = .normal,
        statusCode: MongStatusCode = .normal,
        isSleep: Bool = false,
        expRatio: Double = 10,
        poopCount: Int = 2,
        weight: Double = 5
    ) -> Mong {
        Mong(
            mongId: 1, name: "몽이", mongCode: mongCode, mongName: "몽", 
            stateCode: stateCode, statusCode: statusCode, level: 2,
            sleepAt: Date(timeIntervalSince1970: 0), wakeupAt: Date(timeIntervalSince1970: 0),
            payPoint: 300, isSleep: isSleep,
            strengthRatio: 50, healthyRatio: 60, satietyRatio: 70, fatigueRatio: 30,
            expRatio: expRatio, weight: weight, poopCount: poopCount, randomDrawTicketCount: 1,
            createdAt: Date(timeIntervalSince1970: 0), updatedAt: Date(timeIntervalSince1970: 0)
        )
    }

    private func decode<T: Decodable>(_ type: T.Type, _ json: String) throws -> T {
        try JSONDecoder.mongs().decode(T.self, from: Data(json.utf8))
    }

    // MARK: - 부분 병합

    @Test("쓰다듬기는 경험치만 바꾸고 나머지는 건드리지 않는다")
    func strokeMergesOnlyExp() throws {
        let mong = makeMong(expRatio: 10, poopCount: 2, weight: 5)
        let response = try decode(MongResponse.Stroke.self, """
        {"mongId":1,"expRatio":12.5,"createdAt":"2026-03-04T05:06:07","updatedAt":"2026-03-04T05:06:08"}
        """)

        let next = mong.applying(response)

        #expect(next.expRatio == 12.5)
        // 응답에 없는 필드는 그대로여야 한다
        #expect(next.poopCount == 2)
        #expect(next.weight == 5)
        #expect(next.satietyRatio == 70)
        #expect(next.name == "몽이")
    }

    @Test("배변 처리는 경험치와 똥 개수를 바꾼다")
    func poopCleanMergesExpAndCount() throws {
        let mong = makeMong(expRatio: 10, poopCount: 3)
        let response = try decode(MongResponse.PoopClean.self, """
        {"mongId":1,"expRatio":11,"poopCount":0,"createdAt":"2026-03-04T05:06:07","updatedAt":"2026-03-04T05:06:08"}
        """)

        let next = mong.applying(response)

        #expect(next.poopCount == 0)
        #expect(next.expRatio == 11)
        #expect(next.weight == 5)
    }

    @Test("수면 토글은 isSleep 만 바꾼다")
    func sleepMergesOnlyFlag() throws {
        let mong = makeMong(isSleep: false, expRatio: 10)
        let response = try decode(MongResponse.Sleep.self, """
        {"mongId":1,"isSleep":true,"createdAt":"2026-03-04T05:06:07","updatedAt":"2026-03-04T05:06:08"}
        """)

        let next = mong.applying(response)

        #expect(next.isSleep)
        #expect(next.expRatio == 10)
    }

    @Test("진화는 몽 코드를 바꾼다 — 스프라이트가 통째로 달라지는 지점")
    func evolutionChangesResource() throws {
        let mong = makeMong(mongCode: "CH100", stateCode: .evolutionReady)
        let response = try decode(MongResponse.Evolution.self, """
        {"mongId":1,"mongCode":"CH200","level":3,"expRatio":0,
         "strengthRatio":10,"healthyRatio":10,"satietyRatio":10,"fatigueRatio":10,
         "stateCode":"NORMAL","statusCode":"NORMAL",
         "createdAt":"2026-03-04T05:06:07","updatedAt":"2026-03-04T05:06:08"}
        """)

        let next = mong.applying(response)

        #expect(next.mongCode == "CH200")
        #expect(next.resource == .ch200)
        #expect(next.level == 3)
        #expect(next.stateCode == .normal)
        // 진화 응답에 없는 필드는 유지된다
        #expect(next.name == "몽이")
        #expect(next.weight == 5)
    }

    // MARK: - 화면이 묻는 것

    @Test("아픈/졸린/배고픈 상태가 표정보다 우선한다")
    func statusOverridesExpression() {
        // 아픈 몽은 밥을 먹어도 아픈 표정이다.
        #expect(makeMong(statusCode: .sick).expression(isEating: true) == .sad)
        #expect(makeMong(statusCode: .somnolence).expression(isHappy: true) == .depressed)
        #expect(makeMong(statusCode: .hungry).expression() == .sulky)
    }

    @Test("정상 컨디션에서는 상황에 따라 표정이 갈린다")
    func normalStatusExpressions() {
        #expect(makeMong(isSleep: true).expression() == .sleeping)
        #expect(makeMong().expression(isEating: true) == .eating)
        #expect(makeMong().expression(isHappy: true) == .happy)
        #expect(makeMong().expression() == .smile)
    }

    @Test("정상 컨디션 안에서는 기쁨 > 먹는 중 > 자는 중 순이다")
    func normalStatusExpressionPriority() {
        // 자는 몽을 쓰다듬으면 기쁜 표정이 나온다 — 자는 표정이 먼저면 반응이 묻힌다.
        #expect(makeMong(isSleep: true).expression(isHappy: true) == .happy)
        #expect(makeMong(isSleep: true).expression(isEating: true) == .eating)
        #expect(makeMong().expression(isEating: true, isHappy: true) == .happy)
    }

    @Test("죽었거나 삭제된 몽은 상호작용할 수 없다")
    func deadMongIsNotInteractable() {
        // Android InteractionContent 가 DEAD/DELETE 에서 버튼을 전부 잠근다.
        #expect(!makeMong(stateCode: .dead).isInteractable)
        #expect(!makeMong(stateCode: .delete).isInteractable)
        #expect(makeMong(stateCode: .normal).isInteractable)
        #expect(makeMong(stateCode: .evolutionReady).isInteractable)
    }

    @Test("진화/졸업 가능 여부는 상태 코드로 판단한다")
    func readyStates() {
        #expect(makeMong(stateCode: .evolutionReady).canEvolve)
        #expect(!makeMong(stateCode: .normal).canEvolve)
        #expect(makeMong(stateCode: .graduateReady).canGraduate)
        #expect(!makeMong(stateCode: .normal).canGraduate)
    }

    @Test("모르는 몽 코드는 기본 스프라이트로 떨어진다")
    func unknownCodeFallsBack() {
        // 서버가 새 캐릭터를 내려도 앱이 죽지 않아야 한다.
        #expect(makeMong(mongCode: "CH999").resource == .ch444)
    }

    @Test("전체 응답을 몽으로 옮긴다")
    func decodesFullResponse() throws {
        let response = try decode(MongResponse.Full.self, """
        {"mongId":7,"name":"뭉치","mongCode":"CH300","mongName":"몽","stateCode":"NORMAL",
         "statusCode":"HUNGRY","level":1,"sleepAt":"22:00:00","wakeupAt":"07:00:00",
         "payPoint":150,"isSleep":false,"strengthRatio":10,"healthyRatio":20,
         "satietyRatio":30,"fatigueRatio":40,"expRatio":50,"weight":3.5,
         "poopCount":1,"randomDrawTicketCount":0,
         "createdAt":"2026-03-04T05:06:07.123","updatedAt":"2026-03-04T05:06:08"}
        """)

        let mong = response.mong

        #expect(mong.mongId == 7)
        #expect(mong.name == "뭉치")
        #expect(mong.statusCode == .hungry)
        #expect(mong.resource == .ch300)
        #expect(mong.weight == 3.5)
    }
}
