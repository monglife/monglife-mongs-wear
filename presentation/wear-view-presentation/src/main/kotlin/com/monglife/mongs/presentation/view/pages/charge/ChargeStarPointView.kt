package com.monglife.mongs.presentation.view.pages.charge

import android.app.Activity
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.Text
import com.monglife.mongs.presentation.view.assets.DAL_MU_RI
import com.monglife.mongs.presentation.view.assets.MongsWhite
import com.monglife.mongs.presentation.view.assets.RouterPath
import com.monglife.mongs.presentation.view.component.common.background.DefaultBackground
import com.monglife.mongs.presentation.view.component.common.bar.LoadingBar
import com.monglife.mongs.presentation.view.component.common.button.BlueButton
import com.monglife.mongs.presentation.view.component.common.button.SelectButton
import com.monglife.mongs.presentation.view.component.common.button.YellowButton
import com.monglife.mongs.presentation.view.component.common.pagenation.PageIndicator
import com.monglife.mongs.presentation.view.component.common.textbox.StarPointBox
import com.monglife.mongs.presentation.view.utils.NumberUtil
import com.monglife.mongs.presentation.viewmodel.pages.charge.ChargeStarPointViewModel
import com.mongs.presentation.view.wear.R
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@Composable
internal fun ChargeStarPointView(
    navController: NavController,
    chargeStarPointViewModel: ChargeStarPointViewModel = hiltViewModel(),
    context: Context = LocalContext.current
) {
    val uiState = chargeStarPointViewModel.uiState.collectAsState()

    Box {
        DefaultBackground()
        if (uiState.value.loadingBar) {
            LoadingBar()
        } else {
            Box(modifier = Modifier.zIndex(1f)) {
                ChargeStarPointContent(chargeStarPointViewModel = chargeStarPointViewModel)
            }
        }
    }

    // UI 이벤트 소비
    LaunchedEffect(Unit) {
        chargeStarPointViewModel.uiEvent.collect { event ->
            when (event) {
                is ChargeStarPointViewModel.UiEvent.Buy -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is ChargeStarPointViewModel.UiEvent.Consume -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is ChargeStarPointViewModel.UiEvent.NavMain -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    navController.popBackStack(RouterPath.Main.route, inclusive = false)
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun ChargeStarPointContent(
    modifier: Modifier = Modifier,
    chargeStarPointViewModel: ChargeStarPointViewModel,
    context: Context = LocalContext.current
) {
    val productVos = chargeStarPointViewModel.productVos.collectAsState()
    val productIndex = remember { mutableIntStateOf(0) }
    val starPoint = chargeStarPointViewModel.starPoint.collectAsState()
    val currentProductVo = remember {
        derivedStateOf {
            if (productIndex.intValue < productVos.value.size) {
                productVos.value[productIndex.intValue]
            } else null
        }
    }
    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = 0f
            override val selectedPage: Int
                get() = productIndex.intValue
            override val pageCount: Int
                get() = productVos.value.size
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        PageIndicator(
            pageIndicatorState = pageIndicatorState,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 5.dp)
                .zIndex(1f),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f),
        ) {
            currentProductVo.value?.let {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(15.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.2f)
                    ) {
                        StarPointBox(starPoint = starPoint.value)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                    ) {
                        SelectButton(
                            leftBtnDisabled = productIndex.intValue == 0,
                            rightBtnDisabled = productIndex.intValue == productVos.value.size - 1,
                            leftBtnClick = {
                                productIndex.intValue = max(0, productIndex.intValue - 1)
                            },
                            rightBtnClick = {
                                productIndex.intValue =
                                    min(productIndex.intValue + 1, productVos.value.size - 1)
                            },
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = it.productName,
                                        textAlign = TextAlign.Center,
                                        fontFamily = DAL_MU_RI,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 16.sp,
                                        color = MongsWhite,
                                        maxLines = 1,
                                    )
                                }

                                Spacer(modifier = Modifier.height(15.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.point_icon_star),
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.3f)
                    ) {
                        if (it.orderVos.isNotEmpty()) {
                            BlueButton(
                                text = "소비",
                                height = 37,
                                width = 90,
                                fontSize = 16,
                                onClick = { chargeStarPointViewModel.consume(orderVo = it.orderVos.first()) },
                            )
                        } else {
                            YellowButton(
                                text = "${NumberUtil.formatAsCurrency(ceil(it.price).toInt())}원",
                                height = 37,
                                width = 110,
                                fontSize = 16,
                                onClick = {
                                    chargeStarPointViewModel.orderAndConsume(
                                        activity = context as Activity,
                                        productId = it.productId
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
