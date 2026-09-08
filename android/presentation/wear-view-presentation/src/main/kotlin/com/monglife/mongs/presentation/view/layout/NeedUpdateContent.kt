package com.monglife.mongs.presentation.view.layout

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.core.net.toUri
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.mongs.presentation.view.wear.R

/**
 * 앱 업데이트 필요 화면
 */
@SuppressLint("QueryPermissionsNeeded")
@Composable
internal fun NeedUpdateContent (
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
                text = "새로운 버전 출시",
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 16.sp,
                color = MongsWhite,
            )

            Spacer(modifier = Modifier.height(13.dp))

            Text(
                text = "앱을 업데이트 해주세요!",
                textAlign = TextAlign.Center,
                fontFamily = DAL_MU_RI,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                color = MongsWhite,
            )

            Spacer(modifier = Modifier.height(25.dp))

            val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                data = "market://details?id=${context.packageName}".toUri()
                setPackage("com.android.vending")
            }

            if (playStoreIntent.resolveActivity(context.packageManager) != null) {
                BlueButton(
                    width = 80,
                    text =  "업데이트",
                    onClick = { context.startActivity(playStoreIntent) },
                )
            } else {
                BlueButton(
                    text =  "종료",
                    onClick = { (context as ComponentActivity).finish() },
                )
            }
        }
    }
}