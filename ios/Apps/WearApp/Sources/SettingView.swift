import MongsViewModel
import SwiftUI

/// 설정 화면
///
/// Android `pages/setting/SettingView.kt` 이식.
///
/// 원본은 `ScalingLazyColumn` — 중앙에서 멀수록 작아지고 흐려지는 리스트다.
/// watchOS 는 `List` 에 `.listStyle(.carousel)` 만 주면 같은 효과를 시스템이 준다.
/// 크라운 스크롤과 위치 인디케이터(`PositionIndicator`)도 공짜로 따라온다.
struct SettingView: View {

    @State private var viewModel: SettingViewModel
    let onClose: () -> Void

    init(viewModel: SettingViewModel, onClose: @escaping () -> Void) {
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

                if viewModel.isLogoutConfirmPresented {
                    ConfirmDialogView(
                        message: "로그아웃\n하시겠습니까?",
                        onConfirm: { Task { await viewModel.logout() } },
                        onCancel: viewModel.cancelLogout
                    )
                }
            }
        }
        .task { await viewModel.load() }
        .onChange(of: viewModel.didSignOut) { _, signedOut in
            if signedOut { onClose() }
        }
    }

    private var list: some View {
        List {
            header("설정")

            MongsToggleChip(
                label: "알림",
                isOn: viewModel.notificationOption && viewModel.notificationPermission,
                isEnabled: viewModel.canToggleNotification
            ) {
                Task { await viewModel.toggleNotificationOption() }
            }

            MongsChip(label: "로그아웃", secondaryLabel: "Apple 계정 로그아웃", action: viewModel.askLogout)

            header("권한")

            MongsToggleChip(
                label: "알림 권한",
                isOn: viewModel.notificationPermission,
                isEnabled: true
            ) {
                Task { await viewModel.requestNotificationPermission() }
            }

            // 활동 권한은 상태를 그릴 수 없다 — iOS 는 HealthKit **읽기** 권한을
            // 언제나 `.notDetermined` 로 답한다. 스위치를 두면 거짓말이 되므로
            // "다시 요청" 만 할 수 있는 줄로 바꿨다.
            MongsChip(label: "활동 권한", secondaryLabel: "건강 앱 접근 요청") {
                Task { await viewModel.requestActivityPermission() }
            }
        }
        .listStyle(.carousel)
    }

    private func header(_ title: String) -> some View {
        Text(title)
            .mongsFont(16)
            .foregroundStyle(MongsColor.white)
            .lineLimit(1)
            .frame(maxWidth: .infinity)
            .padding(15)
            .listRowBackground(Color.clear)
    }
}

/// 스위치가 달린 줄
///
/// Android `component/common/chip/ToggleChip.kt` 이식.
/// 배경은 검정 30% — 원본 `backgroundColor.copy(alpha = 0.3f)` 그대로다.
struct MongsToggleChip: View {

    let label: String
    let isOn: Bool
    let isEnabled: Bool
    let onToggle: () -> Void

    var body: some View {
        Button(action: { if isEnabled { onToggle() } }) {
            HStack(spacing: 0) {
                Spacer().frame(width: 10)

                Text(label)
                    .mongsFont(16)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .leading)

                // 시스템 Toggle 은 자기 탭도 받으므로 줄 전체 탭과 겹친다.
                // 그리기만 하고 입력은 줄이 받게 한다.
                Toggle("", isOn: .constant(isOn))
                    .labelsHidden()
                    .allowsHitTesting(false)
                    .opacity(isEnabled ? 1 : 0.4)
            }
            // 스위치 자리는 hit testing 을 껐으므로 그대로 두면 그 영역이 죽는다.
            // 원본은 Chip 전체와 Switch 양쪽이 같은 콜백을 받는다 — 행 전체를 탭 영역으로 만든다.
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled)
        .listRowBackground(chipBackground)
    }

    private var chipBackground: some View {
        RoundedRectangle(cornerRadius: 26).fill(Color.black.opacity(0.3))
    }
}

/// 눌러서 넘어가는 줄
///
/// Android `component/common/chip/Chip.kt` 이식.
struct MongsChip: View {

    let label: String
    var secondaryLabel: String?
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 0) {
                Spacer().frame(width: 10)

                VStack(alignment: .leading, spacing: 2) {
                    Text(label)
                        .mongsFont(16)
                        .foregroundStyle(MongsColor.white)
                        .lineLimit(1)

                    if let secondaryLabel {
                        Text(secondaryLabel)
                            .mongsFont(10)
                            .foregroundStyle(MongsColor.lightGray)
                            .lineLimit(1)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .listRowBackground(RoundedRectangle(cornerRadius: 26).fill(Color.black.opacity(0.3)))
    }
}
