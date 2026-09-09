import MongsModel
import MongsViewModel
import SwiftUI

/// 인벤토리
///
/// Android `pages/inventory/InventoryView.kt` 이식.
///
/// 화면 가운데에 110×130 회색 상자를 놓고, 위쪽에 "인벤토리" 탭을 붙여 서랍처럼 보이게 한다.
/// 상자 안은 2×2 격자로 한 페이지에 4칸이고, 페이지 넘김은 **서버 페이징**이다.
struct InventoryView: View {

    /// 원본 상수 (`INVENTORY_*`)
    private static let boxWidth: CGFloat = 110
    private static let boxHeight: CGFloat = 130
    private static let barWidth: CGFloat = 62
    private static let barHeight: CGFloat = 30
    private static let corner: CGFloat = 10
    /// Compose 의 `Color.LightGray` — 본체(`MongsLightGray` #F0F0F0)보다 짙다.
    private static let backingGray = Color(hex: 0xCCCCCC)

    @State private var viewModel: InventoryViewModel
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader

    init(viewModel: InventoryViewModel, onClose: @escaping () -> Void) {
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

                if let pending = viewModel.pending {
                    ConfirmDialogView(
                        message: "\(pending.inventoryName)\n사용하시겠습니까?",
                        onConfirm: { Task { await viewModel.use() } },
                        onCancel: viewModel.cancelConfirm
                    )
                }
            }
        }
        .task {
            await loader.preload(["btn_icon_left", "btn_icon_right"])
            await viewModel.load()
            await loader.preload(viewModel.items.compactMap(FeedResourceCode.pngName(for:)))
        }
        .onChange(of: viewModel.items) { _, items in
            // 페이지를 넘기면 새 아이콘이 필요하다.
            Task { await loader.preload(items.compactMap(FeedResourceCode.pngName(for:))) }
        }
        .onChange(of: viewModel.isFinished) { _, finished in
            if finished { onClose() }
        }
    }

    private var content: some View {
        ZStack {
            // 페이지 인디케이터 — 화면 맨 아래 5dp 위
            if viewModel.totalPage > 0 {
                VStack {
                    Spacer()
                    PageIndicator(pageCount: viewModel.totalPage, currentPage: viewModel.page - 1)
                        .padding(.bottom, 5)
                }

                SelectButton(
                    canGoPrevious: !viewModel.isFirstPage,
                    canGoNext: !viewModel.isLastPage,
                    onPrevious: { Task { await viewModel.previousPage() } },
                    onNext: { Task { await viewModel.nextPage() } }
                )
            }

            drawer
        }
    }

    /// 서랍
    ///
    /// 원본은 세 겹을 겹쳐 놓는다:
    /// 1. 110×130 짙은 회색(Compose `Color.LightGray` = #CCCCCC) 뒷판 — 네 귀퉁이 모두 둥글다
    /// 2. 그 위 왼쪽 상단에 62×30 탭 (`MongsLightGray` = #F0F0F0, 위쪽만 둥글다)
    /// 3. 아래 110×100 본체 (같은 밝은 회색, 아래쪽만 둥글다)
    ///
    /// 뒷판이 탭 오른쪽 48×30 만큼 드러나면서 서랍 손잡이처럼 보인다.
    /// 탭을 가운데 놓거나 뒷판을 빼면 그 모양이 사라진다 — 원본 `Row` 는
    /// `horizontalArrangement` 를 주지 않아 기본값 **Start** 다.
    private var drawer: some View {
        ZStack(alignment: .topLeading) {
            RoundedRectangle(cornerRadius: Self.corner)
                .fill(Self.backingGray)
                .frame(width: Self.boxWidth, height: Self.boxHeight)

            // 본체 — 아래쪽 정렬
            grid
                .frame(width: Self.boxWidth, height: Self.boxHeight - Self.barHeight)
                .background(
                    UnevenRoundedRectangle(
                        topLeadingRadius: 0, bottomLeadingRadius: Self.corner,
                        bottomTrailingRadius: Self.corner, topTrailingRadius: 0
                    )
                    .fill(MongsColor.lightGray)
                )
                .offset(y: Self.barHeight)

            // 탭 — 왼쪽 상단, 4dp 내려서 본체와 겹친다
            Text("인벤토리")
                .mongsFont(12)
                .foregroundStyle(.black)
                .frame(width: Self.barWidth, height: Self.barHeight)
                .background(
                    UnevenRoundedRectangle(
                        topLeadingRadius: Self.corner, bottomLeadingRadius: 0,
                        bottomTrailingRadius: 0, topTrailingRadius: Self.corner
                    )
                    .fill(MongsColor.lightGray)
                )
                .offset(y: 4)
        }
        .frame(width: Self.boxWidth, height: Self.boxHeight)
    }

    /// 2×2 격자. 빈 칸도 자리를 지킨다.
    private var grid: some View {
        let slots = viewModel.slots
        return VStack(spacing: 0) {
            HStack(spacing: 0) {
                slot(slots[0])
                slot(slots[1])
            }
            .frame(maxHeight: .infinity)

            HStack(spacing: 0) {
                slot(slots[2])
                slot(slots[3])
            }
            .frame(maxHeight: .infinity)
        }
        .padding(6)
    }

    /// 한 칸 — 40×40 둥근 회색 바탕에 28×28 아이콘.
    /// 종류를 모르면(맵 등) 아이콘 대신 "?" 를 띄운다.
    @ViewBuilder
    private func slot(_ item: InventoryItem?) -> some View {
        Button(action: { if let item { viewModel.askConfirm(item) } }) {
            ZStack {
                RoundedRectangle(cornerRadius: 10)
                    .fill(Self.backingGray)
                    .frame(width: 40, height: 40)

                if let name = item.flatMap(FeedResourceCode.pngName(for:)) {
                    AnimatedSprite(sprite: loader.sprite(named: name))
                        .frame(width: 28, height: 28)
                } else if item != nil {
                    Text("?")
                        .mongsFont(14)
                        .foregroundStyle(MongsColor.white)
                }
            }
            .frame(width: 40, height: 40)
        }
        .buttonStyle(.plain)
        .padding(2)
        .disabled(item == nil)
    }
}
