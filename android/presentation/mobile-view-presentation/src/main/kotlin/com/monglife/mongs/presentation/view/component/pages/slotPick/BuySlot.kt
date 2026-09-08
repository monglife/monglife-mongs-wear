package com.monglife.mongs.presentation.view.component.pages.slotPick

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.SlotPickDimens
import com.monglife.mongs.presentation.view.component.common.button.YellowButton
import com.monglife.mongs.presentation.view.component.common.textbox.StarPointBox
import com.mongs.presentation.view.mobile.R

/** 슬롯 구매 카드. 가격은 wear 와 마찬가지로 호출부가 넘긴다. */
@Composable
internal fun BuySlot(
    modifier: Modifier = Modifier,
    starPoint: Int,
    buySlotPrice: Int,
    buySlotDialogOpen: () -> Unit,
) {
    SlotFrame(
        modifier = modifier,
        header = { StarPointBox(starPoint = starPoint) },
        art = {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.mong_shadow),
                    contentDescription = null,
                    modifier = Modifier
                        .offset(y = 34.dp)
                        .width(120.dp)
                        .height(30.dp)
                        .zIndex(1f),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth().zIndex(2f),
                ) {
                    Image(
                        painter = painterResource(R.drawable.point_icon_star),
                        contentDescription = null,
                        modifier = Modifier.size(38.dp),
                    )
                    Text(
                        text = "-",
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 26.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                    Text(
                        text = "$buySlotPrice",
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 26.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
            }
        },
        action = {
            YellowButton(
                text = "슬롯구매",
                height = 48,
                width = SlotPickDimens.ActionWidth + 30,
                fontSize = 15,
                onClick = buySlotDialogOpen,
                disable = starPoint < buySlotPrice,
            )
        },
    )
}
