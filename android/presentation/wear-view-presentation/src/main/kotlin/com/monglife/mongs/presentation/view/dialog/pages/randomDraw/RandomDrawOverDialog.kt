package com.monglife.mongs.presentation.view.dialog.pages.randomDraw

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.monglife.mongs.application.mong.vo.RandomDrawVo
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.FoodResourceCode
import com.monglife.mongs.presentation.view.assets.MapResourceCode
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.SnackResourceCode
import com.monglife.mongs.presentation.view.component.common.button.BlueButton

@Composable
fun RandomDrawOverDialog(
    modifier: Modifier = Modifier,
    randomDrawVo: RandomDrawVo,
    onEndClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color = Color.Black.copy(alpha = 0.95f))
            .fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) {
                val resourceCode = when (randomDrawVo.inventoryTypeCode) {
                    InventoryTypeCode.FOOD -> FoodResourceCode.getResourceCode(randomDrawVo.randomDrawCode)
                    InventoryTypeCode.SNACK -> SnackResourceCode.getResourceCode(randomDrawVo.randomDrawCode)
                    InventoryTypeCode.MAP -> MapResourceCode.getResourceCode(randomDrawVo.randomDrawCode)
                }

                Image(
                    painter = painterResource(resourceCode),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
            ) {
                Text(
                    text = randomDrawVo.randomDrawName,
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    color = MongsWhite,
                    maxLines = 1,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.2f)
            ) {
                BlueButton(
                    text = "닫기",
                    width = 80,
                    onClick = onEndClick
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}