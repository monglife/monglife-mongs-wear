import MongsModel
import MongsViewModel
import SwiftUI

/// 도감 메뉴
///
/// Android `pages/collection/CollectionMenuView.kt` 이식.
/// 0.49 / 0.02 / 0.49 — `FeedMenuView` 와 같은 모양이다.
struct CollectionMenuView: View {

    let onSelect: (CollectionItem.Kind) -> Void

    var body: some View {
        ZStack {
            DefaultBackground()

            GeometryReader { geometry in
                let available = geometry.size.height

                VStack(spacing: 0) {
                    entry("몽 컬렉션") { onSelect(.mong) }
                        .frame(height: available * 0.49)

                    ZStack {
                        Rectangle().fill(Color.white).frame(height: 2.ms)
                    }
                    .frame(height: available * 0.02)

                    entry("맵 컬렉션") { onSelect(.map) }
                        .frame(height: available * 0.49)
                }
            }
        }
    }

    private func entry(_ title: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .mongsFont(18)
                .foregroundStyle(MongsColor.white)
                .lineLimit(1)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

/// 도감 격자
///
/// Android `CollectionMongView.kt` / `CollectionMapView.kt` 이식.
/// **한 줄에 3개**씩 원형 버튼으로 늘어놓고, 못 모은 항목은 `?` 로 그린다.
struct CollectionGridView: View {

    @State private var viewModel: CollectionViewModel
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader

    init(viewModel: CollectionViewModel, onClose: @escaping () -> Void) {
        _viewModel = State(initialValue: viewModel)
        self.onClose = onClose
    }

    /// 원본과 같은 3열.
    private let columns = Array(repeating: GridItem(.flexible(), spacing: 6), count: 3)

    var body: some View {
        ZStack {
            DefaultBackground()

            if viewModel.isLoading {
                LoadingBar()
            } else {
                grid

                // 모은 항목을 탭하면 이름을 띄운다.
                if let selected = viewModel.selected {
                    ZStack {
                        Color.black.opacity(0.9)
                            .ignoresSafeArea()
                            .onTapGesture { viewModel.clearSelection() }

                        VStack(spacing: 10.ms) {
                            AnimatedSprite(sprite: loader.sprite(named: spriteName(for: selected)))
                                .frame(width: 60.ms, height: 60.ms)
                            Text(selected.name)
                                .mongsFont(16)
                                .foregroundStyle(MongsColor.white)
                                .lineLimit(1)
                            MongsButton(title: "닫기", style: .blue, width: 80) {
                                viewModel.clearSelection()
                            }
                        }
                    }
                }
            }
        }
        .task {
            await viewModel.load()
            // 모은 것만 스프라이트가 필요하다.
            await loader.preload(viewModel.items.filter(\.isIncluded).map(spriteName(for:)))
        }
    }

    private var grid: some View {
        ScrollView {
            VStack(spacing: 8.ms) {
                Text(viewModel.kind.title)
                    .mongsFont(16)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)
                    .padding(.top, 15.ms)

                LazyVGrid(columns: columns, spacing: 6.ms) {
                    ForEach(viewModel.items) { item in
                        cell(item)
                    }
                }
                .padding(.horizontal, 6.ms)
                .padding(.bottom, 15.ms)
            }
        }
    }

    /// 모은 항목은 스프라이트, 못 모은 항목은 `?`.
    @ViewBuilder
    private func cell(_ item: CollectionItem) -> some View {
        if item.isIncluded {
            MongsCircleButton(
                iconName: spriteName(for: item),
                borderName: "btn_border_purple_dark",
                size: 48, iconSize: 30
            ) {
                viewModel.select(item)
            }
        } else {
            MongsCircleTextButton(title: "?", borderName: "btn_border_purple_dark", size: 48) {}
        }
    }

    private func spriteName(for item: CollectionItem) -> String {
        switch item.kind {
        case .mong: MongResourceCode.resolve(item.code).pngName
        case .map: MapResourceCode.pngName(item.code)
        }
    }
}
