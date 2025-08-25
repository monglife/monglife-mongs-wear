package com.monglife.mongs.presentation.view.component.pages.battle

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import com.monglife.mongs.application.battle.vo.MatchVo
import com.monglife.mongs.domain.battle.enums.MatchRoundCode
import com.monglife.mongs.presentation.view.assets.MongResourceCode
import com.monglife.mongs.presentation.view.component.common.charactor.Mong
import com.mongs.presentation.view.wear.R


@Composable
fun MatchPlayer(
    matchPlayerVo: MatchVo.MatchPlayerVo,
    matchEffect: Boolean = true,
    effectAlignment: Alignment,
) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components { add(ImageDecoderDecoder.Factory()) }
        .build()

    Box {
        Mong(
            mong = MongResourceCode.valueOf(matchPlayerVo.mongCode),
            ratio = 0.6f,
            modifier = Modifier.zIndex(1f)
        )

        if (matchEffect) {
            Box(
                modifier = Modifier
                    .align(effectAlignment)
                    .zIndex(2f)
            ) {
                when (matchPlayerVo.roundCode) {
                    MatchRoundCode.NONE -> {}
                    MatchRoundCode.MATCH_HEAL -> {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = R.drawable.icon_healthy,    // TODO: 회복 이미지 구급 상자 아이콘으로 변경
                                imageLoader = imageLoader
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .zIndex(2f)
                                .height(40.dp)
                                .width(40.dp),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    MatchRoundCode.MATCH_ATTACKED_HEAL -> {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = R.drawable.icon_healthy,    // TODO: 회복 이미지 구급 상자 아이콘으로 변경
                                imageLoader = imageLoader
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .zIndex(2f)
                                .height(40.dp)
                                .width(40.dp),
                            contentScale = ContentScale.FillBounds
                        )
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = R.drawable.effect_attack,
                                imageLoader = imageLoader
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .zIndex(3f)
                                .height(40.dp)
                                .width(40.dp),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    MatchRoundCode.MATCH_ATTACKED -> {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = R.drawable.effect_attack,
                                imageLoader = imageLoader
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .zIndex(2f)
                                .height(40.dp)
                                .width(40.dp),
                            contentScale = ContentScale.FillBounds
                        )
                    }

                    MatchRoundCode.MATCH_DEFENCE -> {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = R.drawable.effect_defence,
                                imageLoader = imageLoader
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .zIndex(2f)
                                .height(40.dp)
                                .width(40.dp),
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
            }
        }
    }
}