package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.MongsNavy

/**
 * 맵 위에 텍스트와 게이지를 직접 올리면 맵마다 대비가 달라져 읽히지 않는다.
 * (밝은 점포 맵과 어두운 밤 맵의 차이가 크다)
 *
 * MenuRail 에는 쓰지 않는다. 원형 버튼이 이미 btn_bg_circle 배경을 갖고 있다.
 */
internal fun Modifier.mainPanel(): Modifier = this.background(
    color = MongsNavy.copy(alpha = MainDimens.PanelAlpha),
    shape = RoundedCornerShape(MainDimens.PanelRadius),
)
