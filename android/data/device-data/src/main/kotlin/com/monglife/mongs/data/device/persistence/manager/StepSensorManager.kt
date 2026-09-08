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

/**
 * `Sensor.TYPE_STEP_COUNTER` 접근
 *
 * 값은 "부팅 이후 누계" 이고 재부팅하면 0 으로 리셋된다. 앱이 죽어 있어도 OS 가 계속 세므로
 * 주기적으로 읽기만 해도 그동안의 걸음을 회수할 수 있다 - 다만 재부팅 직전 구간은 복구할 수 없다.
 * 그래서 이 경로는 Health Services 를 못 쓰는 기기의 폴백이다.
 */
@Singleton
class StepSensorManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val STEP_SENSOR_TYPE = Sensor.TYPE_STEP_COUNTER
        private const val STEP_LISTENER_TIMEOUT = 3000L
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepSensor: Sensor?
        get() = runCatching { sensorManager.getDefaultSensor(STEP_SENSOR_TYPE) }.getOrNull()

    /**
     * 걸음 센서 탑재 여부
     */
    fun isAvailable(): Boolean = stepSensor != null

    /**
     * 부팅 이후 누계 걸음 수 1회 조회
     *
     * on-change 센서라 등록 직후 현재 값이 한 번 내려온다. 그래도 값이 오지 않는 경우를 대비해
     * 타임아웃을 두고, 그때는 null 을 돌려 호출부가 적립을 건너뛰게 한다.
     */
    suspend fun readTotalWalkingCount(): Int? = suspendCancellableCoroutine { cont ->
        val timeoutJob = CoroutineScope(Dispatchers.IO).launch {
            delay(STEP_LISTENER_TIMEOUT)
            if (cont.isActive) cont.resume(null)
        }

        runCatching {
            stepSensor?.let {
                sensorManager.registerListener(object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent?) {
                        if (event?.sensor?.type == STEP_SENSOR_TYPE && cont.isActive) {
                            val totalWalkingCount = event.values[0].toInt()
                            sensorManager.unregisterListener(this)
                            timeoutJob.cancel()
                            if (cont.isActive) cont.resume(totalWalkingCount)
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

                }, it, SensorManager.SENSOR_DELAY_FASTEST)
            } ?: run {
                timeoutJob.cancel()
                if (cont.isActive) cont.resume(null)
            }
        }.onFailure {
            timeoutJob.cancel()
            if (cont.isActive) cont.resume(null)
        }
    }

    /**
     * 부팅 이후 누계 걸음 수 Flow 조회
     *
     * 센서를 못 읽는 경우에는 아무것도 방출하지 않는다. null 을 흘려 보내면 호출부마다
     * "값 없음"을 다시 분기해야 하는데, 걸음 수집에서 그럴 일이 없다.
     */
    fun observeTotalWalkingCount(): Flow<Int> = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == STEP_SENSOR_TYPE) {
                    trySend(event.values[0].toInt())
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val registered = runCatching {
            stepSensor?.let {
                sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL)
            } ?: false
        }.getOrDefault(false)

        if (!registered) close()

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.conflate()
}
