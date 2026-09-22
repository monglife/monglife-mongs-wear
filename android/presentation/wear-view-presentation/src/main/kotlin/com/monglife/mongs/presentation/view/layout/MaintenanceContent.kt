package com.monglife.mongs.presentation.view.layout

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.mongs.presentation.view.wear.R

/**
 * 서버 점검 화면
 *
 * 서버가 점검 중이라고 알려 주면 진입을 막고 이 화면을 띄운다.
 * 세션은 그대로 둔다 - 서버가 로그인을 막지 않으므로 점검이 끝나면 이어서 쓴다.
 *
 * @param message 서버가 준 안내 문구. 비어 있으면 기본 문구를 쓴다
 * @param endAt '2026-09-21T04:00:00' 꼴. null 이면 종료 미정이라 시각을 보여 주지 않는다
 */
@Composable
internal fun MaintenanceContent (
    message: String?,
    endAt: String?,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Image(
                painter = painterResource(R.drawable.icon_logo_not_open),
                contentDescription = null,
                modifier = Modifier.size(55.dp)
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "서버 점검 중",
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 16.sp,
                color = MongsWhite,
            )

            Spacer(modifier = Modifier.height(13.dp))

            Text(
                text = message?.takeIf { it.isNotBlank() } ?: "잠시 후 다시 이용해 주세요!",
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                color = MongsWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            )

            formatEndAt(endAt)?.let { endAtText ->
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$endAtText 까지",
                    textAlign = TextAlign.Center,
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 11.sp,
                    color = MongsWhite,
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            BlueButton(
                text = "종료",
                onClick = { (context as ComponentActivity).finish() },
            )
        }
    }
}

/**
 * '2026-09-21T04:00:00' -> '09/21 04:00'
 *
 * 서버는 시간대 없는 벽시계 문자열을 준다. 파싱해서 Date 로 바꾸면 기기 시간대가 끼어들어
 * 시각이 밀리므로, 문자열을 그대로 잘라 쓴다. 작은 화면이라 연도는 뺀다.
 */
private fun formatEndAt(endAt: String?): String? {
    if (endAt == null || endAt.length < 16) return null

    val date = endAt.substring(5, 10).replace('-', '/')
    val time = endAt.substring(11, 16)

    return "$date $time"
}
