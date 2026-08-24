package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.button.LabeledCircleButton
import com.mongs.presentation.view.mobile.R

/**
 * 우측 메뉴 레일 — 3열 x 3행.
 *
 * wear 의 2/3/2 피라미드는 원 안에 7개 원을 내접시키려는 형태다.
 * disable 술어는 wear InteractionContent 원문 그대로 유지한다.
 */
@Composable
internal fun MenuRail(
    modifier: Modifier = Modifier,
    navController: NavController,
    currentMongVo: MongVo?,
) {
    val dead = currentMongVo?.let {
        it.stateCode == MongStateCode.DEAD || it.stateCode == MongStateCode.DELETE
    } ?: true
    val idle = currentMongVo?.let {
        it.level == 0 || it.stateCode == MongStateCode.DEAD || it.isSleep
    } ?: true

    val items = listOf(
        Item(R.drawable.btn_icon_collection, R.drawable.btn_border_orange, "도감", false, RouterPath.CollectionNested),
        Item(R.drawable.point_icon_pay, R.drawable.btn_border_purple_dark, "환전", dead, RouterPath.ExchangeNested),
        Item(R.drawable.btn_icon_map_search, R.drawable.btn_border_blue, "탐색", dead, RouterPath.SearchMap),
        Item(R.drawable.btn_icon_slot_pick, R.drawable.btn_border_red, "슬롯", false, RouterPath.SlotPick),
        Item(R.drawable.btn_icon_luck_draw, R.drawable.btn_border_purple, "뽑기", dead, RouterPath.RandomDraw),
        Item(R.drawable.btn_icon_activity, R.drawable.btn_border_green, "훈련", idle, RouterPath.TrainingNested),
        Item(R.drawable.btn_icon_battle, R.drawable.btn_border_pink, "배틀", idle, RouterPath.BattleNested),
    )

    Column(
        modifier = modifier
            .width(MainDimens.RailWidth)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(MainDimens.RailRowGap, Alignment.CenterVertically),
    ) {
        items.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(MainDimens.ActionGap)) {
                row.forEach { item ->
                    LabeledCircleButton(
                        icon = item.icon,
                        border = item.border,
                        label = item.label,
                        disable = item.disable,
                        onClick = { navController.navigate(item.path.route) },
                    )
                }
            }
        }
    }
}

private data class Item(
    val icon: Int,
    val border: Int,
    val label: String,
    val disable: Boolean,
    val path: RouterPath,
)
