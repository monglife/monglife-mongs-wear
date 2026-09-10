import MongsModel
import MongsViewModel
import SwiftUI

/// 훈련 메뉴
///
/// Android `pages/training/TrainingMenuView.kt` 이식.
/// 원본은 `ScalingLazyColumn` + `IconChip` — 여기서는 `List(.carousel)` 이다.
struct TrainingMenuView: View {

    let types: [TrainingType]
    let onSelect: (TrainingType) -> Void

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        ZStack {
            DefaultBackground()

            List {
                Text("훈련")
                    .mongsFont(16)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity)
                    .padding(15.ms)
                    .listRowBackground(Color.clear)

                ForEach(types) { type in
                    Button {
                        onSelect(type)
                    } label: {
                        HStack(spacing: 8.ms) {
                            AnimatedSprite(sprite: loader.sprite(named: Self.icon(for: type)))
                                .frame(width: 28.ms, height: 28.ms)

                            VStack(alignment: .leading, spacing: 2.ms) {
                                Text(type.trainingName)
                                    .mongsFont(15)
                                    .foregroundStyle(MongsColor.white)
                                    .lineLimit(1)
                                Text("+ \(type.payPoint)P")
                                    .mongsFont(10)
                                    .foregroundStyle(MongsColor.lightGray)
                                    .lineLimit(1)
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .listRowBackground(
                        RoundedRectangle(cornerRadius: 26.ms).fill(Color.black.opacity(0.3))
                    )
                }
            }
            .listStyle(.carousel)
        }
        .task { await loader.preload(types.map(Self.icon(for:))) }
    }

    static func icon(for type: TrainingType) -> String {
        switch type.game {
        case .runner: "btn_icon_runner"
        case .basketball: "btn_icon_basketball"
        case .rockPaperScissors: "btn_icon_rock_paper_scissors"
        case nil: "btn_icon_activity"
        }
    }
}

/// 참가 다이얼로그
///
/// Android `dialog/pages/training/TrainingEnteringDialog.kt` 이식.
/// **막 아무 곳이나 터치하면 시작**한다 ("터치해서 시작하기").
struct TrainingEnteringDialog: View {

    let type: TrainingType
    let canEnter: Bool
    let onStart: () -> Void

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        ZStack {
            Color.black.opacity(0.9)
                .ignoresSafeArea()
                .contentShape(Rectangle())
                .onTapGesture { if canEnter { onStart() } }

            VStack(spacing: 6.ms) {
                Text("Mongs")
                    .mongsFont(18)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)

                Text(type.trainingName)
                    .mongsFont(28)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)

                Text("제한시간 " + (type.timeout > 0 ? "\(type.timeout)s" : "없음"))
                    .mongsFont(13)
                    .foregroundStyle(MongsColor.lightGray)
                    .lineLimit(1)

                Text(canEnter ? "터치해서 시작하기" : "페이포인트가 부족해요")
                    .mongsFont(13)
                    .foregroundStyle(canEnter ? MongsColor.lightGray : MongsColor.red)
                    .lineLimit(1)
                    .padding(.top, 6.ms)

                // 보상. 원본은 `+ payPoint` 로 **버는 쪽**을 보여준다.
                HStack(spacing: 10.ms) {
                    AnimatedSprite(sprite: loader.sprite(named: "point_icon_pay"))
                        .frame(width: 20.ms, height: 20.ms)
                    Text("+ \(type.payPoint)")
                        .mongsFont(16)
                        .foregroundStyle(MongsColor.white)
                        .lineLimit(1)
                }
                .padding(.top, 10.ms)
            }
        }
    }
}

/// 결과 다이얼로그
///
/// Android `dialog/pages/training/TrainingOverDialog.kt` 이식.
struct TrainingOverDialog: View {

    let result: TrainingResult
    let onClose: () -> Void

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        ZStack {
            Color.black.opacity(0.9).ignoresSafeArea()

            VStack(spacing: 8.ms) {
                AnimatedSprite(sprite: loader.sprite(named: result.isSuccess ? "txt_win" : "txt_lose"),
                               contentMode: nil)
                    .frame(width: 90.ms, height: 35.ms)

                Text("점수 \(result.score)")
                    .mongsFont(14)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)

                HStack(spacing: 10.ms) {
                    AnimatedSprite(sprite: loader.sprite(named: "point_icon_pay"))
                        .frame(width: 20.ms, height: 20.ms)
                    Text("+ \(result.rewardPayPoint)")
                        .mongsFont(16)
                        .foregroundStyle(MongsColor.white)
                        .lineLimit(1)
                }

                MongsButton(title: "종료", style: .blue, width: 80, action: onClose)
                    .padding(.top, 6.ms)
            }
        }
        .task { await loader.preload(["txt_win", "txt_lose", "point_icon_pay"]) }
    }
}
