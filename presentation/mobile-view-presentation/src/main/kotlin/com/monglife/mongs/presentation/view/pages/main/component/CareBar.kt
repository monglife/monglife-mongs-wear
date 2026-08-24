package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.button.LabeledCircleButton
import com.mongs.presentation.view.mobile.R

/**
 * 하단 좌측 돌봄 바.
 *
 * wear 는 몽을 눌러 InteractionDialog 를 열어야 나오던 버튼들이다.
 * 가로 화면에는 자리가 있으므로 그냥 꺼내 둔다. 다이얼로그는 이식하지 않는다.
 */
@Composable
internal fun CareBar(
    modifier: Modifier = Modifier,
    navController: NavController,
    currentMongVo: MongVo?,
    onStroke: () -> Unit,
    onSleep: () -> Unit,
    onPoopClean: () -> Unit,
) {
    val disable = currentMongVo?.let {
        it.level == 0 || it.stateCode == MongStateCode.DEAD || it.stateCode == MongStateCode.DELETE
    } ?: true
    val sleeping = currentMongVo?.isSleep ?: false

    Row(horizontalArrangement = Arrangement.spacedBy(MainDimens.ActionGap), modifier = modifier) {
        LabeledCircleButton(
            icon = R.drawable.btn_icon_feed,
            border = R.drawable.btn_border_yellow,
            label = "먹이",
            disable = disable || sleeping,
            onClick = { navController.navigate(RouterPath.FeedNested.route) },
        )
        LabeledCircleButton(
            icon = R.drawable.btn_icon_stroke,
            border = R.drawable.btn_border_pink,
            label = "쓰다듬",
            disable = disable || sleeping,
            onClick = onStroke,
        )
        LabeledCircleButton(
            icon = R.drawable.btn_icon_sleep,
            border = R.drawable.btn_border_blue,
            label = if (sleeping) "깨우기" else "재우기",
            disable = disable,
            onClick = onSleep,
        )
        LabeledCircleButton(
            icon = R.drawable.btn_icon_poop_clean,
            border = R.drawable.btn_border_green,
            label = "청소",
            disable = disable,
            onClick = onPoopClean,
        )
        LabeledCircleButton(
            icon = R.drawable.btn_icon_inventory,
            border = R.drawable.btn_border_orange,
            label = "가방",
            disable = disable,
            onClick = { navController.navigate(RouterPath.Inventory.route) },
        )
    }
}
