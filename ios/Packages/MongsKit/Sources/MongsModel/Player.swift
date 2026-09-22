import Foundation

/// 플레이어 (계정 단위 자원)
///
/// Android `domain/member-domain/.../player/model/Player.kt` +
/// `GetPlayerResponseDto` 이식.
public struct Player: Sendable, Equatable, Codable {

    public let accountId: Int64
    /// 보유 슬롯 수. 몽을 담을 수 있는 칸 수다.
    public let slotCount: Int
    /// 별가루. 슬롯 구매에 쓴다.
    public let starPoint: Int

    public init(accountId: Int64, slotCount: Int, starPoint: Int) {
        self.accountId = accountId
        self.slotCount = slotCount
        self.starPoint = starPoint
    }
}

/// 슬롯 한 칸
///
/// Android `presentation/viewmodel-presentation/.../slotPick/vo/SlotVo.kt` 이식.
public struct Slot: Sendable, Equatable, Identifiable {

    public enum Kind: Sendable, Equatable {
        /// 몽이 들어 있는 칸
        case occupied(Mong)
        /// 비어 있는 칸 — 새 몽을 만들 수 있다
        case empty
        /// 슬롯을 더 살 수 있는 칸
        case purchasable
    }

    public let id: Int
    public let kind: Kind

    public var mong: Mong? {
        if case let .occupied(mong) = kind { return mong }
        return nil
    }
}

extension Slot {

    /// 슬롯 하나를 사는 데 드는 별가루.
    /// Android `SlotPickView` 가 `buySlotPrice = 10` 을 그대로 넘긴다.
    public static let purchasePrice = 10

    /// 살 수 있는 슬롯의 최대 개수. 원본이 `slotVos.size < 3` 으로 구매 칸을 붙인다.
    public static let maxSlotCount = 3

    /// 몽 목록과 플레이어 정보로 슬롯 목록을 만든다.
    ///
    /// Android `SlotPickViewModel.updateSlotVos()` 이식:
    /// 몽이 든 칸을 먼저 놓고, 남은 슬롯 수만큼 빈 칸을 채우고,
    /// 전체가 최대치 미만이면 마지막에 구매 칸을 하나 붙인다.
    public static func build(mongs: [Mong], slotCount: Int) -> [Slot] {
        var slots = mongs.enumerated().map { Slot(id: $0.offset, kind: .occupied($0.element)) }

        let emptyCount = max(slotCount - mongs.count, 0)
        for index in 0 ..< emptyCount {
            slots.append(Slot(id: mongs.count + index, kind: .empty))
        }

        if slots.count < maxSlotCount {
            slots.append(Slot(id: slots.count, kind: .purchasable))
        }
        return slots
    }
}
