import MongsViewModel
import SwiftUI

/// 앱 루트 게이트
///
/// Android `presentation/wear-view-presentation/.../view/layout/LayoutView.kt` 대응.
/// 게이트 순서: 설정 → 로딩 → 강제 업데이트 → 로그인 → 메인.
struct RootView: View {

    @Environment(AppContainer.self) private var container
    @Environment(SpriteLoader.self) private var spriteLoader
    @State private var viewModel: RootViewModel?

    var body: some View {
        ZStack {
            // Android `LayoutView` 도 화면 전체 뒤에 배경을 한 장 깔아 둔다.
            // 로그인·로딩·업데이트 안내가 전부 이걸 쓰고, 메인만 그 위에 맵을 덮는다.
            DefaultBackground()

            switch container.configResult {
            case .success:
                gated
            case let .failure(error):
                ConfigurationErrorView(message: String(describing: error))
            }
        }
        // 배경 스프라이트는 앱에서 가장 먼저 필요하다. 여기서 한 번만 준비한다.
        .task { await spriteLoader.preload(["map_mp000", "icon_loading"]) }
    }

    @ViewBuilder
    private var gated: some View {
        if let viewModel {
            content(viewModel)
                .task { await viewModel.start() }
        } else {
            // 설정은 읽혔는데 ViewModel 을 못 만든 경우는 없다. 방어적으로만 둔다.
            ProgressView()
                .onAppear { viewModel = container.makeRootViewModel() }
        }
    }

    @ViewBuilder
    private func content(_ viewModel: RootViewModel) -> some View {
        switch viewModel.phase {
        case .loading:
            ProgressView()
        case .mustUpdate:
            NeedUpdateView()
        case .unreachable:
            UnreachableView(viewModel: viewModel)
        case .signedOut:
            LoginView(viewModel: viewModel)
        case .signedIn:
            MainPagerView()
        }
    }
}
