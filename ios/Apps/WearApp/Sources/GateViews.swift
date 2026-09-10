import MongsViewModel
import SwiftUI

/// 강제 업데이트 안내
///
/// Android `wear-view-presentation/.../layout/NeedUpdateContent.kt` 대응.
/// 서버가 `mustUpdate` 를 주면 여기서 멈춘다 — 구버전이 바뀐 API 를 두드리면
/// 사용자는 원인을 알 수 없는 오류만 보게 된다.
struct NeedUpdateView: View {
    var body: some View {
        ScrollView {
            VStack(spacing: 8.ms) {
                Text("업데이트가 필요합니다")
                    .font(.system(size: 14.ms, weight: .semibold))
                    .multilineTextAlignment(.center)
                Text("App Store 에서 Mongs 를 최신 버전으로 업데이트해 주세요.")
                    .font(.system(size: 11.ms))
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
            .padding(.vertical, 12.ms)
        }
    }
}

/// 서버에 닿지 못했을 때
///
/// 로그인 화면으로 밀어붙이지 않는 게 요점이다. 서버가 죽었는데 로그인 화면을 보여주면
/// 사용자가 자기 계정 문제로 오해한다.
struct UnreachableView: View {

    let viewModel: RootViewModel

    var body: some View {
        ScrollView {
            VStack(spacing: 10.ms) {
                Text("서버에 연결할 수 없습니다")
                    .font(.system(size: 13.ms, weight: .semibold))
                    .multilineTextAlignment(.center)
                Button("다시 시도") {
                    Task { await viewModel.start() }
                }
                .font(.footnote)
            }
            .padding(.vertical, 12.ms)
        }
    }
}

/// xcconfig ↔ Info.plist 배선이 깨졌을 때 원인을 그대로 보여준다.
struct ConfigurationErrorView: View {

    let message: String

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 8.ms) {
                Text("설정 오류")
                    .font(.headline)
                    .foregroundStyle(.red)
                Text(message)
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}
