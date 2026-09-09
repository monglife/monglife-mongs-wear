import MongsModel
import MongsViewModel
import SwiftUI

/// 메인 좌우 페이저
///
/// Android `wear-view-presentation/.../view/pages/main/MainView.kt` 이식.
///
/// 원본은 몽 유무에 따라 페이저가 갈린다:
/// - 몽 있음: 걸음 / 컨디션 / 슬롯 / 상호작용 / 설정 (5쪽, 슬롯에서 시작)
/// - 몽 없음: 컨디션을 빼고 4쪽 (슬롯에서 시작)
///
/// 배경 밝기도 쪽마다 다르다. 슬롯 쪽만 0.0(원본 그대로)이고 나머지는 0.4 로 어둡게 해서
/// 펫이 있는 쪽으로 시선을 모은다.
struct MainPagerView: View {

    @Environment(AppContainer.self) private var container
    @Environment(SpriteLoader.self) private var loader

    /// 슬롯과 컨디션이 같은 ViewModel 을 본다 — Android 도 부모 백스택에 스코프를 걸어 공유한다.
    @State private var slotViewModel: MainSlotViewModel?
    @State private var page: Int = 0
    /// 슬롯 관리 화면. Android 는 라우터로 넘기지만 watchOS 는 전체 화면 시트가 자연스럽다.
    @State private var isSlotPickPresented = false

    private enum Page {
        case step, condition, slot, interaction, configure
    }

    /// Android `MainPagerViewModel` 의 상수 이식.
    /// 쪽 구성과 밝기를 한 배열에서 같이 뽑아 **인덱스가 어긋날 여지를 없앤다.**
    /// 조건부 자식(`if hasMong { ... }`)을 TabView 안에 두면 태그 매핑이 흔들린다.
    private var pages: [(page: Page, dim: Double)] {
        guard slotViewModel?.mong != nil else {
            return [(.step, 0.4), (.slot, 0.0), (.interaction, 0.4), (.configure, 0.4)]
        }
        return [(.step, 0.4), (.condition, 0.4), (.slot, 0.0), (.interaction, 0.4), (.configure, 0.4)]
    }

    /// 시작 쪽 — 언제나 슬롯이다.
    private var initialPage: Int {
        pages.firstIndex { $0.page == .slot } ?? 0
    }

    private var dim: Double {
        pages.indices.contains(page) ? pages[page].dim : 0.4
    }

    var body: some View {
        ZStack {
            MainBackground(dim: dim)

            // 몽 유무가 정해지기 전에는 페이저를 만들지 않는다.
            // 쪽 수가 도중에 바뀌면 선택된 쪽이 어긋난다.
            if let slotViewModel, slotViewModel.hasLoaded {
                TabView(selection: $page) {
                    ForEach(Array(pages.enumerated()), id: \.offset) { index, entry in
                        content(entry.page, viewModel: slotViewModel)
                            // Android(Wear)는 시계를 앱이 그리지 않아 화면 전체가 콘텐츠 영역이다.
                            // watchOS 는 시계가 항상 위에 겹치는데, 안전 영역을 지키면
                            // 원본의 세로 비중(0.2/0.5/0.3 등)이 그만큼 아래로 밀린다.
                            // 비중을 맞추는 쪽을 택했다 — 시계는 위에 겹쳐 그려진다.
                            .ignoresSafeArea()
                            .tag(index)
                    }
                }
                // 좌우 스와이프. 점은 Android 와 같은 모양으로 직접 그리므로 기본 것은 끈다.
                .tabViewStyle(.page(indexDisplayMode: .never))

                PageIndicator(pageCount: pages.count, currentPage: page)
                    .frame(maxHeight: .infinity, alignment: .bottom)
                    .padding(.bottom, 5)
            } else {
                LoadingBar()
            }
        }
        .fullScreenCover(isPresented: $isSlotPickPresented) {
            if let viewModel = container.makeSlotPickViewModel() {
                SlotPickView(viewModel: viewModel) {
                    isSlotPickPresented = false
                    // 고른 몽을 메인이 다시 읽는다.
                    Task { await slotViewModel?.reload() }
                }
            }
        }
        .task {
            // 메인 배경은 기본 맵의 애니메이션 버전이다 (원본 MainBackground 와 동일).
            await loader.preload(["map_mp000_gif", "icon_loading"])

            guard slotViewModel == nil else { return }
            let viewModel = container.makeMainSlotViewModel()
            slotViewModel = viewModel

            // ⚠️ 로딩은 페이저가 시작한다. 자식 뷰(SlotContentView)에 맡기면
            // "로딩이 끝나야 자식이 만들어지는데, 자식이 로딩을 시작하는" 순환이 된다.
            await viewModel?.observe()
        }
        // 페이저가 실제로 만들어진 뒤에 시작 쪽으로 맞춘다.
        // TabView 가 붙기 전에 selection 을 써 두면 TabView 가 자기 값으로 덮어쓴다.
        .onChange(of: slotViewModel?.hasLoaded) { _, loaded in
            guard loaded == true else { return }
            page = initialPage
        }
    }

    @ViewBuilder
    private func content(_ page: Page, viewModel: MainSlotViewModel) -> some View {
        switch page {
        case .step:
            StepContentView(
                viewModel: container.makeMainStepViewModel(),
                mong: viewModel.mong
            )
        case .condition:
            ConditionContentView(viewModel: viewModel)
        case .slot:
            SlotContentView(viewModel: viewModel) {
                isSlotPickPresented = true
            }
        case .interaction:
            InteractionContentView(mong: viewModel.mong) {
                isSlotPickPresented = true
            }
        case .configure:
            ConfigureContentView()
        }
    }
}
