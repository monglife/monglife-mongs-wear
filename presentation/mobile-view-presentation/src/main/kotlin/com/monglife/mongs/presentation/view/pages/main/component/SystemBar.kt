package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.button.LabeledCircleButton
import com.mongs.presentation.view.mobile.R

/**
 * 하단 우측 시스템 바. 몽 상태와 무관하게 항상 활성이다.
 */
@Composable
internal fun SystemBar(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(MainDimens.ActionGap), modifier = modifier) {
        LabeledCircleButton(
            icon = R.drawable.btn_icon_help,
            border = R.drawable.btn_border_purple_dark,
            label = "도움말",
            size = MainDimens.SystemSize,
            onClick = { navController.navigate(RouterPath.Help.route) },
        )
        LabeledCircleButton(
            icon = R.drawable.btn_icon_charge,
            border = R.drawable.btn_border_purple_dark,
            label = "충전",
            size = MainDimens.SystemSize,
            onClick = { navController.navigate(RouterPath.ChargeStarPoint.route) },
        )
        LabeledCircleButton(
            icon = R.drawable.btn_icon_notice,
            border = R.drawable.btn_border_purple_dark,
            label = "공지",
            size = MainDimens.SystemSize,
            onClick = { navController.navigate(RouterPath.Notice.route) },
        )
        LabeledCircleButton(
            icon = R.drawable.btn_icon_setting,
            border = R.drawable.btn_border_purple_dark,
            label = "설정",
            size = MainDimens.SystemSize,
            onClick = { navController.navigate(RouterPath.Setting.route) },
        )
    }
}
