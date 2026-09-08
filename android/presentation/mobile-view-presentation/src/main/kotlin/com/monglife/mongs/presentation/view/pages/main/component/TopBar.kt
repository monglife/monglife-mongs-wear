package com.monglife.mongs.presentation.view.pages.main.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MainDimens
import com.monglife.mongs.presentation.view.assets.MongsRed
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.utils.NumberUtil
import com.mongs.presentation.view.mobile.R

/**
 * 상단바 — 왼쪽에 계정 재화, 오른쪽에 도움말과 설정.
 *
 * 세 칩이 모두 어딘가로 이동하므로 오른쪽 끝에 ">" 를 둔다.
 * 환전은 따로 버튼을 두지 않고 해당 재화 칩이 그 화면으로 데려간다.
 */
@Composable
internal fun TopBar(
    modifier: Modifier = Modifier,
    navController: NavController,
    starPoint: Int,
    walkingCount: Int,
    stepAvailable: Boolean,
    permissionGranted: Boolean,
    exchangeDisabled: Boolean,
    onStepClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(MainDimens.TopBarHeight)
            .mainPanel()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MainDimens.ChipGap),
    ) {
        PointChip(
            modifier = Modifier.weight(MainDimens.CHIP_WEIGHT),
            icon = R.drawable.point_icon_star,
            text = NumberUtil.formatAsCurrency(starPoint),
            onClick = { navController.navigate(RouterPath.ChargeStarPoint.route) },
        )

        /**
         * 세 가지 상태를 구분한다.
         *
         * 권한 없음  -> "권한 필요", 탭하면 안내를 연다.
         * 수집 불가  -> "-". 권한은 있는데 걸음 센서가 없거나 아직 붙지 않은 경우다.
         *              wear StepContent 와 같은 규칙이다.
         * 정상       -> 걸음 수.
         */
        PointChip(
            modifier = Modifier.weight(MainDimens.CHIP_WEIGHT),
            icon = R.drawable.btn_icon_walking,
            text = when {
                !permissionGranted -> "권한 필요"
                !stepAvailable -> "-"
                else -> NumberUtil.formatAsCurrency(walkingCount)
            },
            background = if (!permissionGranted) MongsRed.copy(alpha = 0.42f)
                         else Color.Black.copy(alpha = 0.32f),
            // 권한이 없으면 안내를 열어야 하므로 그때는 막지 않는다.
            disable = permissionGranted && exchangeDisabled,
            onClick = onStepClick,
        )

        Spacer(modifier = Modifier.weight(MainDimens.CHIP_SPACE_WEIGHT))

        Row(horizontalArrangement = Arrangement.spacedBy(MainDimens.TopIconGap)) {
            IconAction(R.drawable.ic_help) { navController.navigate(RouterPath.Help.route) }
            IconAction(R.drawable.btn_icon_setting) { navController.navigate(RouterPath.Setting.route) }
        }
    }
}

@Composable
private fun PointChip(
    modifier: Modifier = Modifier,
    icon: Int,
    text: String,
    alpha: Float = 1f,
    background: Color = Color.Black.copy(alpha = 0.32f),
    disable: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(MainDimens.ChipHeight)
            .pixelBox(fill = background, border = MongsWhite.copy(alpha = 0.4f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (!disable) onClick() },
            )
            .padding(start = 14.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            alpha = alpha * if (disable) 0.4f else 1f,
            modifier = Modifier.size(22.dp),
        )
        Text(
            text = text,
            fontFamily = DAL_MU_RI,
            fontSize = 18.sp,
            color = MongsWhite.copy(alpha = alpha * if (disable) 0.4f else 1f),
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        // 누를 수 있다는 표시
        Text(
            text = ">",
            fontFamily = DAL_MU_RI,
            fontSize = 14.sp,
            color = MongsWhite.copy(alpha = if (disable) 0.2f else 0.5f),
        )
    }
}

/** 테두리 없는 아이콘 버튼. */
@Composable
private fun IconAction(
    icon: Int,
    size: Int = MainDimens.TopIconSize,
    onClick: () -> Unit,
) {
    Image(
        painter = painterResource(icon),
        contentDescription = null,
        modifier = Modifier
            .size(size.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    )
}
