package com.monglife.mongs.presentation.view.component.pages.slotPick

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongResourceCode
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.charactor.Mong
import com.mongs.wear.presentation.view.wear.R


@Composable
internal fun Slot(
    modifier: Modifier = Modifier,
    currentMongId: Long?,
    mongVo: MongVo,
    detailDialogOpen: () -> Unit,
    graduateDialogOpen: () -> Unit,
    deleteDialogOpen: () -> Unit,
    pickDialogOpen: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxHeight()
    ) {
        Spacer(modifier = Modifier.height(15.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f)
        ) {
            Text(
                text = mongVo.name,
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = MongsWhite,
                maxLines = 1,
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.52f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (mongVo.stateCode == MongStateCode.DEAD) {
                    Image(
                        modifier = Modifier
                            .padding(bottom = 25.dp)
                            .size(130.dp),
                        painter = painterResource(R.drawable.mong_rip),
                        contentDescription = null
                    )
                } else {
                    Mong(
                        isPng = true,
                        mong = MongResourceCode.getResource(mongVo.mongCode),
                        onClick = detailDialogOpen,
                        ratio = 0.65f,
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.28f)
        ) {
            if (mongVo.stateCode == MongStateCode.GRADUATE_READY) {
                BlueButton(
                    text = "졸업",
                    height = 32,
                    width = 55,
                    onClick = graduateDialogOpen,
                )
            } else {
                BlueButton(
                    text = "삭제",
                    height = 32,
                    width = 55,
                    onClick = deleteDialogOpen,
                )
            }

            if (mongVo.stateCode !in listOf(MongStateCode.DEAD, MongStateCode.DELETE, MongStateCode.GRADUATE)) {

                Spacer(modifier = Modifier.width(5.dp))

                BlueButton(
                    text = "선택",
                    height = 32,
                    width = 55,
                    onClick = pickDialogOpen,
                    disable = mongVo.mongId == currentMongId,
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))
    }
}