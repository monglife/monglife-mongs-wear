package com.monglife.mongs.presentation.view.dialog.pages.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.mongs.presentation.view.mobile.R

/**
 * 첫 진입 안내.
 *
 * wear 는 아이콘과 두 줄 문구, 버튼 둘을 세로로 쌓았다. 원형 화면에서는 그 방법뿐이었지만
 * 가로에서는 아이콘을 왼쪽에 두고 문구와 버튼을 오른쪽에 모으는 편이 낫다.
 *
 * 몽 발밑의 탭 어포던스 고리와 역할이 겹치지만, 이 다이얼로그가 있어야
 * MainSlotViewModel 의 InitNotification 상태를 벗어나면서 "다시 보지 않기" 가 저장된다.
 */
@Composable
internal fun InitNotificationDialog(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit,
    onCloseForeverClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCloseClick,
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.btn_icon_notice),
                contentDescription = null,
                modifier = Modifier.size(44.dp),
            )

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "몽을 클릭하면 상호작용 메뉴가 열려요",
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 18.sp,
                    color = MongsWhite,
                    maxLines = 1,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BlueButton(
                        text = "그만보기",
                        width = 120,
                        height = 42,
                        fontSize = 15,
                        onClick = onCloseForeverClick,
                    )
                    BlueButton(
                        text = "닫기",
                        width = 96,
                        height = 42,
                        fontSize = 15,
                        onClick = onCloseClick,
                    )
                }
            }
        }
    }
}
