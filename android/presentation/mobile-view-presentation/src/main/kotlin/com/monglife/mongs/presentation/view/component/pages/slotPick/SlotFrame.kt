package com.monglife.mongs.presentation.view.component.pages.slotPick

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monglife.mongs.presentation.view.assets.SlotPickDimens

/**
 * 슬롯 카드 세 종류(보유/빈/구매)가 공유하는 골격.
 *
 * wear 는 셋 다 0.2 / 0.52 / 0.28 weight 컬럼을 각자 복사해 갖고 있었다.
 * 가로에서는 카드 높이가 고정이라 weight 대신 이름표/액션을 고정으로 잡고
 * 아트가 남는 높이를 갖는 편이 예측 가능하다.
 */
@Composable
internal fun SlotFrame(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    art: @Composable () -> Unit,
    action: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(SlotPickDimens.RowGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().height(SlotPickDimens.NamePlateHeight),
        ) { header() }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) { art() }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().height(SlotPickDimens.ActionHeight),
        ) { action() }
    }
}
