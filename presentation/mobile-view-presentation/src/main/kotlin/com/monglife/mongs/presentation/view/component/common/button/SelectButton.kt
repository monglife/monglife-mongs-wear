package com.monglife.mongs.presentation.view.component.common.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SelectButton(
    modifier: Modifier = Modifier,
    leftBtnDisabled: Boolean = false,
    rightBtnDisabled: Boolean = false,
    leftBtnClick: () -> Unit,
    rightBtnClick: () -> Unit,
    // wear 기본값은 18x35dp 다. 워치에서는 통했지만 폰에서는 터치 영역이 너무 작다.
    btnWidth: Int = 18,
    btnHeight: Int = 35,
    content: @Composable () -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {

                Spacer(modifier = Modifier.width(15.dp))

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.15f)
                ) {
                    if (!leftBtnDisabled) {
                        LeftButton(width = btnWidth, height = btnHeight, onClick = leftBtnClick)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.7f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        // 호출자의 modifier 를 여기서 또 적용하면 패딩/크기가 두 번 걸린다.
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        content()
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.15f)
                ) {
                    if (!rightBtnDisabled) {
                        RightButton(width = btnWidth, height = btnHeight, onClick = rightBtnClick)
                    }
                }

                Spacer(modifier = Modifier.width(15.dp))
            }
        }
    }
}