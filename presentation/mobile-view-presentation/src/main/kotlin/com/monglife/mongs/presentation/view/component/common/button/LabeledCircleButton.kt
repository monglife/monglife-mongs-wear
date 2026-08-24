package com.monglife.mongs.presentation.view.component.common.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.MongsWhite

/**
 * 아이콘 원형 버튼 + 아래 라벨.
 *
 * 가로 화면에서는 버튼을 한 줄로 펴게 되는데, 픽셀 글리프만 늘어놓으면
 * 무엇인지 알 수 없다. wear 의 피라미드 배치는 최소한 기능별로 묶여 있었다.
 */
@Composable
internal fun LabeledCircleButton(
    modifier: Modifier = Modifier,
    icon: Int,
    border: Int,
    label: String,
    size: Int = MainDimens.ActionSize,
    disable: Boolean = false,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CircleImageButton(
            icon = icon,
            border = border,
            size = size,
            disable = disable,
            onClick = onClick,
        )
        Text(
            text = label,
            fontFamily = DAL_MU_RI,
            fontSize = MainDimens.ActionLabel,
            lineHeight = MainDimens.ActionLabel,
            color = MongsWhite.copy(alpha = if (disable) 0.5f else 1f),
            maxLines = 1,
        )
    }
}
