package com.monglife.mongs.presentation.view.component.pages.main.slot.section

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.mongs.presentation.view.mobile.R

/**
 * 빈 슬롯.
 *
 * wear 는 0.85 / 0.15 weight 컬럼으로 원형 화면 안에 밀어 넣었지만,
 * 가로에서는 지면선 기준으로 쌓는 편이 다른 상태들과 정렬이 맞는다.
 */
@Composable
internal fun EmptySection(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(bottom = MainDimens.GroundPadding),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.mong_body_blind),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                )
                Text(
                    text = "!",
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 34.sp,
                    color = MongsWhite,
                )
            }

            BlueButton(
                text = "슬롯 선택",
                width = 130,
                height = 42,
                fontSize = 15,
                onClick = onClick,
            )
        }
    }
}
