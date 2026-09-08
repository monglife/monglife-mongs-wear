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
    val MongSize = (120 * MONG_RATIO).dp   // 180.dp
    val GroundPadding = 20.dp              // 무대 바닥에서 몽 발까지

    /**
     * 머리 위 이펙트 기준선.
     *
     * 몽 정수리보다 살짝 아래다. wear 도 그랬다 — 192dp 화면에서 몽은 아래 25dp 에
     * 120dp 로 서고(정수리 145dp), 졸업 효과는 25dp 에서 35dp 를 차지해
     * 아래 끝이 132dp 였다. 즉 정수리보다 13dp 낮게 걸쳐 머리에 얹힌다.
     */
    val AboveMong = GroundPadding + MongSize - 15.dp   // 185.dp

    /** 원형 버튼 (CareBar / MenuRail / SystemBar 공용) */
    const val ActionSize = 56      // 우측 열 기능 버튼

    const val ICON_RATIO = 0.62f
    val ActionGap = 10.dp
    val ActionLabel = 11.sp

    /**
     * 우측 열 폭 — 버튼 2개가 붙어 들어갈 만큼만.
     *
     * 비율을 2 : 7 : 1 로 두면 1 파트가 80dp 인데 56dp 버튼 두 개와 간격은 122dp 라
     * 잘린다. 우측만 내용 폭으로 고정하고 나머지를 2 : 7 로 나누면
     * 결과는 약 1.9 : 6.6 : 1.5 로, 슬롯 폭은 2 : 7 : 1 일 때와 거의 같으면서
     * 어떤 화면 폭에서도 잘리지 않는다.
     */
    val RailPadding = 16.dp
    val RailWidth = (2 * ActionSize).dp + ActionGap + RailPadding * 2

    /**
     * 세로 예산.
     *
     * 실측(Pixel 9, 923 x 411dp)에서 safeDrawing 인셋이 위아래로 약 40dp 를 먹는다.
     * 상태바와 제스처바다.
     *
     * 하단 바를 화면 폭 전체로 두면 가운데가 비어 낭비였다. 우측 열을 세로로 접어
     * 위는 기능 버튼, 아래는 시스템 버튼으로 두고, 스텟과 슬롯이 전체 높이를 쓴다.
     *
     * 인셋은 추정하지 말고 재야 한다. Pixel 9 에서 실측한 값은 위아래 합쳐 78dp 로,
     * 처음 가정했던 40dp 의 두 배였다. 그것 때문에 우측 마지막 행 라벨이 잘렸다.
     *
     *   사용 가능    411 - 78(실측 인셋) = 333
     *   밴드         333 - 12(패딩) - 60(상단바) - 8(간격) = 253
     *   우측 열      2 x 79(기능 2행) + 8 + 79(하단바) = 245   여유 8
     *   스텟         30(레벨) + 2 x 17(구분선) + 4 x 36 + 30(페이) + 24(패딩) = 262
     *   무대         38(이름) + 6 + 167(AboveMong) + 47(졸업 효과) = 258
     *
     * 전체 화면이라 실제 인셋은 컷아웃뿐이지만, 바가 잠깐 나타날 때를 대비해
     * safeDrawing 패딩은 그대로 두고 위 예산도 인셋이 있다고 보고 잡았다.
     */
    val RailRowGap = 8.dp
    val GaugeGap = 24.dp

    /** 상단바 */
    val TopBarHeight = 60.dp
    val ChipHeight = 40.dp
    val ChipGap = 10.dp
    /**
      * 칩 폭은 dp 로 고정하지 않는다. 상단바에서 차지할 비율만 정해 두면
      * 화면 폭이 달라져도 같이 늘고 줄어든다.
      *
      * 스타포인트와 걸음 수가 1 씩, 남는 자리가 3 이다. 두 칩이 같은 폭을 갖고
      * 네 자리 숫자도 들어간다.
      *
      * 주의: 칩 안의 Text 에 weight 를 주면서 칩 자체의 최대 폭을 열어 두면
      * 칩이 상단바 전체로 늘어난다. 실제로 걸음 수와 도움말/설정이 밀려났었다.
      */
    const val CHIP_WEIGHT = 1f
    const val CHIP_SPACE_WEIGHT = 3f
    const val TopIconSize = 30
    val TopIconGap = 22.dp

    /** 밴드 */
    val BandGap = 20.dp
    val PanelRadius = 16.dp

    /** 맵 위 텍스트 대비 확보용 패널 스크림 알파 */
    const val PanelAlpha = 0.45f
    const val ScrimAlpha = 0.25f
}
