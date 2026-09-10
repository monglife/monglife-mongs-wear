import MongsModel
import SwiftUI

/// 몽이 든 슬롯
///
/// Android `component/pages/slotPick/Slot.kt` 이식.
/// 세로 비중 0.2(이름) / 0.52(몽) / 0.28(버튼) 이고 위아래에 15·5 여백이 붙는다.
struct OccupiedSlotView: View {

    let mong: Mong
    /// 지금 메인이 보고 있는 몽. 같으면 "선택" 버튼을 잠근다.
    let currentMongId: Int64?
    let onDetail: () -> Void
    let onGraduate: () -> Void
    let onDelete: () -> Void
    let onPick: () -> Void

    @Environment(SpriteLoader.self) private var loader

    /// 죽거나 삭제·졸업한 몽은 선택할 수 없다.
    private var canPick: Bool {
        ![.dead, .delete, .graduate].contains(mong.stateCode)
    }

    var body: some View {
        SlotCardLayout(
            header: {
                Text(mong.name)
                    .mongsFont(14)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)
            },
            content: {
                if mong.stateCode == .dead {
                    AnimatedSprite(sprite: loader.sprite(named: "mong_rip"))
                        .frame(width: 130.ms, height: 130.ms)
                        .padding(.bottom, 25.ms)
                } else {
                    // 슬롯에서는 몽을 **정적 이미지**로 작게 그린다 (원본 `isPng = true`, `ratio = 0.65`).
                    MongView(code: mong.resource, bodySize: 120 * 0.65, isAnimated: false)
                        .onTapGesture(perform: onDetail)
                }
            },
            footer: {
                HStack(spacing: 5.ms) {
                    if mong.canGraduate {
                        MongsButton(title: "졸업", style: .blue, width: 55, height: 32, action: onGraduate)
                    } else {
                        MongsButton(title: "삭제", style: .blue, width: 55, height: 32, action: onDelete)
                    }
                    if canPick {
                        MongsButton(
                            title: "선택", style: .blue, width: 55, height: 32,
                            isEnabled: mong.mongId != currentMongId,
                            action: onPick
                        )
                    }
                }
            }
        )
    }
}

/// 빈 슬롯
///
/// Android `component/pages/slotPick/EmptySlot.kt` 이식.
struct EmptySlotView: View {

    let onCreate: () -> Void

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        SlotCardLayout(
            header: {
                Text("NEW MONG")
                    .mongsFont(18)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)
            },
            content: {
                ZStack {
                    AnimatedSprite(sprite: loader.sprite(named: "mong_body_blind"))
                        .frame(width: 79.ms, height: 79.ms)
                    Text("?")
                        .mongsFont(25)
                        .foregroundStyle(MongsColor.white)
                }
            },
            footer: {
                MongsButton(title: "생성", style: .blue, width: 75, height: 33, action: onCreate)
            }
        )
    }
}

/// 슬롯 구매 칸
///
/// Android `component/pages/slotPick/BuySlot.kt` 이식.
/// 몽 자리에 그림자와 가격을 그린다.
struct PurchasableSlotView: View {

    let starPoint: Int
    let onBuy: () -> Void

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        SlotCardLayout(
            header: { StarPointBox(starPoint: starPoint) },
            content: {
                ZStack {
                    AnimatedSprite(sprite: loader.sprite(named: "mong_shadow"), contentMode: nil)
                        .frame(width: 80.ms, height: 20.ms)
                        .offset(y: 23.ms)

                    HStack(spacing: 6.ms) {
                        AnimatedSprite(sprite: loader.sprite(named: "point_icon_star"))
                            .frame(width: 26.ms, height: 26.ms)
                        Text("-")
                        Text("\(Slot.purchasePrice)")
                    }
                    .mongsFont(18)
                    .foregroundStyle(MongsColor.white)
                }
            },
            footer: {
                MongsButton(
                    title: "슬롯구매", width: 85, height: 36,
                    isEnabled: starPoint >= Slot.purchasePrice,
                    action: onBuy
                )
            }
        )
    }
}

/// 슬롯 카드 공통 레이아웃
///
/// 세 카드가 같은 비중을 쓴다 — 위 여백 15, 0.2 / 0.52 / 0.28, 아래 여백 5.
/// Compose `weight` 처럼 고정 여백을 뺀 남은 공간에 비율을 건다.
private struct SlotCardLayout<Header: View, Content: View, Footer: View>: View {

    @ViewBuilder let header: () -> Header
    /// `body` 라는 이름은 `View.body` 와 부딪힌다.
    @ViewBuilder let content: () -> Content
    @ViewBuilder let footer: () -> Footer

    var body: some View {
        GeometryReader { geometry in
            let available = geometry.size.height - 20

            VStack(spacing: 0) {
                Spacer().frame(height: 15.ms)
                ZStack { header() }.frame(height: available * 0.2)
                ZStack { content() }.frame(height: available * 0.52)
                ZStack { footer() }.frame(height: available * 0.28)
                Spacer().frame(height: 5.ms)
            }
            .frame(maxWidth: .infinity)
        }
    }
}
