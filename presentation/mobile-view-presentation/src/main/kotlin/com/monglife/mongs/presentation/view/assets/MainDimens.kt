package com.monglife.mongs.presentation.view.assets

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 메인 화면 좌표계의 단일 출처.
 *
 * 가로 고정(851 x 393dp, 2340x1080 @2.75 기준)에 맞춰 wear 값을 1.5배 한 것이다.
 * 방향이 고정이라 대응할 비율이 하나뿐이므로 런타임 배율 대신 상수로 적는다.
 */
internal object MainDimens {

    /**
     * 몽 스테이지.
     *
     * 이펙트(하트/Z/똥/진화)는 전부 [GroundPadding] 의 지면선을 원점으로 위로 올린다.
     * wear 처럼 화면 상단(TopCenter + padding)에 붙이면 세로 393dp 에서
     * 몽과 200dp 넘게 떨어져 무관한 위치에 뜬다.
     */
    const val MONG_RATIO = 1.25f
    val MongSize = (120 * MONG_RATIO).dp   // 150.dp
    val GroundPadding = 20.dp              // 무대 바닥에서 몽 발까지

    /**
     * 머리 위 이펙트 기준선.
     *
     * 몽 정수리보다 살짝 아래다. wear 도 그랬다 — 192dp 화면에서 몽은 아래 25dp 에
     * 120dp 로 서고(정수리 145dp), 졸업 효과는 25dp 에서 35dp 를 차지해
     * 아래 끝이 132dp 였다. 즉 정수리보다 13dp 낮게 걸쳐 머리에 얹힌다.
     */
    val AboveMong = GroundPadding + MongSize - 15.dp   // 155.dp

    /** 원형 버튼 (CareBar / MenuRail / SystemBar 공용) */
    const val ActionSize = 56
    const val SystemSize = 48
    const val ICON_RATIO = 0.62f
    val ActionGap = 10.dp
    val ActionLabel = 11.sp

    /**
     * 세로 예산.
     *
     * 실측(Pixel 9, 923 x 411dp)에서 safeDrawing 인셋이 위아래로 약 40dp 를 먹는다.
     * 상태바와 제스처바다. 처음엔 이걸 빼먹어서 컨디션 게이지 4번째, 레일 3번째 행,
     * 빈 슬롯 버튼이 잘렸다.
     *
     *   사용 가능    411 - 40(인셋) = 371
     *   밴드         371 - 12(패딩) - 40(HUD) - 16(간격) - 76(하단) = 227
     *   컨디션       4 x (20 + 4 + 8) + 3 x 8 + 20 = 172   여유 55
     *   레일 2행     2 x (56 + 4 + 11) + 8 = 150           여유 77
     *   무대         20 + 150 + 44(졸업 효과) = 214        여유 13
     *
     * 레일은 3열 x 3행에서 4열 x 2행으로 바꿨다. 3행이면 225dp 라
     * 227 에 2dp 만 남아 반올림 한 번에 잘린다. 가로는 923dp 라 여유가 많다.
     */
    val RailRowGap = 8.dp
    val GaugeGap = 8.dp

    /** 밴드 */
    val HudHeight = 40.dp
    val BottomHeight = 76.dp
    val BottomPadding = 20.dp
    val PanelWidth = 200.dp
    val RailWidth = 254.dp   // 4열 x 56 + 3 x 10
    val BandGap = 20.dp
    val PanelRadius = 16.dp

    /** 맵 위 텍스트 대비 확보용 패널 스크림 알파 */
    const val PanelAlpha = 0.45f
    const val ScrimAlpha = 0.25f
}
