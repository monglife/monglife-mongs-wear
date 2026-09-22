package com.monglife.mongs.data.device.persistence.service

import android.util.Log
import androidx.health.services.client.PassiveListenerService
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.IntervalDataPoint
import com.monglife.mongs.data.device.persistence.collector.StepCollector
import com.monglife.mongs.data.device.persistence.datastore.DeviceDataStore
import com.monglife.mongs.domain.device.model.HealthStepSample
import com.monglife.mongs.domain.device.model.StepSource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Health Services 가 걸음 배치를 밀어 넣는 지점
 *
 * 앱이 실행 중이 아니어도 OS 가 이 서비스를 바인드해 호출한다. 여기가 사실상 걸음 수집의 본체다.
 */
@AndroidEntryPoint
class StepPassiveListenerService : PassiveListenerService() {

    @Inject lateinit var stepCollector: StepCollector

    @Inject lateinit var deviceDataStore: DeviceDataStore

    override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
        // 코루틴을 띄우고 바로 반환하면 안 된다. 이 메서드가 반환하는 순간 Health Services 가
        // 언바인드하고 프로세스가 회수될 수 있어 DataStore 쓰기가 통째로 날아간다.
        Log.i("StepCollection", "onNewDataPointsReceived types=${dataPoints.dataTypes}")
        runBlocking {
            stepCollector.creditHealthDeltaSamples(dataPoints.getData(DataType.STEPS).toSamples())
            stepCollector.creditHealthDailySamples(dataPoints.getData(DataType.STEPS_DAILY).toSamples())
        }
    }

    /**
     * 활동 권한이 회수되면 Health Services 가 등록을 스스로 해제한다.
     * 수집 경로를 NONE 으로 내려 UI 가 "-" 를 그리게 하고, 죽은 커서로 적립하지 않게 한다.
     */
    override fun onPermissionLost() {
        Log.w("StepCollection", "활동 권한 상실 - 수집 중단")
        runBlocking { deviceDataStore.setStepSource(StepSource.NONE) }
    }

    private fun List<IntervalDataPoint<Long>>.toSamples(): List<HealthStepSample> = map {
        HealthStepSample(
            steps = it.value,
            startDurationFromBootMillis = it.startDurationFromBoot.toMillis(),
            endDurationFromBootMillis = it.endDurationFromBoot.toMillis(),
        )
    }
}
