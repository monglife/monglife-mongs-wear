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

    /// 로그아웃은 루트 게이트를 로그인 화면으로 되돌려야 해서 루트 ViewModel 이 필요하다.
    let root: RootViewModel

    @Environment(AppContainer.self) private var container
    @Environment(SpriteLoader.self) private var loader

    /// 슬롯과 컨디션이 같은 ViewModel 을 본다 — Android 도 부모 백스택에 스코프를 걸어 공유한다.
    @State private var slotViewModel: MainSlotViewModel?
    @State private var page: Int = 0
    /// 슬롯 관리 화면. Android 는 라우터로 넘기지만 watchOS 는 전체 화면 시트가 자연스럽다.
    @State private var isSlotPickPresented = false
    /// 환전. nil 이면 닫힘, 값이 있으면 그 종류의 환전 화면이 열린다.
    @State private var exchangeKind: ExchangeViewModel.Kind?
    @State private var isExchangeMenuPresented = false
    /// 먹이 메뉴 / 먹이 화면. nil 이면 닫힘.
    @State private var isFeedMenuPresented = false
    @State private var feedKind: FeedItem.Kind?
    @State private var isInventoryPresented = false
    @State private var isSettingPresented = false
    @State private var isChargePresented = false
    @State private var isNoticePresented = false
    @State private var isFeedbackPresented = false
    @State private var isRandomDrawPresented = false
    @State private var isCollectionMenuPresented = false
    /// 도감. nil 이면 닫힘, 값이 있으면 그 종류의 목록이 열린다.
    @State private var collectionKind: CollectionItem.Kind?
    @State private var isMapSearchPresented = false
    @State private var isTrainingPresented = false
    /// 아직 이식하지 않은 화면. nil 이면 닫힘.
    @State private var notReady: NotReadyDestination?

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
                    .padding(.bottom, 5.ms)
            } else {
                LoadingBar()
            }
        }
        .fullScreenCover(isPresented: $isExchangeMenuPresented) {
            ExchangeMenuView(
                onSelect: { kind in
                    isExchangeMenuPresented = false
                    exchangeKind = kind
                },
                onClose: { isExchangeMenuPresented = false }
            )
        }
        .fullScreenCover(item: $exchangeKind) { kind in
            if let viewModel = container.makeExchangeViewModel(kind: kind) {
                ExchangeView(viewModel: viewModel) {
                    exchangeKind = nil
                    Task { await slotViewModel?.reload() }
                }
            }
        }
        .fullScreenCover(isPresented: $isCollectionMenuPresented) {
            CollectionMenuView { kind in
                isCollectionMenuPresented = false
                collectionKind = kind
            }
        }
        .fullScreenCover(item: $collectionKind) { kind in
            if let viewModel = container.makeCollectionViewModel(kind: kind) {
                CollectionGridView(viewModel: viewModel) { collectionKind = nil }
            }
        }
        .fullScreenCover(isPresented: $isTrainingPresented) {
            TrainingFlowView {
                isTrainingPresented = false
                // 훈련은 스탯과 페이포인트를 바꾼다.
                Task { await slotViewModel?.reload() }
            }
        }
        .fullScreenCover(isPresented: $isMapSearchPresented) {
            if let viewModel = container.makeMapSearchViewModel() {
                MapSearchView(viewModel: viewModel) { isMapSearchPresented = false }
            }
        }
        .fullScreenCover(isPresented: $isNoticePresented) {
            if let viewModel = container.makeNoticeViewModel() {
                NoticeView(viewModel: viewModel) { isNoticePresented = false }
            }
        }
        .fullScreenCover(isPresented: $isFeedbackPresented) {
            if let viewModel = container.makeFeedbackViewModel() {
                FeedbackView(viewModel: viewModel) { isFeedbackPresented = false }
            }
        }
        .fullScreenCover(isPresented: $isRandomDrawPresented) {
            if let viewModel = container.makeRandomDrawViewModel() {
                RandomDrawView(viewModel: viewModel) {
                    isRandomDrawPresented = false
                    // 뽑기로 페이포인트·인벤토리가 바뀐다.
                    Task { await slotViewModel?.reload() }
                }
            }
        }
        .fullScreenCover(isPresented: $isChargePresented) {
            if let viewModel = container.makeChargeViewModel() {
                ChargeView(viewModel: viewModel) { isChargePresented = false }
            }
        }
        .fullScreenCover(item: $notReady) { destination in
            NotReadyView(destination: destination) { notReady = nil }
        }
        .fullScreenCover(isPresented: $isSettingPresented) {
            SettingView(
                viewModel: container.makeSettingViewModel {
                    // 세션이 끝나면 계정 토픽 구독도 끊어야 한다.
                    await container.stopRealtime()
                    await root.signOut()
                }
            ) {
                isSettingPresented = false
            }
        }
        .fullScreenCover(isPresented: $isFeedMenuPresented) {
            FeedMenuView { kind in
                isFeedMenuPresented = false
                feedKind = kind
            }
        }
        .fullScreenCover(item: $feedKind) { kind in
            if let viewModel = container.makeFeedViewModel(kind: kind) {
                FeedView(viewModel: viewModel) {
                    feedKind = nil
                    // 원본은 먹이 화면에서 메인까지 되돌아오며 먹는 표정을 띄운다.
                    Task {
                        await slotViewModel?.reload()
                        await slotViewModel?.eatingEvent()
                    }
                }
            }
        }
        .fullScreenCover(isPresented: $isInventoryPresented) {
            if let viewModel = container.makeInventoryViewModel() {
                InventoryView(viewModel: viewModel) {
                    isInventoryPresented = false
                    Task {
                        await slotViewModel?.reload()
                        await slotViewModel?.eatingEvent()
                    }
                }
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
            //
            // ⚠️ `observe()` 는 **돌아오지 않는다** — 끝에 몽 스트림을 도는 for-await 가 있다.
            // 그래서 뒤에 다른 초기화를 이어 붙이면 그 코드는 영영 실행되지 않는다.
            // (실제로 MQTT 와 푸시 등록을 여기 뒤에 붙였다가 둘 다 죽어 있었다.)
            // 함께 시작해야 하는 것들은 아래의 **별도 `.task`** 에 둔다.
            await viewModel?.observe()
        }
        // 구독 루프와 나란히 도는 초기화. 위 `.task` 는 돌아오지 않으므로 여기 둔다.
        .task {
            // MQTT 는 로그인 뒤 메인이 뜰 때 붙는다. 계정·기기 토픽이 먼저 열리고,
            // 몽 토픽은 아래 onChange 가 현재 몽을 보고 연다.
            await container.startRealtime()

            // 알림 권한은 로그인 뒤에 묻는다 — iOS 는 한 번 거부하면 다시 못 묻는다.
            await container.startPush()
        }
        .onChange(of: slotViewModel?.mong?.mongId) { _, mongId in
            Task { await container.observeRealtimeMong(mongId) }
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
                mong: viewModel.mong,
                onExchange: { exchangeKind = .step }
            )
        case .condition:
            ConditionContentView(viewModel: viewModel)
        case .slot:
            SlotContentView(
                viewModel: viewModel,
                onOpenSlotPick: { isSlotPickPresented = true },
                onOpenFeed: { isFeedMenuPresented = true },
                onOpenInventory: { isInventoryPresented = true }
            )
        case .interaction:
            InteractionContentView(
                mong: viewModel.mong,
                onOpenSlotPick: { isSlotPickPresented = true },
                onOpenExchange: { isExchangeMenuPresented = true },
                onOpenRandomDraw: { isRandomDrawPresented = true },
                onOpenCollection: { isCollectionMenuPresented = true },
                onOpenMapSearch: { isMapSearchPresented = true },
                onOpenTraining: { isTrainingPresented = true },
                onNotReady: { notReady = $0 }
            )
        case .configure:
            ConfigureContentView(
                onOpenSetting: { isSettingPresented = true },
                onOpenCharge: { isChargePresented = true },
                onOpenNotice: { isNoticePresented = true },
                onOpenFeedback: { isFeedbackPresented = true },
                onNotReady: { notReady = $0 }
            )
        }
    }
}
