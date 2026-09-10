import MongsViewModel
import SwiftUI

/// 오류 신고
///
/// Android `pages/feedback/FeedbackView.kt` + `dialog/.../CreateFeedbackDialog.kt` 이식.
/// 원본은 목록 화면에서 다이얼로그를 띄우지만, 목록에 담을 게 없어(등록만 한다)
/// **작성 화면 하나로 합쳤다.** 제목 → 내용 두 단계는 그대로다.
struct FeedbackView: View {

    @State private var viewModel: FeedbackViewModel
    let onClose: () -> Void

    init(viewModel: FeedbackViewModel, onClose: @escaping () -> Void) {
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
            }
        }
        .onChange(of: viewModel.didSubmit) { _, done in
            if done { onClose() }
        }
    }

    @ViewBuilder
    private var content: some View {
        @Bindable var model = viewModel

        VStack(spacing: 6.ms) {
            HStack(spacing: 6.ms) {
                Text("제목")
                    .mongsFont(11)
                    .foregroundStyle(viewModel.step == .title ? MongsColor.lightGray : MongsColor.darkGray)
                Text("내용")
                    .mongsFont(11)
                    .foregroundStyle(viewModel.step == .content ? MongsColor.lightGray : MongsColor.darkGray)
            }

            Group {
                switch viewModel.step {
                case .title:
                    TextField("무엇이 문제인가요", text: $model.title)
                        .mongsFont(15)
                        .multilineTextAlignment(.center)
                case .content:
                    TextField("자세히 알려주세요", text: $model.content, axis: .vertical)
                        .mongsFont(13)
                        .lineLimit(4, reservesSpace: true)
                }
            }
            .frame(maxHeight: .infinity)

            footer
        }
        .padding(.vertical, 8.ms)
    }

    private var footer: some View {
        HStack(spacing: 5.ms) {
            MongsButton(
                title: viewModel.step == .title ? "닫기" : "이전",
                style: .blue, width: 58
            ) {
                if viewModel.step == .title { onClose() } else { viewModel.back() }
            }

            if viewModel.step == .title {
                MongsButton(title: "다음", width: 58, isEnabled: viewModel.canGoNext) {
                    viewModel.next()
                }
            } else {
                MongsButton(title: "등록", width: 58, isEnabled: viewModel.canSubmit) {
                    Task { await viewModel.submit() }
                }
            }
        }
    }
}
