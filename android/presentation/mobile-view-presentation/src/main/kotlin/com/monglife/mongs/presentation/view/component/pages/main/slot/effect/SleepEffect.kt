package com.monglife.mongs.presentation.view.component.pages.main.slot.effect

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.mongs.presentation.view.mobile.R

/**
 * wear 는 화면 상단(TopCenter + top 25dp)에 붙였다. 원형 화면에서는 그게 몽 머리 위 40dp 였다.
 * 가로 393dp 화면에서 그대로 두면 몽에서 200dp 넘게 떨어진 화면 꼭대기에 뜬다.
 * 지면선(MainDimens.AboveMong)을 기준으로 몽 머리 바로 위에 올린다.
 */
@Composable
internal fun SleepEffect(
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier
                .padding(bottom = MainDimens.AboveMong)
                .size(26.dp),
            painter = painterResource(R.drawable.icon_sleep),
            contentDescription = null
        )
    }
}