package com.monglife.mongs.presentation.view.component.pages.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI

/**
 * 진행도를 배경으로 칠하는 미션 항목.
 *
 * 칩 아래에 별도 바를 두면 진행도 0 인 항목이 줄줄이 있을 때 빈 트랙이 구분선처럼 읽힌다.
 * 칩 자체를 트랙으로 쓰면 그 줄이 사라지고, 목록을 훑을 때 색이 찬 정도로 진행도가 바로 보인다.
 */
@Composable
internal fun MissionChip(
    modifier: Modifier = Modifier,
    label: String,
    /**
     * 부분마다 색을 달리 쓰기 위해 AnnotatedString 을 받는다.
     * "달성 1/5 · 받을 수 있음 1" 에서 뒤쪽만 노랗게 하는 식이다.
     */
    secondaryLabel: AnnotatedString,
    progressRatio: Float,
    fontColor: Color,
    progressColor: Color,
    height: Int = 52,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(onClick = onClick),
    ) {
        // 채워진 부분. 글자가 그 위에 얹히므로 진하게 칠하지 않는다
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progressRatio.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(progressColor.copy(alpha = 0.35f)),
        )

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
        ) {
            /**
             * 제목이 길면 한 줄에 안 들어간다. 자르면 무슨 미션인지 알 수 없어
             * 가만히 두면 천천히 흘러 끝까지 읽히게 한다. 짧은 제목은 넘치지 않아 움직이지 않는다.
             */
            Text(
                text = label,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 16.sp,
                color = fontColor,
                maxLines = 1,
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    repeatDelayMillis = 1500,
                    velocity = 24.dp,
                ),
            )
            Text(
                text = secondaryLabel,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 12.sp,
                color = fontColor,
                maxLines = 1,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
    }
}
