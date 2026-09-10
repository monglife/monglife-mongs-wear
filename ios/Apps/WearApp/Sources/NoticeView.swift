import MongsModel
import MongsViewModel
import SwiftUI

/// 공지사항
///
/// Android `pages/notice/NoticeView.kt` 이식.
/// `ScalingLazyColumn` 자리에 `List(.carousel)` — `SettingView` 와 같은 대응이다.
/// 리스트 끝에 닿으면 다음 쪽을 이어 붙인다 (원본 `OnScroll`).
struct NoticeView: View {

    @State private var viewModel: NoticeViewModel
    let onClose: () -> Void

    init(viewModel: NoticeViewModel, onClose: @escaping () -> Void) {
        _viewModel = State(initialValue: viewModel)
        self.onClose = onClose
    }

    var body: some View {
        ZStack {
            DefaultBackground()

            if viewModel.isLoading {
                LoadingBar()
            } else {
                list

                if let detail = viewModel.detail {
                    NoticeDetailDialogView(content: detail, onClose: viewModel.closeDetail)
                }
            }
        }
        .task { await viewModel.load() }
    }

    private var list: some View {
        List {
            Text("공지사항")
                .mongsFont(16)
                .foregroundStyle(MongsColor.white)
                .lineLimit(1)
                .frame(maxWidth: .infinity)
                .padding(15.ms)
                .listRowBackground(Color.clear)

            ForEach(viewModel.notices) { notice in
                MongsChip(
                    label: notice.title,
                    secondaryLabel: "[\(notice.writerName)] \(Self.dateText(notice.createdAt))"
                ) {
                    viewModel.openDetail(notice)
                }
                // 마지막 줄이 보이면 다음 쪽을 불러온다.
                .onAppear {
                    if notice.id == viewModel.notices.last?.id {
                        Task { await viewModel.loadMoreIfNeeded() }
                    }
                }
            }

            if viewModel.isLoadingMore {
                LoadingBar(size: 25)
                    .frame(maxWidth: .infinity)
                    .listRowBackground(Color.clear)
            }
        }
        .listStyle(.carousel)
    }

    /// 원본 `TimeUtil.localDateTimeToString` 대응.
    private static func dateText(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy.MM.dd"
        return formatter.string(from: date)
    }
}

/// 공지 상세
///
/// Android `dialog/pages/notice/NoticeDetailDialog.kt` 이식.
/// 95% 검은 막 위에 본문을 띄우고, **막 아무 곳이나 누르면 닫힌다.**
struct NoticeDetailDialogView: View {

    let content: String
    let onClose: () -> Void

    var body: some View {
        ZStack {
            Color.black.opacity(0.95)
                .ignoresSafeArea()
                .onTapGesture(perform: onClose)

            VStack(spacing: 8.ms) {
                Text("공지사항")
                    .mongsFont(14)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)

                ScrollView {
                    Text(content)
                        .mongsFont(13)
                        .foregroundStyle(MongsColor.white)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                .frame(width: 144.ms)

                MongsButton(title: "닫기", style: .blue, width: 80, action: onClose)
            }
            .padding(.vertical, 20.ms)
        }
    }
}
