package com.monglife.mongs.presentation.view.component.pages.slotPick

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongResourceCode
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.SlotPickDimens
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.charactor.Mong
import com.mongs.presentation.view.mobile.R

/**
 * 몽이 들어 있는 슬롯.
 *
 * wear 는 몽 스프라이트를 눌러야만 상세가 열렸다. 워치에서는 그게 유일한 방법이었지만
 * 폰에서는 그걸 알 길이 없어서 `정보` 버튼을 추가한다. 스프라이트 탭도 그대로 둔다.
 *
 * isPng = true 유지: 로스터 화면이라 정지 이미지가 맞고, GIF 로 바꾸면
 * statusCode / isSleep 을 여기까지 배선해야 한다.
 */
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
    SlotFrame(
        modifier = modifier,
        header = {
            Text(
                text = mongVo.name,
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 20.sp,
                color = MongsWhite,
                maxLines = 1,
            )
        },
        art = {
            if (mongVo.stateCode == MongStateCode.DEAD) {
                Image(
                    painter = painterResource(R.drawable.mong_rip),
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 12.dp).size(150.dp),
                )
            } else {
                Mong(
                    isPng = true,
                    mong = MongResourceCode.getResource(mongVo.mongCode),
                    onClick = detailDialogOpen,
                    ratio = SlotPickDimens.MONG_RATIO,
                )
            }
        },
        action = {
            BlueButton(
                text = "정보",
                height = 48,
                width = SlotPickDimens.ActionWidth,
                fontSize = 15,
                onClick = detailDialogOpen,
            )

            if (mongVo.stateCode == MongStateCode.GRADUATE_READY) {
                BlueButton(
                    text = "졸업",
                    height = 48,
                    width = SlotPickDimens.ActionWidth,
                    fontSize = 15,
                    onClick = graduateDialogOpen,
                )
            } else {
                BlueButton(
                    text = "삭제",
                    height = 48,
                    width = SlotPickDimens.ActionWidth,
                    fontSize = 15,
                    onClick = deleteDialogOpen,
                )
            }

            if (mongVo.stateCode !in listOf(
                    MongStateCode.DEAD,
                    MongStateCode.DELETE,
                    MongStateCode.GRADUATE,
                )
            ) {
                BlueButton(
                    text = "선택",
                    height = 48,
                    width = SlotPickDimens.ActionWidth,
                    fontSize = 15,
                    onClick = pickDialogOpen,
                    disable = mongVo.mongId == currentMongId,
                )
            }
        },
    )
}
