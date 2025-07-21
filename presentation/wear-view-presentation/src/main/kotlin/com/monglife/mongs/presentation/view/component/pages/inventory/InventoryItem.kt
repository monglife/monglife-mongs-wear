package com.monglife.mongs.presentation.view.component.pages.inventory

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.monglife.mongs.application.mong.vo.InventoryVo
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.FoodResourceCode
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.SnackResourceCode

@Composable
fun InventoryItem(
    modifier: Modifier = Modifier,
    height: Int = 40,
    width: Int = 40,
    inventoryVo: InventoryVo?,
    onClick: (InventoryVo) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(2.dp)
            .size(height = height.dp, width = width.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color = Color.LightGray)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { inventoryVo?.let(onClick) },
            )
    ) {
        val resourceCode = when (inventoryVo?.inventoryTypeCode) {
            InventoryTypeCode.FOOD -> FoodResourceCode.getResourceCode(inventoryVo.inventoryCode)
            InventoryTypeCode.SNACK -> SnackResourceCode.getResourceCode(inventoryVo.inventoryCode)
            else -> null
        }

        resourceCode?.let {
            Image(
                painter = painterResource(it),
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp),
            )
        } ?: run {
            Text(
                text = "?",
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = MongsWhite,
            )
        }
    }
}