package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.button.LabeledCircleButton
import com.mongs.presentation.view.mobile.R

/**
 * 우측 상단 기능 버튼 — 탐색 / 뽑기 / 훈련 / 배틀 (2행 2열).
 *
 * disable 술어는 wear InteractionContent 원문 그대로다.
 * 환전은 상단바의 페이포인트 칩이 대신하고, 충전과 공지는 설정 안으로 들어간다.
 */
@Composable
internal fun ActionGrid(
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
        Item(R.drawable.btn_icon_map_search, R.drawable.btn_border_blue, "탐색", dead, RouterPath.SearchMap),
        Item(R.drawable.btn_icon_luck_draw, R.drawable.btn_border_purple, "뽑기", dead, RouterPath.RandomDraw),
        Item(R.drawable.btn_icon_activity, R.drawable.btn_border_green, "훈련", idle, RouterPath.TrainingNested),
        Item(R.drawable.btn_icon_battle, R.drawable.btn_border_pink, "배틀", idle, RouterPath.BattleNested),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MainDimens.RailRowGap),
    ) {
        items.chunked(2).forEach { row ->
            // weight 로 셀을 나누면 열이 넓을 때 버튼이 양끝으로 벌어진다. 붙여서 가운데로 모은다.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MainDimens.ActionGap, Alignment.CenterHorizontally),
            ) {
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

/**
 * 우측 하단 바 — 도감 / 슬롯 (1행 2열).
 *
 * 몽 상태와 무관하게 항상 누를 수 있다.
 */
@Composable
internal fun BottomBar(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MainDimens.ActionGap, Alignment.CenterHorizontally),
    ) {
        LabeledCircleButton(
            icon = R.drawable.btn_icon_collection,
            border = R.drawable.btn_border_orange,
            label = "도감",
            onClick = { navController.navigate(RouterPath.CollectionNested.route) },
        )
        LabeledCircleButton(
            icon = R.drawable.btn_icon_slot_pick,
            border = R.drawable.btn_border_red,
            label = "슬롯",
            onClick = { navController.navigate(RouterPath.SlotPick.route) },
        )
    }
}

private data class Item(
    val icon: Int,
    val border: Int,
    val label: String,
    val disable: Boolean,
    val path: RouterPath,
)
