package com.monglife.mongs.data.device.persistence.collector

import android.os.SystemClock
import android.util.Log
import com.monglife.mongs.data.device.persistence.datastore.DeviceDataStore
import com.monglife.mongs.domain.device.model.HealthStepSample
import com.monglife.mongs.domain.device.model.StepAccumulator
import com.monglife.mongs.domain.device.model.StepCursor
import com.monglife.mongs.domain.device.model.StepSource
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 걸음 수 지갑에 쓰는 유일한 통로
 *
 * 수집 경로가 셋(Health Services 두 종류 + 센서)이라 적립 지점이 흩어지면 이중 카운트를 막을 수
 * 없다. 전부 여기를 거치게 하고, 실제 적립 여부는 DataStore 트랜잭션 안의 경로 게이트가 판단한다.
 * 활성 경로가 아닌 입력은 조용히 버려진다.
 */
@Singleton
class StepCollector @Inject constructor(
    private val deviceDataStore: DeviceDataStore,
) {
    companion object {
        private const val TAG = "StepCollection"
    }

    /**
     * Health Services `DataType.STEPS` (구간 delta) 적립
     */
    suspend fun creditHealthDeltaSamples(samples: List<HealthStepSample>) {
        if (samples.isEmpty()) return

        val state = deviceDataStore.creditStep(StepSource.HEALTH_STEPS) { cursor ->
            StepAccumulator.applyDeltaSamples(cursor, bootMark(), samples)
        }
        Log.i(TAG, "delta samples=${samples.size} balance=${state.balance} source=${state.source}")
    }

    /**
     * Health Services `DataType.STEPS_DAILY` (일일 누계) 적립
     */
    suspend fun creditHealthDailySamples(samples: List<HealthStepSample>) {
        if (samples.isEmpty()) return

        val state = deviceDataStore.creditStep(StepSource.HEALTH_DAILY_STEPS) { cursor ->
            StepAccumulator.applyDailySamples(cursor, bootMark(), samples)
        }
        Log.i(TAG, "daily samples=${samples.size} balance=${state.balance} source=${state.source}")
    }

    /**
     * `Sensor.TYPE_STEP_COUNTER` (부팅 이후 누계) 적립
     */
    suspend fun creditSensorTotal(sensorTotal: Int) {
        val state = deviceDataStore.creditStep(StepSource.SENSOR) { cursor ->
            StepAccumulator.applySensorTotal(cursor, bootMark(), sensorTotal)
        }
        Log.i(TAG, "sensor total=$sensorTotal balance=${state.balance} source=${state.source}")
    }

    /**
     * 환전 차감. 잔액이 모자라면 예외를 던진다.
     */
    suspend fun consume(amount: Int) {
        deviceDataStore.consumeStep(amount)
    }

    /**
     * 환전 실패분 복구
     *
     * 적립 경로를 거치지 않는다. 센서가 센 걸음이 아니라 이미 차감했던 걸음을 되돌리는 것이라
     * 수집 경로 게이트를 통과할 수 없고, 통과시켜서도 안 된다.
     */
    suspend fun restore(restoreWalkingCount: Int, eventId: String) {
        val state = deviceDataStore.restoreStep(restoreWalkingCount, eventId)
        Log.i(TAG, "restore walkingCount=$restoreWalkingCount balance=${state.balance} eventId=$eventId")
    }

    /**
     * 부팅 시각 추정
     *
     * elapsedRealtime 은 부팅 이후 단조 증가하므로 벽시계에서 빼면 부팅 시각이 나온다.
     * 다만 벽시계가 NTP 로 보정되면 이 값이 흔들려서, 절삭 없이 비교하면 매번 재부팅으로 오판한다.
     * 절삭은 [StepCursor.bootMarkOf] 가 한다.
     */
    private fun bootMark(): Long =
        StepCursor.bootMarkOf(System.currentTimeMillis(), SystemClock.elapsedRealtime())
}
