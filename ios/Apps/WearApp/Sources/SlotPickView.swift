import MongsModel
import MongsViewModel
import SwiftUI

/// 슬롯 관리 화면
///
/// Android `wear-view-presentation/.../view/pages/slotPick/SlotPickView.kt` 이식.
///
/// 슬롯을 한 칸씩 좌우로 넘기며 보고, 칸 종류에 따라 다른 카드를 그린다:
/// 몽이 든 칸 / 빈 칸(생성) / 구매 칸.
struct SlotPickView: View {

    @State private var viewModel: SlotPickViewModel
    /// 선택을 마치면 닫고 메인으로 돌아간다.
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader

    init(viewModel: SlotPickViewModel, onClose: @escaping () -> Void) {
        _viewModel = State(initialValue: viewModel)
        self.onClose = onClose
    }

    var body: some View {
        ZStack {
            DefaultBackground()

            if viewModel.isLoading {
                LoadingBar()
            } else {
                card                                    // z = 1
                PageIndicator(pageCount: viewModel.slots.count, currentPage: viewModel.index)
                    .frame(maxHeight: .infinity, alignment: .bottom)
                    .padding(.bottom, 5)
                SelectButton(                           // z = 3
                    canGoPrevious: viewModel.canGoPrevious,
                    canGoNext: viewModel.canGoNext,
                    onPrevious: viewModel.goPrevious,
                    onNext: viewModel.goNext
                )
                dialog                                  // z = 4
            }
        }
        .task {
            await loader.preload([
                "mong_body_blind", "mong_rip", "mong_shadow",
                "point_bg", "point_icon_star",
                "btn_icon_left", "btn_icon_right",
                "bnt_bg_blue", "btn_bg_yellow", "btn_bg_disable",
            ])
            await viewModel.load()
        }
        // 몽을 고르면 메인으로 돌아간다.
        .onChange(of: viewModel.shouldReturnToMain) { _, shouldReturn in
            guard shouldReturn else { return }
            viewModel.didReturnToMain()
            onClose()
        }
        // 새로 뽑힌 몽의 스프라이트를 준비한다.
        .task(id: viewModel.currentSlot?.mong?.mongCode) {
            guard let mong = viewModel.currentSlot?.mong else { return }
            await loader.preload([mong.resource.pngName])
        }
    }

    // MARK: - 카드

    @ViewBuilder
    private var card: some View {
        switch viewModel.currentSlot?.kind {
        case let .occupied(mong):
            OccupiedSlotView(
                mong: mong,
                currentMongId: viewModel.currentMongId,
                onDetail: { viewModel.dialog = .detail },
                onGraduate: { viewModel.dialog = .confirmGraduate },
                onDelete: { viewModel.dialog = .confirmDelete },
                onPick: { viewModel.dialog = .confirmPick }
            )
        case .empty:
            EmptySlotView { viewModel.dialog = .create }
        case .purchasable:
            PurchasableSlotView(starPoint: viewModel.starPoint) {
                viewModel.dialog = .confirmBuySlot
            }
        case nil:
            EmptyView()
        }
    }

    // MARK: - 다이얼로그

    @ViewBuilder
    private var dialog: some View {
        switch viewModel.dialog {
        case .create:
            CreateMongDialogView(
                onCreate: { name, sleepAt, wakeupAt in
                    Task { await viewModel.createMong(name: name, sleepAt: sleepAt, wakeupAt: wakeupAt) }
                },
                onCancel: { viewModel.dialog = nil }
            )

        case .confirmBuySlot:
            confirm("새로운 슬롯을\n구매하시겠습니까?") { await viewModel.buySlot() }

        case .confirmDelete:
            if let mong = viewModel.currentSlot?.mong {
                confirm("현재 몽을\n삭제하시겠습니까?") { await viewModel.deleteMong(mong) }
            }

        case .confirmGraduate:
            if let mong = viewModel.currentSlot?.mong {
                confirm("현재 몽을\n졸업시키시겠습니까?") { await viewModel.graduateMong(mong) }
            }

        case .confirmPick:
            if let mong = viewModel.currentSlot?.mong {
                confirm("현재 몽을\n선택하시겠습니까?") { await viewModel.pickMong(mong) }
            }

        case .detail:
            if let mong = viewModel.currentSlot?.mong {
                MongDetailDialogView(mong: mong) { viewModel.dialog = nil }
            }

        case nil:
            EmptyView()
        }
    }

    private func confirm(_ message: String, action: @escaping () async -> Void) -> some View {
        ConfirmDialogView(
            message: message,
            onConfirm: { Task { await action() } },
            onCancel: { viewModel.dialog = nil }
        )
    }
}
