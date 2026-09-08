package com.monglife.mongs.data.device.persistence.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.monglife.mongs.data.device.persistence.coordinator.StepCollectionCoordinator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * 걸음 수집 유지보수
 *
 * 두 가지 일을 한다.
 *  - Health Services 모드: 등록이 살아 있는지 재등록으로 확인한다(멱등). 재부팅 브로드캐스트를
 *    놓쳤거나 앱 교체로 등록이 날아갔을 때 스스로 복구되는 경로다.
 *  - 센서 폴백 모드: 센서 누계를 읽어 그동안의 걸음을 지갑에 회수한다. TYPE_STEP_COUNTER 는
 *    앱이 죽어 있어도 OS 가 계속 세므로 주기적으로 읽기만 하면 된다.
 */
@HiltWorker
class StepMaintenanceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val stepCollectionCoordinator: StepCollectionCoordinator,
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val PERIODIC_WORKER_NAME = "STEP_MAINTENANCE_WORKER"
        const val ONE_TIME_WORKER_NAME = "STEP_MAINTENANCE_WORKER_ONE_TIME"
    }

    override suspend fun doWork(): Result = try {
        stepCollectionCoordinator.synchronize()
        Result.success()
    } catch (e: Exception) {
        // 실패를 확정하지 않고 재시도를 남긴다. 부팅 직후에는 Health Services 가 아직 안 떠서
        // 등록이 실패할 수 있는데, 여기서 failure 를 반환하면 다음 주기까지 수집이 멈춘다.
        Result.retry()
    }
}
