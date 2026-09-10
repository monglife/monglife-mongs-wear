import MongsModel
import SwiftUI

/// 상호작용 쪽
///
/// Android `.../pages/main/InteractionContent.kt` 이식.
/// 3줄 배치(2개 / 3개 / 2개)와 테두리 색, 아이콘 크기까지 원본 그대로다.
///
/// 환전과 슬롯 관리만 실제 화면으로 간다. 나머지는 v1 범위 밖이라
/// `NotReadyView` 자리표시자로 보낸다 — 원본 모바일 앱과 같은 방식이다.
/// 잠금 조건은 원본과 같게 맞춰 뒀다.
struct InteractionContentView: View {

    let mong: Mong?
    /// 슬롯 관리 화면 열기
    let onOpenSlotPick: () -> Void
    /// 환전 메뉴 열기
    let onOpenExchange: () -> Void
    /// 랜덤 뽑기 열기
    let onOpenRandomDraw: () -> Void
    /// 도감 메뉴 열기
    let onOpenCollection: () -> Void
    /// 맵 탐색 열기
    let onOpenMapSearch: () -> Void
    /// 훈련 열기
    let onOpenTraining: () -> Void
    /// 배틀 열기
    let onOpenBattle: () -> Void
    /// 아직 이식하지 않은 화면 열기 (자리표시자)
    let onNotReady: (NotReadyDestination) -> Void

    @Environment(SpriteLoader.self) private var loader

    private var sprites: [String] {
        ["btn_bg_circle",
         "btn_icon_collection", "point_icon_pay", "btn_icon_map_search", "btn_icon_slot_pick",
         "btn_icon_luck_draw", "btn_icon_activity", "btn_icon_battle", "btn_icon_locker",
         "btn_border_orange", "btn_border_purple_dark", "btn_border_blue", "btn_border_red",
         "btn_border_purple", "btn_border_green", "btn_border_pink"]
    }

    /// 죽었거나 삭제된 몽은 대부분의 기능이 잠긴다.
    private var isAlive: Bool { mong?.isInteractable ?? false }

    /// 훈련·배틀은 알(level 0)이거나 자고 있으면 추가로 잠긴다.
    private var canPlay: Bool {
        guard let mong else { return false }
        return mong.level > 0 && mong.stateCode != .dead && !mong.isSleep
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 8.ms) {
                MongsCircleButton(iconName: "btn_icon_collection", borderName: "btn_border_orange", iconSize: 34,
                                  action: onOpenCollection)
                MongsCircleButton(iconName: "point_icon_pay", borderName: "btn_border_purple_dark",
                                  isEnabled: isAlive, action: onOpenExchange)
            }
            HStack(spacing: 8.ms) {
                MongsCircleButton(iconName: "btn_icon_map_search", borderName: "btn_border_blue", iconSize: 34,
                                  isEnabled: isAlive, action: onOpenMapSearch)
                MongsCircleButton(iconName: "btn_icon_slot_pick", borderName: "btn_border_red", iconSize: 34,
                                  action: onOpenSlotPick)
                MongsCircleButton(iconName: "btn_icon_luck_draw", borderName: "btn_border_purple", iconSize: 34,
                                  isEnabled: isAlive, action: onOpenRandomDraw)
            }
            HStack(spacing: 8.ms) {
                MongsCircleButton(iconName: "btn_icon_activity", borderName: "btn_border_green", iconSize: 34,
                                  isEnabled: canPlay, action: onOpenTraining)
                MongsCircleButton(iconName: "btn_icon_battle", borderName: "btn_border_pink", iconSize: 30,
                                  isEnabled: canPlay, action: onOpenBattle)
            }
        }
        // 원본은 fillMaxSize + Center 다. 명시하지 않으면 콘텐츠가 위로 몰린다.
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .task { await loader.preload(sprites) }
    }
}
