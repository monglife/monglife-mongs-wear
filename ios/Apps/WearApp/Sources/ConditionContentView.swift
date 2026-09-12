import MongsModel
import MongsViewModel
import SwiftUI

/// 컨디션 쪽
///
/// Android `.../pages/main/ConditionContent.kt` 이식.
/// 2×2 원형 게이지이고, 바깥을 경험치 진행바가 감싼다.
/// 색 배정도 원본 그대로다 — 건강 분홍 / 포만감 노랑 / 힘 초록 / 피로 파랑.
struct ConditionContentView: View {

    let viewModel: MainSlotViewModel

    @Environment(SpriteLoader.self) private var loader

    private let icons = ["icon_healthy", "icon_satiety", "icon_strength", "icon_fatigue"]

    #if DEBUG
    /// 게이지를 눈으로 확인하기 위한 것 (Debug 전용).
    ///
    /// 경험치를 실제로 채우려면 먹이고 훈련시켜야 해서, 링이 한 바퀴 돌 때 모양이
    /// 맞는지 보려면 값을 직접 넣어야 한다.
    ///
    /// ```
    /// xcrun simctl launch <UDID> com.monglife.mongs.wear.ios -MongsPreviewProgress 100
    /// ```
    private var previewProgress: Double? {
        let value = UserDefaults.standard.double(forKey: "MongsPreviewProgress")
        return value > 0 ? value : nil
    }
    #endif

    /// 화면에 그릴 진행도. Debug 미리보기 값이 있으면 그걸 쓴다.
    private func progress(_ actual: Double) -> Double {
        #if DEBUG
        previewProgress ?? actual
        #else
        actual
        #endif
    }

    var body: some View {
        ZStack {
            if let mong = viewModel.mong {
                // 화면 테두리를 따라 도는 경험치 진행바 (원본 `ProgressIndicator`)
                EdgeProgressRing(progress: progress(mong.expRatio))

                VStack(spacing: 0) {
                    HStack(spacing: 0) {
                        ConditionGauge(iconName: "icon_healthy", progress: progress(mong.healthyRatio), color: MongsColor.pink)
                        ConditionGauge(iconName: "icon_satiety", progress: progress(mong.satietyRatio), color: MongsColor.yellow)
                    }
                    HStack(spacing: 0) {
                        ConditionGauge(iconName: "icon_strength", progress: progress(mong.strengthRatio), color: MongsColor.green)
                        ConditionGauge(iconName: "icon_fatigue", progress: progress(mong.fatigueRatio), color: MongsColor.blue)
                    }
                }
            } else {
                LoadingBar()
            }
        }
        // 원본은 fillMaxSize + Center 다. 명시하지 않으면 콘텐츠가 위로 몰린다.
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .task { await loader.preload(icons) }
    }
}
