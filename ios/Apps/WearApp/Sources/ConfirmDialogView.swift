import SwiftUI

/// 확인/취소 다이얼로그
///
/// Android `dialog/common/ConfirmAndCancelDialog.kt` 이식.
/// 화면 전체를 95% 검은 막으로 덮고, **막을 누르면 취소**된다.
/// 문구는 줄바꿈으로 나눠 각 줄을 같은 높이로 나눠 갖는다(원본 `weight(0.65f / texts.size)`).
struct ConfirmDialogView: View {

    let message: String
    let onConfirm: () -> Void
    let onCancel: () -> Void

    private var lines: [String] {
        message.trimmingCharacters(in: .whitespacesAndNewlines).components(separatedBy: "\n")
    }

    var body: some View {
        ZStack {
            Color.black.opacity(0.95)
                .ignoresSafeArea()
                .onTapGesture(perform: onCancel)

            VStack(spacing: 0) {
                VStack(spacing: 0) {
                    ForEach(Array(lines.enumerated()), id: \.offset) { _, line in
                        Text(line)
                            .mongsFont(14)
                            .foregroundStyle(MongsColor.white)
                            .lineLimit(1)
                            .frame(maxHeight: .infinity)
                    }
                }
                .frame(height: 150 * 0.65)

                HStack(spacing: 5) {
                    MongsButton(title: "닫기", style: .blue, action: onCancel)
                    MongsButton(title: "확인", action: onConfirm)
                }
                .frame(height: 150 * 0.35)
            }
            .frame(height: 150)
        }
    }
}
