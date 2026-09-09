import MongsModel
import MongsViewModel
import SwiftUI

/// 먹이 메뉴
///
/// Android `pages/feed/FeedMenuView.kt` 이식.
/// 위아래 0.49 / 0.02 / 0.49 — 가운데 2dp 흰 줄이 두 칸을 가른다.
struct FeedMenuView: View {

    let onSelect: (FeedItem.Kind) -> Void

    var body: some View {
        ZStack {
            DefaultBackground()

            GeometryReader { geometry in
                let available = geometry.size.height

                VStack(spacing: 0) {
                    entry("밥") { onSelect(.food) }
                        .frame(height: available * 0.49)

                    // 0.02 밴드 안의 2dp 흰 줄
                    ZStack {
                        Rectangle()
                            .fill(Color.white)
                            .frame(height: 2)
                    }
                    .frame(height: available * 0.02)

                    entry("간식") { onSelect(.snack) }
                        .frame(height: available * 0.49)
                }
            }
        }
    }

    private func entry(_ title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .mongsFont(18)
                .foregroundStyle(MongsColor.white)
                .lineLimit(1)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                // 빈 영역도 탭이 먹어야 한다. 원본은 Box 전체에 clickable 이 걸려 있다.
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

/// 먹이주기 (밥/간식)
///
/// Android `pages/feed/FeedFoodView.kt` + `FeedSnackView.kt` 이식.
///
/// **목록이 아니라 캐러셀이다.** 세로 비중 0.2(페이포인트) / 0.52(이름+아이콘) / 0.28(가격 버튼),
/// 좌우 화살표로 종류를 넘기고 아래에 페이지 인디케이터가 붙는다.
struct FeedView: View {

    @State private var viewModel: FeedViewModel
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader

    init(viewModel: FeedViewModel, onClose: @escaping () -> Void) {
        _viewModel = State(initialValue: viewModel)
        self.onClose = onClose
    }

    var body: some View {
        ZStack {
            DefaultBackground()

            if viewModel.isLoading {
                LoadingBar()
            } else {
                content

                if let item = viewModel.current {
                    if viewModel.isDetailPresented {
                        FeedDetailDialogView(item: item, onClose: viewModel.closeDetail)
                    } else if viewModel.isConfirmPresented {
                        ConfirmDialogView(
                            message: "\(item.name)\n구매하시겠습니까?",
                            onConfirm: { Task { await viewModel.buy() } },
                            onCancel: viewModel.closeConfirm
                        )
                    }
                }
            }
        }
        .task {
            await loader.preload([
                "point_bg", "point_icon_pay",
                "btn_icon_left", "btn_icon_right",
                "btn_bg_yellow", "btn_bg_disable",
                "icon_healthy", "icon_satiety", "icon_strength", "icon_fatigue",
            ])
            await viewModel.load()
            // 목록을 받은 뒤에야 어떤 스프라이트가 필요한지 안다.
            await loader.preload(viewModel.items.map {
                FeedResourceCode.pngName(kind: $0.kind, code: $0.code)
            })
        }
        .onChange(of: viewModel.isFinished) { _, finished in
            if finished { onClose() }
        }
    }

    private var content: some View {
        GeometryReader { geometry in
            // Compose weight 는 고정 크기(위 15dp + 아래 5dp)를 뺀 남은 공간에 비율을 건다.
            let available = geometry.size.height - 20

            ZStack {
                VStack(spacing: 0) {
                    Spacer().frame(height: 15)

                    // 0.2 — 현재 페이포인트
                    ZStack { PayPointBox(payPoint: viewModel.payPoint) }
                        .frame(height: available * 0.2)

                    // 0.52 — 이름(0.3) + 아이콘(0.7). 아이콘을 누르면 상세가 열린다.
                    VStack(spacing: 0) {
                        Text(viewModel.current?.name ?? "")
                            .mongsFont(14)
                            .foregroundStyle(MongsColor.white)
                            .lineLimit(1)
                            .frame(height: available * 0.52 * 0.3)

                        Button(action: viewModel.openDetail) {
                            AnimatedSprite(sprite: loader.sprite(named: iconName))
                                .frame(width: 50, height: 50)
                        }
                        .buttonStyle(.plain)
                        .frame(height: available * 0.52 * 0.7)
                    }
                    .frame(height: available * 0.52)

                    // 0.28 — 가격 버튼. 원본은 밴드 위쪽에 붙는다(Row 기본 정렬 Top).
                    VStack(spacing: 0) {
                        MongsButton(
                            title: "$\(viewModel.current?.price ?? 0)",
                            style: .blue,
                            width: 70,
                            isEnabled: viewModel.canBuyCurrent,
                            action: viewModel.openConfirm
                        )
                        Spacer(minLength: 0)
                    }
                    .frame(height: available * 0.28)

                    Spacer().frame(height: 5)
                }
                .frame(maxWidth: .infinity)

                // 페이지 인디케이터 — 화면 맨 아래 5dp 위
                if !viewModel.items.isEmpty {
                    VStack {
                        Spacer()
                        PageIndicator(pageCount: viewModel.items.count, currentPage: viewModel.index)
                            .padding(.bottom, 5)
                    }
                }

                SelectButton(
                    canGoPrevious: !viewModel.isFirst,
                    canGoNext: !viewModel.isLast,
                    onPrevious: viewModel.previous,
                    onNext: viewModel.next
                )
            }
        }
    }

    private var iconName: String {
        guard let item = viewModel.current else { return "mong_none" }
        return FeedResourceCode.pngName(kind: item.kind, code: item.code)
    }
}

/// 먹이 상세 (스탯 변화량)
///
/// Android `dialog/pages/feed/FeedItemDetailDialog.kt` 이식.
/// 90% 검은 막 위에 0 보다 큰 항목만 순서대로(체력·포만감·힘·피로·무게) 나열한다.
/// **막 아무 곳이나 누르면 닫힌다.**
struct FeedDetailDialogView: View {

    let item: FeedItem
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader

    /// (아이콘 이름 또는 nil, 라벨, 값) — 무게만 아이콘 대신 "Kg" 글자를 쓴다.
    private var entries: [(icon: String?, label: String, value: Double)] {
        [
            ("icon_healthy", "", item.healthy),
            ("icon_satiety", "", item.satiety),
            ("icon_strength", "", item.strength),
            ("icon_fatigue", "", item.fatigue),
            (nil, "Kg", item.weight),
        ].filter { $0.2 > 0 }
    }

    var body: some View {
        ZStack {
            Color.black.opacity(0.9)
                .ignoresSafeArea()
                .onTapGesture(perform: onClose)

            VStack(spacing: 0) {
                ForEach(Array(entries.enumerated()), id: \.offset) { _, entry in
                    HStack(spacing: 0) {
                        ZStack {
                            if let icon = entry.icon {
                                // 원본이 ContentScale.FillBounds 라 비율을 무시하고 20×20 에 맞춘다.
                                AnimatedSprite(sprite: loader.sprite(named: icon), contentMode: nil)
                                    .frame(width: 20, height: 20)
                            } else {
                                Text(entry.label)
                                    .mongsFont(18)
                                    .foregroundStyle(MongsColor.white)
                                    .lineLimit(1)
                                    .frame(width: 20, height: 20)
                            }
                        }
                        .frame(width: 100 * 0.2)

                        Text("+ \(format(entry.value))")
                            .mongsFont(20)
                            .foregroundStyle(MongsColor.white)
                            .lineLimit(1)
                            .frame(width: 100 * 0.8)
                    }
                    .frame(width: 100, height: 34)
                }
            }
        }
    }

    /// Kotlin 의 `Double` 문자열화는 항상 소수점을 남긴다 (`3.0`). 그대로 맞춘다.
    private func format(_ value: Double) -> String {
        value == value.rounded() ? String(format: "%.1f", value) : "\(value)"
    }
}
