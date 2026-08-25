package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite

/**
 * 슬롯 상단 이름표.
 *
 * name 은 사용자가 지은 이름, mongName 은 종족 이름이다. 둘 다 MongVo 에 있는데
 * wear 에서는 어느 화면에도 나오지 않았다. 가로에서는 몽 위가 비어 있어 자리가 난다.
 */
@Composable
internal fun SlotHeader(
    modifier: Modifier = Modifier,
    currentMongVo: MongVo?,
) {
    currentMongVo ?: return

    Row(
        modifier = modifier
            .background(
                color = Color.Black.copy(alpha = 0.32f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 18.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = currentMongVo.name,
            fontFamily = DAL_MU_RI,
            fontSize = 18.sp,
            color = MongsWhite,
            maxLines = 1,
        )
        Text(
            text = currentMongVo.mongName,
            fontFamily = DAL_MU_RI,
            fontSize = 13.sp,
            color = MongsWhite.copy(alpha = 0.6f),
            maxLines = 1,
        )
    }
}
