package com.monglife.mongs.presentation.view.component.pages.main.slot.section

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import com.monglife.mongs.presentation.view.assets.MongResourceCode
import com.monglife.mongs.presentation.view.component.common.charactor.Mong

@Composable
internal fun NormalSection(
    modifier: Modifier = Modifier,
    mongCode: String,
    statusCode: MongStatusCode,
    isSleep: Boolean,
    isHappy: Boolean,
    isEating: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier.fillMaxSize(),
    ) {
        Mong(
            state = statusCode,
            isHappy = isHappy,
            isEating = isEating,
            isSleep = isSleep,
            mong = MongResourceCode.getResource(mongCode),
            onClick = onClick,
            modifier = Modifier
                .padding(bottom = 22.dp)
                .zIndex(1f)
        )
    }
}