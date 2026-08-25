package com.monglife.mongs.presentation.view.dialog.pages.slotPick

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monglife.mongs.application.mong.vo.MongVo
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongResourceCode
import com.monglife.mongs.presentation.view.assets.MongsBlue
import com.monglife.mongs.presentation.view.assets.MongsGreen
import com.monglife.mongs.presentation.view.assets.MongsPink
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.MongsYellow
import com.monglife.mongs.presentation.view.component.common.bar.StatGauge
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.charactor.Mong
import com.monglife.mongs.presentation.view.component.common.textbox.PayPointBox
import com.monglife.mongs.presentation.view.pages.main.component.PanelDivider
import com.monglife.mongs.presentation.view.pages.main.component.mainPanel
import com.monglife.mongs.presentation.view.pages.main.component.pixelBox
import com.mongs.presentation.view.mobile.R
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * 몽 상세.
 *
 * wear 는 정보 / 지수 / 상태 3개 탭으로 나눠 놓았다. 192dp 원형 화면에 다 못 넣어서였다.
 * 가로에서는 한 카드에 좌우 2단으로 들어간다 - 탭 기계장치를 걷어냈다.
 *
 * wear 는 스칼라 11개를 평면으로 받아 mongCode 가 없었고 그래서 몽을 그리지 못했다.
 * MongVo 는 compose_stability.conf 상 stable 이라 통째로 받아도 리컴포지션 비용이 없다.
 *
 * 지수 4개는 wear 의 원형 CircularProgressIndicator 대신 StatGauge 를 쓴다.
 * wear 쪽 시그니처(indicatorColor/trackColor)가 wear 전용이라 import 치환으로는 컴파일되지 않고,
 * 단위도 맞는다 - MongVo 의 *Ratio 가 이미 0..100 이다.
 */
@Composable
internal fun SlotDetailDialog(
    modifier: Modifier = Modifier,
    mongVo: MongVo,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.88f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
    ) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .width(760.dp)
                .height(340.dp)
                .mainPanel()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SlotDetailProfile(mongVo = mongVo, modifier = Modifier.width(220.dp))

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .drawBehind { drawRect(color = MongsWhite.copy(alpha = 0.18f)) }
            )

            SlotDetailStats(
                mongVo = mongVo,
                onClose = onClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SlotDetailProfile(
    modifier: Modifier = Modifier,
    mongVo: MongVo,
) {
    /** wear 와 같은 1초 틱. mongId 가 바뀌면 다시 센다. */
    var age by remember { mutableStateOf("00시간 00분 00초") }
    LaunchedEffect(mongVo.mongId) {
        while (true) {
            val now = LocalDateTime.now()
            val hours = ChronoUnit.HOURS.between(mongVo.createdAt, now)
            val minutes = ChronoUnit.MINUTES.between(mongVo.createdAt.plusHours(hours), now)
            val seconds = ChronoUnit.SECONDS.between(
                mongVo.createdAt.plusHours(hours).plusMinutes(minutes), now
            )
            age = "%02d시간 %02d분 %02d초".format(hours, minutes, seconds)
            delay(1000)
        }
    }

    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically),
    ) {
        if (mongVo.stateCode == MongStateCode.DEAD) {
            Image(
                painter = painterResource(R.drawable.mong_rip),
                contentDescription = null,
                modifier = Modifier.size(104.dp),
            )
        } else {
            Mong(
                isPng = true,
                mong = MongResourceCode.getResource(mongVo.mongCode),
                ratio = 1.0f,
            )
        }

        Text(
            text = mongVo.name,
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 18.sp,
            color = MongsWhite,
            maxLines = 1,
        )
        Text(
            text = mongVo.mongName,
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 13.sp,
            color = MongsWhite.copy(alpha = 0.6f),
            maxLines = 1,
        )

        PayPointBox(width = 130, payPoint = mongVo.payPoint)

        Text(
            text = "%.2f kg".format(mongVo.weight),
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 15.sp,
            color = MongsWhite.copy(alpha = 0.8f),
            maxLines = 1,
        )
        Text(
            text = age,
            fontFamily = DAL_MU_RI,
            fontWeight = FontWeight.Light,
            fontSize = 15.sp,
            color = MongsWhite.copy(alpha = 0.8f),
            maxLines = 1,
        )
    }
}

@Composable
private fun SlotDetailStats(
    modifier: Modifier = Modifier,
    mongVo: MongVo,
    onClose: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        StatGauge(
            icon = R.drawable.icon_healthy,
            label = "체력",
            progress = mongVo.healthyRatio.toFloat(),
            indicatorColor = MongsPink,
        )
        StatGauge(
            icon = R.drawable.icon_satiety,
            label = "포만감",
            progress = mongVo.satietyRatio.toFloat(),
            indicatorColor = MongsYellow,
        )
        StatGauge(
            icon = R.drawable.icon_strength,
            label = "힘",
            progress = mongVo.strengthRatio.toFloat(),
            indicatorColor = MongsGreen,
        )
        StatGauge(
            icon = R.drawable.icon_fatigue,
            label = "피로도",
            progress = mongVo.fatigueRatio.toFloat(),
            indicatorColor = MongsBlue,
        )

        PanelDivider()

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StateChip(if (mongVo.isSleep) "수면중" else "기상")
            StateChip(mongVo.statusCode.message)
            StateChip(mongVo.stateCode.message)

            Box(modifier = Modifier.weight(1f))

            BlueButton(text = "닫기", width = 96, height = 44, fontSize = 15, onClick = onClose)
        }
    }
}

@Composable
private fun StateChip(text: String) {
    Text(
        text = text,
        fontFamily = DAL_MU_RI,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        color = MongsWhite,
        maxLines = 1,
        modifier = Modifier
            .pixelBox(
                fill = Color.Black.copy(alpha = 0.32f),
                border = MongsWhite.copy(alpha = 0.4f),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}
