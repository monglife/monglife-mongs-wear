import SwiftUI

/// 아직 이식하지 않은 화면의 자리표시자
///
/// Android `mobile-view-presentation/.../pages/common/NotReadyView.kt` 이식.
/// 모바일 쪽이 같은 이유로 먼저 만들어 뒀고, 문구와 구성을 그대로 가져왔다.
///
/// 원본은 등록되지 않은 라우트로 navigate 하면 `IllegalArgumentException` 이라 필요했다.
/// 여기서는 크래시가 나지는 않지만 **버튼이 눌러도 아무 반응이 없어 고장난 것처럼 보인다.**
/// 실제 화면이 들어오면 `NotReadyDestination` 에서 해당 항목만 지운다.
struct NotReadyView: View {

    let destination: NotReadyDestination
    let onClose: () -> Void

    var body: some View {
        ZStack {
            DefaultBackground()

            VStack(spacing: 12.ms) {
                Text(destination.title)
                    .mongsFont(22)
                    .foregroundStyle(MongsColor.white)
                    .lineLimit(1)

                Text("준비 중이에요")
                    .mongsFont(16)
                    .foregroundStyle(MongsColor.white.opacity(0.7))
                    .lineLimit(1)

                MongsButton(
                    title: "돌아가기", style: .blue,
                    width: 120, height: 42, fontSize: 15,
                    action: onClose
                )
            }
        }
    }
}

/// 자리표시자로 보낼 화면들
///
/// Android `Router.kt` 가 라우트-제목 쌍 목록으로 들고 있는 것과 같다.
/// v1 범위 밖이라 아직 화면이 없는 것들만 있다 — 이식하면 케이스를 지운다.
enum NotReadyDestination: String, Identifiable, CaseIterable {

    case collection
    case searchMap
    case randomDraw
    case training
    case battle
    case help
    case charge
    case notice
    case feedback

    var id: String { rawValue }

    /// 문구는 Android `Router.kt` 의 목록 그대로다.
    var title: String {
        switch self {
        case .collection: "도감"
        case .searchMap: "맵 탐색"
        case .randomDraw: "랜덤 뽑기"
        case .training: "훈련"
        case .battle: "배틀"
        case .help: "도움말"
        case .charge: "충전"
        case .notice: "공지사항"
        case .feedback: "오류 신고"
        }
    }
}
