package com.monglife.mongs.data.device.persistence.manager

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
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
//        private const val STEP_SENSOR_TYPE = Sensor.TYPE_RELATIVE_HUMIDITY
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var stepSensor: Sensor? = sensorManager.getDefaultSensor(STEP_SENSOR_TYPE)

    /**
     * 전체 걸음 수 조회
     */
    suspend fun getTotalWalkingCount(): Int = suspendCancellableCoroutine { cont ->
        stepSensor?.let {
            sensorManager.registerListener(object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event?.sensor?.type == STEP_SENSOR_TYPE) {
                        val totalWalkingCount = event.values[0].toInt()
                        sensorManager.unregisterListener(this)
                        cont.resume(totalWalkingCount)
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

            }, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } ?: run {
            cont.resume(0)
        }
    }

    /**
     * 전체 걸음 수 Flow 조회
     */
    fun getTotalWalkingCountFlow(): Flow<Int> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == STEP_SENSOR_TYPE) {
                    val totalWalkingCount = event.values[0].toInt()
                    trySend(totalWalkingCount)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        // 센서 등록
        sensorManager.registerListener(
            listener,
            stepSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        // 정리
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.conflate()
}