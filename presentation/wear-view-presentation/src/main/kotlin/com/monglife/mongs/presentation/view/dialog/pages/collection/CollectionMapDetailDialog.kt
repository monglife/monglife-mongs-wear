package com.monglife.mongs.presentation.view.dialog.pages.collection

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.monglife.mongs.application.member.collection.vo.CollectionMapVo
import com.monglife.mongs.presentation.view.assets.MapResourceCode
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog


@Composable
internal fun CollectionMapDetailDialog(
    modifier: Modifier = Modifier,
    collectionMapVo: CollectionMapVo,
    onSetClick: (String) -> Unit,
    onClick: () -> Unit,
) {
    val confirmDialog = remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            Image(
                painter = painterResource(MapResourceCode.getResourceCode(collectionMapVo.code)),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }

        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
                .zIndex(2f)
        ) {
            BlueButton(
                text = "배경 설정",
                width = 100,
                onClick = { confirmDialog.value = true }
            )
        }

        if (confirmDialog.value) {
            ConfirmAndCancelDialog(
                text = "배경화면으로\n설정하시겠습니까?",
                confirm = { onSetClick(collectionMapVo.code) },
                cancel = { confirmDialog.value = false },
                modifier = Modifier.zIndex(3f)
            )
        }
    }
}
