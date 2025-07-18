package com.monglife.mongs.presentation.view.pages.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.button.CircleTextButton
import com.monglife.mongs.presentation.view.component.common.button.CircleImageButton
import com.mongs.wear.presentation.view.wear.R

@Composable
internal fun ConfigureContent(
    navController: NavController,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircleTextButton(
                    text = "i",
                    border = R.drawable.btn_border_purple_dark,
                ) {
                    navController.navigate(RouterPath.Help.route)
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
                    .offset(y = (-14).dp)
            ) {
                CircleImageButton(
                    icon = R.drawable.btn_icon_charge,
                    border = R.drawable.btn_border_purple_dark,
                ) {
                    navController.navigate(RouterPath.ChargeStarPoint.route)
                }

                Spacer(modifier = Modifier.width(48.dp))

                CircleImageButton(
                    icon = R.drawable.btn_icon_notice,
                    border = R.drawable.btn_border_purple_dark,
                ) {
                    navController.navigate(RouterPath.Notice.route)
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
                    .offset(y = (-8).dp)
            ) {
                CircleImageButton(
                    icon = R.drawable.btn_icon_feedback,
                    border = R.drawable.btn_border_purple_dark,
                ) {
                    navController.navigate(RouterPath.Feedback.route)
                }

                Spacer(modifier = Modifier.width(10.dp))

                CircleImageButton(
                    icon = R.drawable.btn_icon_setting,
                    border = R.drawable.btn_border_purple_dark,
                ) {
                    navController.navigate(RouterPath.Setting.route)
                }
            }
        }
    }
}