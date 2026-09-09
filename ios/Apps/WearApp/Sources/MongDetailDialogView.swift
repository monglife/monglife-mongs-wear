import MongsModel
import SwiftUI

/// 몽 상세 정보 다이얼로그
///
/// Android `dialog/pages/slotPick/SlotDetailDialog.kt` 이식 (축약).
/// 슬롯에서 몽을 누르면 열린다. 아무 곳이나 누르면 닫힌다.
struct MongDetailDialogView: View {

    let mong: Mong
    let onClose: () -> Void

    private static let bornFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "yyyy.MM.dd"
        return formatter
    }()

    var body: some View {
        ZStack {
            Color.black.opacity(0.95)
                .ignoresSafeArea()
                .onTapGesture(perform: onClose)

            ScrollView {
                VStack(spacing: 5) {
                    Text(mong.name)
                        .mongsFont(15)
                        .foregroundStyle(MongsColor.white)

                    Text("Lv.\(mong.level) · \(mong.statusCode.message)")
                        .mongsFont(10)
                        .foregroundStyle(MongsColor.lightGray)

                    if mong.stateCode != .normal {
                        Text(mong.stateCode.message)
                            .mongsFont(10)
                            .foregroundStyle(MongsColor.pink)
                    }

                    VStack(spacing: 2) {
                        row("건강", "\(Int(mong.healthyRatio))")
                        row("포만감", "\(Int(mong.satietyRatio))")
                        row("힘", "\(Int(mong.strengthRatio))")
                        row("피로", "\(Int(mong.fatigueRatio))")
                        row("무게", String(format: "%.1fkg", mong.weight))
                        row("페이포인트", "\(mong.payPoint)")
                        row("생일", Self.bornFormatter.string(from: mong.createdAt))
                    }
                    .padding(.top, 4)

                    MongsButton(title: "닫기", style: .blue, action: onClose)
                        .padding(.top, 6)
                }
                .padding(.vertical, 10)
            }
        }
    }

    private func row(_ label: String, _ value: String) -> some View {
        HStack {
            Text(label)
                .foregroundStyle(MongsColor.darkGray)
            Spacer(minLength: 8)
            Text(value)
                .foregroundStyle(MongsColor.white)
        }
        .mongsFont(11)
        .padding(.horizontal, 18)
    }
}
