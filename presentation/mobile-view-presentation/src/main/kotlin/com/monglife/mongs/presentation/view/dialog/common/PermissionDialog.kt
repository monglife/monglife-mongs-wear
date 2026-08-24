package com.monglife.mongs.presentation.view.dialog.common

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.mongs.presentation.view.mobile.R

/**
 * 권한 안내.
 *
 * wear 는 4개 weight 행으로 세로로 쌓았지만, 가로 화면에서는 아이콘과 문구를
 * 좌우로 놓는 편이 자연스럽다. 설정 화면 이동 런처와 callback 흐름은 원문 그대로다.
 */
@Composable
internal fun PermissionDialog(
    modifier: Modifier = Modifier,
    permissionName: String,
    onDismiss: () -> Unit,
    callback: () -> Unit,
    context: Context = LocalContext.current,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black.copy(alpha = 0.8f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
    ) {
        // 권한 요청 설정 화면 이동 런처
        val permissionLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                callback()
            }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.btn_icon_locker),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "$permissionName 권한이 필요해요",
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp,
                    color = MongsWhite,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "권한을 설정해주세요",
                    fontFamily = DAL_MU_RI,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    color = MongsWhite.copy(alpha = 0.8f),
                )
                Spacer(modifier = Modifier.height(20.dp))
                BlueButton(
                    text = "설정",
                    width = 110,
                    height = 42,
                    fontSize = 15,
                    onClick = {
                        permissionLauncher.launch(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                        )
                    },
                )
            }
        }
    }
}
