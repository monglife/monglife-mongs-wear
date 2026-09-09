import MongsModel
import MongsViewModel
import SwiftUI

/// 슬롯(펫) 쪽
///
/// Android `wear-view-presentation/.../view/pages/main/SlotContent.kt` 이식.
///
/// 원본은 레이어를 z 순서로 쌓는다:
/// 0. 알(level 0)일 때 부화 진행 링
/// 1. 콘텐츠 — 상태에 따라 섹션이 갈린다 (정상 / 죽음 / 삭제 / 졸업 / 비어 있음)
/// 2. 이펙트 — 똥, 하트, 수면
/// 3. 다이얼로그 — 몽을 탭하면 열리는 상호작용 메뉴
///
/// **버튼을 화면에 두지 않는다.** 몽을 탭해 다이얼로그를 여는 것이 원본 방식이다.
struct SlotContentView: View {

    @Bindable var viewModel: MainSlotViewModel

    @Environment(SpriteLoader.self) private var loader

    #if DEBUG
    /// 이펙트를 눈으로 확인하기 위한 것 (Debug 전용).
    ///
    /// 진화·졸업 연출은 서버가 몽을 그 상태로 만들어 줘야 볼 수 있는데,
    /// 경험치를 채우려면 실제로 먹이고 훈련시켜야 한다. 화면만 확인할 때 쓴다.
    ///
    /// ```
    /// xcrun simctl launch <UDID> com.mongs.wear -MongsPreviewEffect evolution
    /// ```
    /// 값: `evolution` / `graduation` / `graduated` / `poopClean` / `loading` / `poop`
    private var previewEffect: String? {
        UserDefaults.standard.string(forKey: "MongsPreviewEffect")
    }

    @State private var previewEvolving = false
    #endif

    /// 알에서 부화하기까지 걸리는 시간. 원본 `SlotContent` 의 `5 * 60 * 1000L`.
    private static let hatchDuration: TimeInterval = 5 * 60

    init(viewModel: MainSlotViewModel) {
        self.viewModel = viewModel
    }

    var body: some View {
        ZStack {
            if viewModel.uiState.loadingBar {
                LoadingBar()
            } else if let mong = viewModel.mong {
                hatchProgress(mong)      // z = 0
                content(mong)            // z = 1
                effects(mong)            // z = 2
                dialog(mong)             // z = 3
            } else {
                emptySection
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .task {
            await loader.preload(
                MongExpression.allCases.map(\.spriteName)
                    + ["mong_body_blind", "mong_rip", "bnt_bg_blue",
                       "icon_poop", "icon_sleep", "icon_heart", "icon_loading"]
            )
        }
        // 몽이 바뀌면(진화 등) 새 몸통 스프라이트를 준비한다.
        .task(id: viewModel.mong?.mongCode) {
            guard let code = viewModel.mong?.resource else { return }
            await loader.preload([code.animationName])
        }
    }

    // MARK: - z0 부화 진행 링

    /// 알은 5분 뒤 부화한다. 남은 시간을 화면 테두리 링으로 보여준다.
    @ViewBuilder
    private func hatchProgress(_ mong: Mong) -> some View {
        if mong.level == 0 {
            TimelineView(.periodic(from: .now, by: 1)) { context in
                let elapsed = context.date.timeIntervalSince(mong.createdAt)
                let ratio = min(max(elapsed / Self.hatchDuration, 0), 1)

                EdgeProgressRing(progress: ratio * 100)
            }
        }
    }

    // MARK: - z1 콘텐츠

    @ViewBuilder
    private func content(_ mong: Mong) -> some View {
        switch mong.stateCode {
        case .dead:
            // 원본 `DeadSection` — 몽 대신 묘비를 그린다. 눌러도 다이얼로그는 열리지 않는다.
            bottomSprite("mong_rip", size: 130)

        case .delete:
            // 원본 `DeleteSection` — 묘비 + 안내 문구
            ZStack {
                bottomSprite("mong_rip", size: 130)
                notice("삭제되었습니다\n\n슬롯을 변경해주세요")
            }

        case .graduate:
            // 원본 `GraduatedSection` — 몽을 어둡게 깔고 안내 문구
            ZStack {
                Color.black.opacity(0.6).ignoresSafeArea()
                VStack {
                    Spacer(minLength: 0)
                    MongView(code: mong.resource, expression: .smile, bodySize: 120)
                        .padding(.bottom, 25)
                }
                notice("졸업한 몽입니다\n\n슬롯을 변경해주세요")
            }

        default:
            // 진화 연출 중에는 이펙트 레이어가 몽을 직접 그리므로 여기서는 감춘다
            // (원본도 `if (!uiState.value.isEvolving)` 로 NormalSection 을 건너뛴다).
            if !viewModel.isEvolving {
                mongBody(mong)
            }
        }
    }

    /// 화면 아래에 스프라이트를 붙인다 (원본 `Alignment.BottomCenter` + `padding(bottom = 25.dp)`).
    private func bottomSprite(_ name: String, size: CGFloat) -> some View {
        VStack {
            Spacer(minLength: 0)
            AnimatedSprite(sprite: loader.sprite(named: name))
                .frame(width: size, height: size)
                .padding(.bottom, 25)
        }
    }

    /// 상태 안내 문구. 원본은 검은 막(0.6) 위에 픽셀 폰트 16sp 로 그린다.
    private func notice(_ text: String) -> some View {
        ZStack {
            Color.black.opacity(0.6).ignoresSafeArea()
            Text(text)
                .mongsFont(16)
                .foregroundStyle(MongsColor.white)
                .multilineTextAlignment(.center)
        }
    }

    /// 원본 `NormalSection` — 몽을 **화면 아래에 붙이고** 22dp 띄운다.
    private func mongBody(_ mong: Mong) -> some View {
        VStack {
            Spacer(minLength: 0)
            MongView(
                code: mong.resource,
                expression: mong.expression(isHappy: viewModel.isHappy),
                bodySize: 120
            )
            .onTapGesture { viewModel.openInteractionDialog() }
            .padding(.bottom, 22)
        }
    }

    // MARK: - z2 이펙트

    @ViewBuilder
    private func effects(_ mong: Mong) -> some View {
        #if DEBUG
        if let previewEffect {
            previewEffects(previewEffect, mong)
        } else {
            realEffects(mong)
        }
        #else
        realEffects(mong)
        #endif
    }

    #if DEBUG
    @ViewBuilder
    private func previewEffects(_ kind: String, _ mong: Mong) -> some View {
        switch kind {
        case "evolution":
            // 미리보기는 ViewModel 을 거치지 않는다 — `startEvolution()` 이 몽 상태를 확인하는데,
            // 미리보기의 요점이 바로 그 상태를 못 만드는 상황이기 때문이다.
            EvolutionEffect(
                mongResource: mong.resource,
                isEvolving: previewEvolving,
                onStart: { previewEvolving = true },
                onFinished: {
                    // 연출이 0.7초라 한 번 보고 놓치기 쉽다. 미리보기에서는 반복시킨다.
                    Task {
                        previewEvolving = false
                        try? await Task.sleep(for: .seconds(1))
                        previewEvolving = true
                    }
                }
            )
        case "graduation": GraduationEffect()
        case "graduated": GraduatedEffect()
        case "poopClean": PoopCleanEffect()
        case "loading": SlotLoadingEffect()
        case "poop": PoopEffect(poopCount: 4)
        default: EmptyView()
        }
    }
    #endif

    @ViewBuilder
    private func realEffects(_ mong: Mong) -> some View {
        switch mong.stateCode {
        case .normal:
            // 원본 순서: 로딩 > 하트 > 똥치우기 > 수면. 하나만 보인다.
            if viewModel.uiState.isBusy {
                SlotLoadingEffect()
            } else if viewModel.isHappy {
                SlotBadgeEffect(kind: .heart)
            } else if viewModel.isPoopCleaning {
                PoopCleanEffect()
            } else if mong.isSleep {
                SlotBadgeEffect(kind: .sleep)
            }
            // 똥은 위 이펙트와 별개로 항상 그린다.
            PoopEffect(poopCount: mong.poopCount)

        case .graduateReady:
            // 연출을 한 번 본 뒤에는 배지만 남는다 (원본 `graduateCheck`).
            if viewModel.didPlayGraduation {
                GraduatedEffect()
            } else {
                GraduationEffect { viewModel.markGraduationPlayed() }
            }

        case .evolutionReady:
            // 자는 중에는 진화 안내를 띄우지 않는다 (원본과 동일).
            if !mong.isSleep {
                EvolutionEffect(
                    mongResource: mong.resource,
                    isEvolving: viewModel.isEvolving,
                    onStart: { viewModel.startEvolution() },
                    onFinished: { Task { await viewModel.evolve() } }
                )
            }

        default:
            EmptyView()
        }
    }

    // MARK: - z3 다이얼로그

    @ViewBuilder
    private func dialog(_ mong: Mong) -> some View {
        if viewModel.isInteractionDialogOpen {
            InteractionDialogView(mong: mong, viewModel: viewModel)
        }
    }

    // MARK: - 몽 없음

    /// Android `EmptySection.kt` 이식 — 가려진 몽 실루엣 위에 "!" 를 얹는다.
    private var emptySection: some View {
        VStack {
            Spacer(minLength: 0)
            ZStack {
                AnimatedSprite(sprite: loader.sprite(named: "mong_body_blind"))
                    .frame(width: 100, height: 100)
                Text("!")
                    .mongsFont(25)
                    .foregroundStyle(MongsColor.white)
            }
            MongsButton(title: "슬롯 선택", style: .blue, width: 90) {
                // TODO: SlotPick 화면 (v1 후속)
            }
            .padding(.top, 8)
            Spacer().frame(height: 30)
        }
    }
}
