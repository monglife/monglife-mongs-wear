package com.monglife.mongs.data.device.persistence.manager

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class StepSensorManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val STEP_SENSOR_TYPE = Sensor.TYPE_STEP_COUNTER
        private const val STEP_LISTENER_TIMEOUT = 3000L
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    /**
     * 전체 걸음 수 조회
     */
    suspend fun getTotalWalkingCount(): Int? = suspendCancellableCoroutine { cont ->
        // 타임 아웃 측정
        val timeoutJob = CoroutineScope(Dispatchers.IO).launch {
            delay(STEP_LISTENER_TIMEOUT)
            cont.resume(null)
        }

        runCatching {
            sensorManager.getDefaultSensor(STEP_SENSOR_TYPE)?.let {
                sensorManager.registerListener(object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if (event?.sensor?.type == STEP_SENSOR_TYPE) {
                            val totalWalkingCount = event.values[0].toInt()
                            sensorManager.unregisterListener(this)
                            timeoutJob.cancel()
                            cont.resume(totalWalkingCount)
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

                }, it, SensorManager.SENSOR_DELAY_FASTEST)
            } ?: run {
                timeoutJob.cancel()
                cont.resume(null)
            }
        }.onFailure {
            timeoutJob.cancel()
            cont.resume(null)
        }
    }

    /**
     * 전체 걸음 수 Flow 조회
     */
    fun getTotalWalkingCountFlow(): Flow<Int?> = callbackFlow {

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == STEP_SENSOR_TYPE) {
                    val totalWalkingCount = event.values[0].toInt()
                    trySend(totalWalkingCount)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        runCatching {
            sensorManager.getDefaultSensor(STEP_SENSOR_TYPE)?.let {
                sensorManager.registerListener(
                    listener,
                    it,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            } ?: run {
                trySend(null)
            }
        }.onFailure {
            trySend(null)
        }

        // 정리
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.conflate()
}