package com.monglife.mongs.presentation.view.pages.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.button.BlueButton

/**
 * 아직 이식하지 않은 화면의 자리표시자.
 *
 * 이게 없으면 등록되지 않은 라우트로 navigate 하는 순간 IllegalArgumentException 이다.
 * 호출부를 wear 와 동일하게 유지하기 위해 navigate 를 감싸는 대신 목적지를 등록한다.
 * 실제 화면이 들어오면 해당 항목만 지우면 된다.
 */
@Composable
internal fun NotReadyView(
    navController: NavController,
    title: String,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        DefaultBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = title,
                fontFamily = DAL_MU_RI,
                fontSize = 22.sp,
                color = MongsWhite,
            )
            Text(
                text = "준비 중이에요",
                fontFamily = DAL_MU_RI,
                fontSize = 16.sp,
                color = MongsWhite.copy(alpha = 0.7f),
            )
            BlueButton(
                text = "돌아가기",
                width = 120,
                height = 42,
                fontSize = 15,
                onClick = { navController.popBackStack() },
            )
        }
    }
}
