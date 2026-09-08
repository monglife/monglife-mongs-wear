package com.monglife.mongs.presentation.view.assets

import androidx.compose.ui.unit.dp

/**
 * 슬롯 선택 화면 좌표계.
 *
 * MainDimens 에 넣지 않는다 - 그쪽 KDoc 이 "메인 화면 좌표계의 단일 출처" 로 범위를 못박고 있다.
 *
 * 세로 예산 (Pixel 9 가로, safeDrawing + 20/14 패딩 후 880 x 383dp):
 *   밴드   383 - 60(상단바) - 24(인디케이터) - 2 x 12(간격) = 275
 *   카드   275 - 2 x 12(카드 세로 패딩)                     = 251
 *   내부   251 - 44(이름표) - 48(액션) - 2 x 10(간격)       = 139   <- 아트
 *
 * 가로: 화살표 좌우 여백을 빼고 카드 520dp. 컷아웃이 54dp 를 먹는 최악에서도 남는다.
 */
internal object SlotPickDimens {

    val BandGap = 12.dp
    val IndicatorHeight = 24.dp

    val CardWidth = 520.dp
    val CardPaddingH = 20.dp
    val CardPaddingV = 12.dp

    val NamePlateHeight = 44.dp
    val ActionHeight = 48.dp
    const val ActionWidth = 104
    val RowGap = 10.dp

    /** 아트 영역이 139dp 라 Mong 기본 120dp 를 1.1배까지만 키운다 (132dp, 여유 7dp) */
    const val MONG_RATIO = 1.1f

    const val ArrowHeight = 64
    const val ArrowWidth = 34

    val Dot = 10.dp
    val DotGap = 8.dp
}
