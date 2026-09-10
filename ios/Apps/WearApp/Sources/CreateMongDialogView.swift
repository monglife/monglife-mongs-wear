import SwiftUI

/// 몽 생성 다이얼로그
///
/// Android `dialog/pages/slotPick/CreateSlotDialog.kt` 이식.
/// 3단계로 넘어가며 입력받는다 — 이름 → 취침 시각 → 기상 시각, 마지막에 생성.
///
/// **입력 방식만 원본과 다르다.** Android 는 `ScalingLazyColumn` 을 스크롤 휠처럼 써서
/// 시/분을 고르는데, watchOS 는 `Picker` 가 디지털 크라운 입력을 공짜로 준다.
/// 억지로 원본을 흉내내면 오히려 쓰기 나쁜 화면이 된다.
struct CreateMongDialogView: View {

    let onCreate: (String, Date, Date) -> Void
    let onCancel: () -> Void

    private enum Step: Int, CaseIterable {
        case name, sleep, wakeup
    }

    @State private var step: Step = .name
    @State private var name = ""
    /// 원본 기본값 — 취침 22:00, 기상 08:00
    @State private var sleepHour = 22
    @State private var sleepMinute = 0
    @State private var wakeupHour = 8
    @State private var wakeupMinute = 0

    /// 원본이 6자를 넘으면 토스트로 막는다.
    private static let nameLimit = 6

    private var isNameValid: Bool {
        let trimmed = name.trimmingCharacters(in: .whitespaces)
        return !trimmed.isEmpty && trimmed.count <= Self.nameLimit
    }

    var body: some View {
        ZStack {
            Color.black.opacity(0.95).ignoresSafeArea()

            VStack(spacing: 6.ms) {
                stepIndicator

                Group {
                    switch step {
                    case .name: nameStep
                    case .sleep: timeStep(hour: $sleepHour, minute: $sleepMinute, title: "취침")
                    case .wakeup: timeStep(hour: $wakeupHour, minute: $wakeupMinute, title: "기상")
                    }
                }
                .frame(maxHeight: .infinity)

                footer
            }
            .padding(.vertical, 8.ms)
        }
    }

    // MARK: - 단계 표시

    private var stepIndicator: some View {
        HStack(spacing: 6.ms) {
            ForEach(Step.allCases, id: \.rawValue) { item in
                Text(label(for: item))
                    .mongsFont(11)
                    .foregroundStyle(item == step ? MongsColor.lightGray : MongsColor.darkGray)
            }
        }
    }

    private func label(for step: Step) -> String {
        switch step {
        case .name: "이름"
        case .sleep: "취침"
        case .wakeup: "기상"
        }
    }

    // MARK: - 단계별 입력

    private var nameStep: some View {
        VStack(spacing: 4.ms) {
            TextField("이름", text: $name)
                .mongsFont(16)
                .multilineTextAlignment(.center)
                // 6자를 넘으면 자른다. 원본은 토스트로 알리지만 시계에서는 애초에 못 넘기는 편이 낫다.
                .onChange(of: name) { _, value in
                    if value.count > Self.nameLimit {
                        name = String(value.prefix(Self.nameLimit))
                    }
                }
            Text("최대 \(Self.nameLimit)자")
                .mongsFont(9)
                .foregroundStyle(MongsColor.darkGray)
        }
    }

    private func timeStep(hour: Binding<Int>, minute: Binding<Int>, title: String) -> some View {
        VStack(spacing: 2.ms) {
            Text("\(title) 시각")
                .mongsFont(11)
                .foregroundStyle(MongsColor.lightGray)

            HStack(spacing: 4.ms) {
                // 크라운으로 돌린다 — Android 의 스크롤 휠 자리다.
                Picker("", selection: hour) {
                    ForEach(0 ..< 24, id: \.self) { Text(String(format: "%02d", $0)).mongsFont(18) }
                }
                .frame(width: 60.ms)

                Text(":").mongsFont(18).foregroundStyle(MongsColor.white)

                Picker("", selection: minute) {
                    ForEach(0 ..< 60, id: \.self) { Text(String(format: "%02d", $0)).mongsFont(18) }
                }
                .frame(width: 60.ms)
            }
            .labelsHidden()
        }
    }

    // MARK: - 이동 / 확정

    private var footer: some View {
        HStack(spacing: 5.ms) {
            MongsButton(title: step == .name ? "닫기" : "이전", style: .blue, width: 58) {
                if let previous = Step(rawValue: step.rawValue - 1) {
                    step = previous
                } else {
                    onCancel()
                }
            }

            if step == .wakeup {
                MongsButton(title: "생성", width: 58, isEnabled: isNameValid) {
                    onCreate(
                        name.trimmingCharacters(in: .whitespaces),
                        time(hour: sleepHour, minute: sleepMinute),
                        time(hour: wakeupHour, minute: wakeupMinute)
                    )
                }
            } else {
                MongsButton(
                    title: "다음", width: 58,
                    isEnabled: step != .name || isNameValid
                ) {
                    if let next = Step(rawValue: step.rawValue + 1) { step = next }
                }
            }
        }
    }

    /// 서버는 타임존 없는 시각(`HH:mm:ss`)을 받는다.
    /// 오늘 날짜의 그 시각으로 만들면 `MongsCoding` 이 같은 형식으로 직렬화한다.
    private func time(hour: Int, minute: Int) -> Date {
        var components = Calendar.current.dateComponents([.year, .month, .day], from: .now)
        components.hour = hour
        components.minute = minute
        components.second = 0
        return Calendar.current.date(from: components) ?? .now
    }
}
