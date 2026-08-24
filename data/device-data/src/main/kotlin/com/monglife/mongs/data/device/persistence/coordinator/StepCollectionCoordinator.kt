package com.monglife.mongs.data.device.persistence.coordinator

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.monglife.mongs.data.device.persistence.collector.StepCollector
import com.monglife.mongs.data.device.persistence.datastore.DeviceDataStore
import com.monglife.mongs.data.device.persistence.manager.HealthServicesStepManager
import com.monglife.mongs.data.device.persistence.manager.StepSensorManager
import com.monglife.mongs.data.device.persistence.worker.StepMaintenanceWorker
import com.monglife.mongs.domain.device.model.StepSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 걸음 수집 경로를 정하고 유지하는 곳
 *
 * 진입점이 셋이다 - 앱 메인 진입, 재부팅/앱 교체 브로드캐스트, 15분 주기 워커. 셋 다 같은
 * [synchronize] 를 부르며, 모든 동작이 멱등이라 몇 번을 겹쳐 불러도 문제가 없다.
 */
@Singleton
class StepCollectionCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceDataStore: DeviceDataStore,
    private val healthServicesStepManager: HealthServicesStepManager,
    private val stepSensorManager: StepSensorManager,
    private val stepCollector: StepCollector,
    private val workManager: WorkManager,
) {
    companion object {
        private const val TAG = "StepCollection"
        private const val MAINTENANCE_INTERVAL_MINUTES = 15L
    }

    private val mutex = Mutex()

    /**
     * 수집 경로 해석 → 등록 → 폴백 폴링 → 주기 워커 예약
     */
    suspend fun synchronize() = mutex.withLock {
        deviceDataStore.migrateStepSchema()

        val source = resolveSource(current = deviceDataStore.getStepState().source)
        // 경로가 바뀌면 커서를 비운다. 전환 직후 첫 관측은 기준선만 잡고 적립하지 않는다.
        deviceDataStore.setStepSource(source)

        Log.i(TAG, "source=$source")

        if (source.isHealthServices()) {
            // 재등록은 이전 등록을 대체하므로 매번 불러도 중복되지 않는다.
            // 재부팅/앱 교체 후 등록이 사라졌더라도 이 한 줄로 복구된다.
            Log.i(TAG, "passive register=${healthServicesStepManager.register(source)}")
        } else {
            healthServicesStepManager.unregister()
        }

        if (source == StepSource.SENSOR) {
            // 폴백 경로에는 푸시가 없으므로 지금 값을 읽어 그동안의 걸음을 회수한다.
            stepSensorManager.readTotalWalkingCount()?.let {
                Log.i(TAG, "sensor total=$it")
                stepCollector.creditSensorTotal(it)
            }
        }

        scheduleMaintenance()
    }

    /**
     * 재부팅/앱 교체 직후 재등록 요청
     *
     * BroadcastReceiver 는 10초 안에 반환해야 하는데 Health Services 등록이 그보다 오래 걸릴 수
     * 있어 워커로 넘긴다.
     */
    fun requestSynchronize() {
        workManager.enqueueUniqueWork(
            StepMaintenanceWorker.ONE_TIME_WORKER_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<StepMaintenanceWorker>().build(),
        )
    }

    private suspend fun resolveSource(current: StepSource): StepSource = when {
        !hasActivityPermission() -> StepSource.NONE
        // 한 번 정해진 경로는 다시 묻지 않는다. 화면에 들어올 때마다 capability 를 조회하면
        // 그만큼 첫 화면이 늦어지고, 기기의 지원 여부가 도중에 바뀔 일도 없다.
        // NONE 은 권한이 없어서 그렇게 된 것일 수 있으므로 다시 해석한다.
        current.isCollecting() -> current
        else -> healthServicesStepManager.resolveSupportedSource()
            ?: if (stepSensorManager.isAvailable()) StepSource.SENSOR else StepSource.NONE
    }

    private fun hasActivityPermission(): Boolean =
        context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) ==
                PackageManager.PERMISSION_GRANTED

    private fun scheduleMaintenance() {
        workManager.enqueueUniquePeriodicWork(
            StepMaintenanceWorker.PERIODIC_WORKER_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            PeriodicWorkRequestBuilder<StepMaintenanceWorker>(
                MAINTENANCE_INTERVAL_MINUTES,
                TimeUnit.MINUTES,
            ).build(),
        )
    }
}
