import MongsModel
import SwiftUI

/// 똥 이펙트
///
/// Android `component/pages/main/slot/effect/PoopEffect.kt` 이식.
/// 최대 4개까지 그리고, 개수마다 위치가 정해져 있다 — 몽 좌우로 번갈아 흩어진다.
struct PoopEffect: View {

    let poopCount: Int

    @Environment(SpriteLoader.self) private var loader

    /// 원본의 `poopPadding` 배열. (x 오프셋, 아래 여백)
    /// 원본은 start/end 패딩으로 좌우를 갈랐는데 여기서는 부호로 표현한다.
    private static let placements: [(x: CGFloat, bottom: CGFloat)] = [
        (-60, 18), (54, 16), (-80, 26), (76, 28),
    ]

    var body: some View {
        ZStack(alignment: .bottom) {
            Color.clear
            ForEach(0 ..< min(poopCount, Self.placements.count), id: \.self) { index in
                let placement = Self.placements[index]
                AnimatedSprite(sprite: loader.sprite(named: "icon_poop"))
                    .frame(width: 25, height: 25)
                    .offset(x: placement.x, y: -placement.bottom)
            }
        }
        .allowsHitTesting(false)
    }
}

/// 수면 / 하트 이펙트
///
/// Android `SleepEffect.kt` / `HeartEffect.kt` 이식.
/// 둘 다 몽 위쪽에 작은 아이콘 하나를 띄우는 같은 모양이라 하나로 합쳤다.
struct SlotBadgeEffect: View {

    enum Kind {
        case sleep, heart

        var spriteName: String {
            switch self {
            case .sleep: "icon_sleep"
            case .heart: "icon_heart"
            }
        }
    }

    let kind: Kind

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        VStack {
            AnimatedSprite(sprite: loader.sprite(named: kind.spriteName))
                .frame(width: 17, height: 17)
                .padding(.top, 25)
            Spacer(minLength: 0)
        }
        .allowsHitTesting(false)
    }
}


/// 순서대로 이미지를 갈아 끼우는 연출
///
/// Android `EvolutionEffect` / `GraduationEffect` 가 쓰는 공통 패턴 —
/// `LaunchedEffect` 에서 인덱스를 올리며 `delay` 를 걸고, 끝나면 콜백을 부른다.
/// 프레임마다 머무는 시간이 다르므로 GIF 로는 표현되지 않는다.
struct FrameSequenceEffect: View {

    /// 순서대로 보여줄 스프라이트 이름
    let spriteNames: [String]
    /// 각 프레임이 머무는 시간(초). `spriteNames` 와 길이가 같아야 한다.
    let durations: [TimeInterval]
    var size: CGFloat?
    /// 마지막 프레임까지 끝났을 때
    var onFinished: () -> Void = {}

    @Environment(SpriteLoader.self) private var loader
    @State private var index = 0

    var body: some View {
        // 크기를 주지 않으면 화면 전체를 덮는 이펙트다 — 원본이 `fillMaxSize()` +
        // `ContentScale.FillBounds` 라 비율을 무시하고 늘린다. 그래야 터지는 중심이 화면 가운데 온다.
        AnimatedSprite(sprite: loader.sprite(named: spriteNames[min(index, spriteNames.count - 1)]),
                       contentMode: size == nil ? nil : .fit)
            .frame(width: size, height: size)
            .task {
                for step in spriteNames.indices {
                    index = step
                    try? await Task.sleep(for: .seconds(durations[min(step, durations.count - 1)]))
                }
                onFinished()
            }
    }
}

/// 진화 안내 / 진화 연출
///
/// Android `EvolutionEffect.kt` 이식.
///
/// **두 상태가 한 컴포넌트에 들어 있다:**
/// - 진화 전 — 검은 막(0.6) 위에 "진화를 위해 / 화면을 터치해주세요." 안내. 누르면 시작.
/// - 진화 중 — 몽을 **정적 이미지로** 그리고 그 위에 이펙트 3장을 순서대로 덮는다.
///   몽을 정적으로 두는 건 연출 중에 몸통 애니메이션이 같이 움직이면 산만해서다.
struct EvolutionEffect: View {

    let mongResource: MongResourceCode
    let isEvolving: Bool
    /// 안내를 눌렀을 때
    var onStart: () -> Void = {}
    /// 연출이 끝났을 때 — 이때 서버에 진화를 요청한다
    var onFinished: () -> Void = {}

    @Environment(SpriteLoader.self) private var loader

    private static let frames = ["effect_evolution_1", "effect_evolution_2", "effect_evolution_3"]
    /// 원본 `DELAYS` 의 앞 3개. 네 번째(400ms)는 마지막 프레임 뒤 여유다.
    private static let durations: [TimeInterval] = [0.1, 0.3, 0.3]

    var body: some View {
        if isEvolving {
            ZStack {
                VStack {
                    Spacer(minLength: 0)
                    // 연출 중에는 몸통을 정적으로 둔다 (원본 `isPng = true`).
                    AnimatedSprite(sprite: loader.sprite(named: mongResource.pngName))
                        .frame(width: 120, height: 120)
                        .padding(.bottom, 25)
                }
                FrameSequenceEffect(
                    spriteNames: Self.frames,
                    durations: Self.durations,
                    onFinished: onFinished
                )
            }
            .task { await loader.preload(Self.frames + [mongResource.pngName]) }
        } else {
            ZStack {
                Color.black.opacity(0.6).ignoresSafeArea()
                VStack(spacing: 20) {
                    Text("진화를 위해")
                    Text("화면을 터치해주세요.")
                }
                .mongsFont(16)
                .foregroundStyle(MongsColor.white)
                .multilineTextAlignment(.center)
            }
            .contentShape(Rectangle())
            .onTapGesture(perform: onStart)
        }
    }
}

/// 졸업 연출
///
/// Android `GraduationEffect.kt` 이식 — 4프레임을 순서대로 보여준다.
/// 마지막 프레임(졸업 아이콘)은 2초간 머문다.
struct GraduationEffect: View {

    var onFinished: () -> Void = {}

    var body: some View {
        FrameSequenceEffect(
            spriteNames: ["effect_graduate_1", "effect_graduate_2", "effect_graduate_3", "icon_graduate"],
            durations: [0.3, 0.4, 0.5, 2.0],
            size: 145,
            onFinished: onFinished
        )
        // 원본은 화면 정중앙이다. watchOS 는 화면이 세로로 길고 몽이 아래에 붙어 있어서
        // 같은 "정중앙" 이 몽 몸통에 겹쳐 보인다. 몽 머리 위로 오도록 올린다.
        .offset(y: -30)
    }
}

/// 졸업 완료 표시
///
/// Android `GraduatedEffect.kt` 이식 — 연출이 끝난 뒤 화면 위에 남는 작은 배지.
struct GraduatedEffect: View {

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        VStack {
            AnimatedSprite(sprite: loader.sprite(named: "effect_graduate_3"))
                .frame(width: 35, height: 35)
                .padding(.top, 25)
            Spacer(minLength: 0)
        }
        .allowsHitTesting(false)
    }
}

/// 똥 치우기 연출
///
/// Android `PoopCleanEffect.kt` 이식 — 진공청소기 GIF 를 몽 위에 덮는다.
struct PoopCleanEffect: View {

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        VStack {
            Spacer(minLength: 0)
            AnimatedSprite(sprite: loader.sprite(named: "effect_vacuum"))
                .frame(width: 140, height: 140)
                .padding(.bottom, 23)
        }
        .allowsHitTesting(false)
    }
}

/// 상호작용 진행 표시
///
/// Android `LoadingEffect.kt` 이식 — 화면 위쪽의 작은 스피너.
struct SlotLoadingEffect: View {

    @Environment(SpriteLoader.self) private var loader

    var body: some View {
        VStack {
            AnimatedSprite(sprite: loader.sprite(named: "icon_loading"))
                .frame(width: 25, height: 25)
                .padding(.top, 25)
            Spacer(minLength: 0)
        }
        .allowsHitTesting(false)
    }
}
