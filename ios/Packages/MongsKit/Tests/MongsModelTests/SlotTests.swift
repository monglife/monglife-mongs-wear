import Foundation
import Testing
@testable import MongsModel

/// 슬롯 구성 규칙 테스트
///
/// Android `SlotPickViewModel.updateSlotVos()` 의 규칙을 고정한다 —
/// 몽 칸 → 남은 빈 칸 → (최대치 미만이면) 구매 칸.
@Suite("슬롯 구성")
struct SlotTests {

    private func makeMong(_ id: Int64) -> Mong {
        Mong(
            mongId: id, name: "몽\(id)", mongCode: "CH100", mongName: "몽",
            stateCode: .normal, statusCode: .normal, level: 1,
            sleepAt: Date(timeIntervalSince1970: 0), wakeupAt: Date(timeIntervalSince1970: 0),
            payPoint: 0, isSleep: false, strengthRatio: 0, healthyRatio: 0,
            satietyRatio: 0, fatigueRatio: 0, expRatio: 0, weight: 0,
            poopCount: 0, randomDrawTicketCount: 0,
            createdAt: Date(timeIntervalSince1970: 0), updatedAt: Date(timeIntervalSince1970: 0)
        )
    }

    @Test("슬롯 1칸에 몽이 없으면 빈 칸 하나 + 구매 칸")
    func emptyAccount() {
        let slots = Slot.build(mongs: [], slotCount: 1)

        #expect(slots.count == 2)
        #expect(slots[0].kind == .empty)
        #expect(slots[1].kind == .purchasable)
    }

    @Test("몽이 슬롯을 채우면 빈 칸이 없다")
    func fullSlots() {
        let slots = Slot.build(mongs: [makeMong(1)], slotCount: 1)

        #expect(slots.count == 2)
        #expect(slots[0].mong?.mongId == 1)
        #expect(slots[1].kind == .purchasable)
    }

    @Test("최대치를 채우면 구매 칸이 붙지 않는다")
    func noPurchaseSlotAtMax() {
        // 원본이 `slotVos.size < 3` 으로 구매 칸을 붙인다.
        let slots = Slot.build(mongs: [makeMong(1), makeMong(2), makeMong(3)], slotCount: 3)

        #expect(slots.count == 3)
        #expect(!slots.contains { $0.kind == .purchasable })
    }

    @Test("몽이 슬롯 수보다 많아도 빈 칸을 음수로 만들지 않는다")
    func moreMongsThanSlots() {
        // 슬롯을 산 뒤 서버 상태가 어긋나는 경우를 방어한다.
        let slots = Slot.build(mongs: [makeMong(1), makeMong(2)], slotCount: 1)

        #expect(slots.count == 3)   // 몽 2 + 구매 1
        #expect(!slots.contains { $0.kind == .empty })
    }

    @Test("죽거나 졸업한 몽도 칸을 차지한다")
    func deadMongOccupiesSlot() {
        // 슬롯 화면이 그 상태를 보여주고 삭제/졸업 버튼을 띄워야 한다.
        var dead = makeMong(1)
        dead.stateCode = .dead

        let slots = Slot.build(mongs: [dead], slotCount: 1)

        #expect(slots[0].mong?.stateCode == .dead)
    }
}
