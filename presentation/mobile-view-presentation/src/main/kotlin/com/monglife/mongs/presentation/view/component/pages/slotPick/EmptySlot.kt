package com.monglife.mongs.presentation.view.component.pages.slotPick

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.SlotPickDimens
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.mongs.presentation.view.mobile.R

/** 비어 있는 슬롯. */
@Composable
internal fun EmptySlot(
    modifier: Modifier = Modifier,
    createDialogOpen: () -> Unit,
) {
    SlotFrame(
        modifier = modifier,
        header = {
            Text(
                text = "NEW MONG",
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 22.sp,
                color = MongsWhite,
                maxLines = 1,
            )
        },
        art = {
            Image(
                painter = painterResource(R.drawable.mong_body_blind),
                contentDescription = null,
                modifier = Modifier.size(132.dp),
            )
            Text(
                text = "?",
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 40.sp,
                color = MongsWhite,
                maxLines = 1,
            )
        },
        action = {
            BlueButton(
                text = "생성",
                height = 48,
                width = SlotPickDimens.ActionWidth,
                fontSize = 15,
                onClick = createDialogOpen,
            )
        },
    )
}
