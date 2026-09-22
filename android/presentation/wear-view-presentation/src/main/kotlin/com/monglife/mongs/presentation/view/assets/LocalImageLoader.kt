package com.monglife.mongs.presentation.view.assets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.decode.ImageDecoderDecoder

/**
 * GIF 디코더가 붙은 앱 공용 ImageLoader
 *
 * ImageLoader 는 메모리·디스크 캐시와 OkHttp 클라이언트를 들고 있는 무거운 객체다.
 * 컴포저블 본문에서 매번 새로 만들면 리컴포지션마다 캐시가 통째로 버려지고
 * GIF 디코딩이 처음부터 다시 시작된다. 반드시 이 CompositionLocal 을 통해 공유한다.
 */
val LocalMongsImageLoader: ProvidableCompositionLocal<ImageLoader> =
    compositionLocalOf { error("LocalMongsImageLoader 가 제공되지 않았습니다. MongsTheme 안에서 사용하세요.") }

/**
 * 앱 전역에서 재사용할 ImageLoader 를 생성한다.
 */
fun createMongsImageLoader(context: Context): ImageLoader =
    ImageLoader.Builder(context)
        .components { add(ImageDecoderDecoder.Factory()) }
        .build()

/**
 * MongsTheme 등 컴포지션 루트에서 한 번만 호출해 ImageLoader 를 기억해 둔다.
 */
@Composable
internal fun rememberMongsImageLoader(): ImageLoader {
    val context = LocalContext.current

    return remember(context) { createMongsImageLoader(context = context) }
}
