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
    const val MONG_RATIO = 1.5f
    val MongSize = (120 * MONG_RATIO).dp            // 180.dp
    val GroundPadding = 24.dp                       // 무대 바닥에서 몽 발까지
    val AboveMong = GroundPadding + MongSize + 10.dp // 214.dp — 머리 위 이펙트 기준선

    /** 원형 버튼 (CareBar / MenuRail / SystemBar 공용) */
    const val ActionSize = 56
    const val SystemSize = 48
    val ActionGap = 10.dp
    val ActionLabel = 11.sp

    /** 밴드 */
    val HudHeight = 44.dp
    val BottomHeight = 84.dp
    val BottomPadding = 20.dp
    val PanelWidth = 200.dp
    val RailWidth = 188.dp
    val BandGap = 20.dp
    val PanelRadius = 16.dp

    /** 맵 위 텍스트 대비 확보용 패널 스크림 알파 */
    const val PanelAlpha = 0.45f
    const val ScrimAlpha = 0.25f
}
