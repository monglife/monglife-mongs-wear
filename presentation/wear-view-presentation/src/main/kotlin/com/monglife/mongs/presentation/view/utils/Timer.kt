package com.monglife.mongs.presentation.view.utils

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay

@Composable
fun Timer(
    progress: MutableFloatState,
    timerDelay: Long = 1000L,
    startTimeMillis: Long = 0L,
    maxTimeMillis: Long,
) {
    val timeMillis = remember { mutableLongStateOf(startTimeMillis) }

    // 타이머
    LaunchedEffect(Unit) {
        Log.d("TEST", "startTimeMillis: $startTimeMillis, maxTimeMillis: $maxTimeMillis")

        if (startTimeMillis < maxTimeMillis) {
            while (progress.floatValue < 100f) {
                delay(timerDelay)
                timeMillis.longValue += timerDelay
                progress.floatValue = timeMillis.longValue / maxTimeMillis * 100f
                Log.d("TEST", "timeMillis: ${timeMillis.longValue}, progress: ${progress.value}")
            }
        } else {
            progress.floatValue = 100f
        }
    }
}