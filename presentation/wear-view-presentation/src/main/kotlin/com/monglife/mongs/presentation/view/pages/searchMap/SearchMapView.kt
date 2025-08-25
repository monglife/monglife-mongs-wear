package com.monglife.mongs.presentation.view.pages.searchMap

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.SearchMapBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.dialog.common.ConfirmAndCancelDialog
import com.monglife.mongs.presentation.view.dialog.common.PermissionDialog
import com.monglife.mongs.presentation.view.dialog.pages.searchMap.SearchMapOverDialog
import com.monglife.mongs.presentation.viewmodel.pages.searchMap.SearchMapViewModel
import com.mongs.presentation.view.wear.R

@Composable
internal fun SearchMapView(
    navController: NavController,
    searchMapViewModel: SearchMapViewModel = hiltViewModel(),
    context: Context = LocalContext.current,
) {
    val uiState = searchMapViewModel.uiState.collectAsState()
    val collectionMapVo = searchMapViewModel.collectionMapVo.collectAsState()
    val permission = searchMapViewModel.permission.collectAsState()

    Box {
        SearchMapBackground()

        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                SearchMapContent(searchMapViewModel = searchMapViewModel)
            }

            Box(modifier = Modifier.zIndex(2f)) {
                if (!permission.value) {
                    PermissionDialog(
                        permissionName = "위치",
                        callback = searchMapViewModel::verifyLocationPermission,
                    )
                } else if (uiState.value.confirmDialogOpen) {
                    ConfirmAndCancelDialog(
                        text = "맵 탐색을\n하시겠습니까?\n(현재 위치 정보 수집)",
                        cancel = searchMapViewModel::searchMapConfirmDialogClose,
                        confirm = searchMapViewModel::searchMap,
                    )
                } else if (uiState.value.searchDetailDialogOpen) {
                    collectionMapVo.value?.let {
                        SearchMapOverDialog(
                            collectionMapVo = it,
                            onEndClick = searchMapViewModel::searchMapDetailDialogClose
                        )
                    }
                }
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        searchMapViewModel.uiEvent.collect { event ->
            when (event) {
                is SearchMapViewModel.UiEvent.NavMain -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack(RouterPath.Main.route, inclusive = false)
                }

                is SearchMapViewModel.UiEvent.NotFound -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                else -> {}
            }
        }
    }
}

private const val SEARCH_MAX_SIZE = 95f
private const val SEARCH_MIN_SIZE = 40f

@Composable
private fun SearchMapContent(
    modifier: Modifier = Modifier,
    searchMapViewModel: SearchMapViewModel,
) {
    val uiState = searchMapViewModel.uiState.collectAsState()

    // animation
    val currentSize = remember { Animatable(SEARCH_MIN_SIZE) }
    val currentRotation = remember { mutableFloatStateOf(0f) }
    val rotation = remember { Animatable(currentRotation.floatValue) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxHeight()
        ) {
            Spacer(modifier = Modifier.height(15.dp))

            if (uiState.value.searchLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f)
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon_search),
                        contentDescription = null,
                        modifier = Modifier
                            .size(currentSize.value.dp)
                            .rotate(degrees = rotation.value),
                        contentScale = ContentScale.FillBounds,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f)
                ) {
                    Text(
                        text = "주변 맵 탐색중",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon_search),
                        contentDescription = null,
                        modifier = Modifier.size(SEARCH_MIN_SIZE.dp),
                        contentScale = ContentScale.FillBounds,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f)
                ) {
                    Text(
                        text = "주변 맵 탐색",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 24.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                ) {
                    Text(
                        text = "현재 위치 기준",
                        textAlign = TextAlign.Center,
                        fontFamily = DAL_MU_RI,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        color = MongsWhite,
                        maxLines = 1,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.2f)
                ) {
                    BlueButton(
                        text = "탐색",
                        onClick = searchMapViewModel::searchMapConfirmDialogOpen,
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
        }
    }

    // for size animation
    LaunchedEffect(uiState.value.searchLoading) {
        if (uiState.value.searchLoading) {
            currentSize.animateTo(
                targetValue = SEARCH_MAX_SIZE,
                animationSpec = tween(500, easing = LinearEasing)
            )
        } else if (currentSize.value > SEARCH_MIN_SIZE) {
            currentSize.animateTo(
                targetValue = SEARCH_MIN_SIZE,
                animationSpec = tween(0, easing = LinearEasing)
            )
        }
    }
    // for rotate animation
    LaunchedEffect(uiState.value.searchLoading) {
        if (uiState.value.searchLoading) {
            rotation.animateTo(
                targetValue = currentRotation.floatValue + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            ) { currentRotation.floatValue = value }
        } else if (currentRotation.floatValue > 0) {
            rotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(0, easing = LinearEasing)
            ) { currentRotation.floatValue = value }
        }
    }
}