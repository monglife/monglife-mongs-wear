import MongsModel
import MongsViewModel
import SwiftUI

/// 맵 탐색
///
/// Android `pages/map/SearchMapView.kt` 이식.
/// 현재 위치를 한 번 읽어 서버에 물어보고, 새 맵을 찾으면 도감에 추가된다.
struct MapSearchView: View {

    @State private var viewModel: MapSearchViewModel
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader

    init(viewModel: MapSearchViewModel, onClose: @escaping () -> Void) {
        _viewModel = State(initialValue: viewModel)
        self.onClose = onClose
    }

    var body: some View {
        ZStack {
            DefaultBackground()

            switch viewModel.phase {
            case .idle:
                idle
            case .searching:
                VStack(spacing: 10.ms) {
                    LoadingBar()
                    Text("위치를 찾는 중")
                        .mongsFont(12)
                        .foregroundStyle(MongsColor.lightGray)
                        .lineLimit(1)
                }
            case let .found(item):
                found(item)
            case .notFound:
                message("새로운 맵이 없어요", buttonTitle: "다시") {
                    Task { await viewModel.search() }
                }
            case .denied:
                // 위치 권한은 한 번 거부하면 앱이 다시 못 묻는다 — 설정 앱으로 안내한다.
                message("설정에서 위치 권한을\n켜주세요", buttonTitle: "닫기", action: onClose)
            }
        }
        .task { await loader.preload(["btn_icon_map_search", "bnt_bg_blue", "btn_bg_yellow"]) }
    }

    private var idle: some View {
        VStack(spacing: 12.ms) {
            AnimatedSprite(sprite: loader.sprite(named: "btn_icon_map_search"))
                .frame(width: 60.ms, height: 60.ms)

            Text("맵 탐색")
                .mongsFont(18)
                .foregroundStyle(MongsColor.white)
                .lineLimit(1)

            MongsButton(title: "탐색", style: .blue, width: 90, height: 37, fontSize: 16) {
                Task { await viewModel.search() }
            }
        }
    }

    private func found(_ item: CollectionItem) -> some View {
        VStack(spacing: 10.ms) {
            AnimatedSprite(sprite: loader.sprite(named: MapResourceCode.pngName(item.code)))
                .frame(width: 70.ms, height: 70.ms)

            Text(item.name)
                .mongsFont(16)
                .foregroundStyle(MongsColor.white)
                .lineLimit(1)

            MongsButton(title: "닫기", style: .blue, width: 80, action: onClose)
        }
        .task { await loader.preload([MapResourceCode.pngName(item.code)]) }
    }

    private func message(_ text: String, buttonTitle: String, action: @escaping () -> Void) -> some View {
        VStack(spacing: 12.ms) {
            ForEach(Array(text.components(separatedBy: "\n").enumerated()), id: \.offset) { _, line in
                Text(line)
                    .mongsFont(14)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)
            }
            MongsButton(title: buttonTitle, style: .blue, width: 80, action: action)
        }
    }
}
