package com.monglife.mongs.data.device.persistence.manager

import android.content.Context
import android.util.Log
import androidx.health.services.client.HealthServices
import androidx.health.services.client.clearPassiveListenerService
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.PassiveListenerConfig
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.setPassiveListenerService
import com.monglife.mongs.data.device.persistence.service.StepPassiveListenerService
import com.monglife.mongs.domain.device.model.StepSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Health Services Passive Monitoring 등록 래퍼
 *
 * 이 경로를 쓰는 이유는 하나다. 앱이 죽어 있어도 OS 가 걸음을 모아 두었다가 서비스를 깨워
 * 전달해 주므로, 앱이 꺼진 동안의 걸음을 유실 없이 회수할 수 있다.
 *
 * 모바일 앱도 이 모듈을 공유한다. 폰에는 Health Services 가 없어 호출이 실패하거나 응답이 없는데,
 * 그 경우 조용히 폴백으로 내려가도록 전부 타임아웃 + 예외 흡수로 감쌌다.
 */
@Singleton
class HealthServicesStepManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val TAG = "StepCollection"
        private const val CAPABILITY_TIMEOUT_MILLIS = 5_000L

        /** Health Services 를 제공하는 시스템 앱. AAR 매니페스트가 <queries> 로 이미 열어 둔다. */
        private const val HEALTH_SERVICES_PACKAGE = "com.google.android.wearable.healthservices"
    }

    private val passiveMonitoringClient by lazy {
        HealthServices.getClient(context).passiveMonitoringClient
    }

    /**
     * 이 기기가 지원하는 걸음 수집 경로 조회
     *
     * `STEPS`(구간 delta) 는 선택 지원이고 `STEPS_DAILY`(자정부터의 누계) 만 모든 Wear OS 기기의
     * 필수 지원 타입이다. delta 쪽이 다루기 훨씬 단순하므로 있으면 그쪽을 쓰고, 없으면 일일 누계로 내려간다.
     */
    suspend fun resolveSupportedSource(): StepSource? = if (!isInstalled()) {
        // 폰에는 Health Services 가 없다. 패키지 유무를 먼저 보고 빠져나가지 않으면
        // capability 조회가 타임아웃까지 버티면서 첫 진입 로딩이 그만큼 길어진다.
        null
    } else try {
        withTimeout(CAPABILITY_TIMEOUT_MILLIS) {
            val supported = passiveMonitoringClient.getCapabilities().supportedDataTypesPassiveMonitoring
            Log.i(TAG, "passive capabilities=$supported")
            when {
                DataType.STEPS in supported -> StepSource.HEALTH_STEPS
                DataType.STEPS_DAILY in supported -> StepSource.HEALTH_DAILY_STEPS
                else -> null
            }
        }
    } catch (e: TimeoutCancellationException) {
        null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "capability 조회 실패", e)
        null
    }

    private fun isInstalled(): Boolean = runCatching {
        context.packageManager.getPackageInfo(HEALTH_SERVICES_PACKAGE, 0)
    }.isSuccess

    /**
     * 리스너 서비스 등록
     *
     * 등록은 앱당 하나뿐이고 재등록이 이전 등록을 대체한다(공식 KDoc). 즉 몇 번을 불러도 안전하며,
     * 재부팅 후 등록이 유지되지 않는 문제도 그냥 다시 부르는 것으로 해결된다.
     */
    suspend fun register(source: StepSource): Boolean {
        val dataType = when (source) {
            StepSource.HEALTH_STEPS -> DataType.STEPS
            StepSource.HEALTH_DAILY_STEPS -> DataType.STEPS_DAILY
            else -> return false
        }

        return try {
            passiveMonitoringClient.setPassiveListenerService(
                StepPassiveListenerService::class.java,
                PassiveListenerConfig.builder()
                    .setDataTypes(setOf(dataType))
                    .build(),
            )
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "passive 등록 실패", e)
            false
        }
    }

    /**
     * 리스너 서비스 해제
     *
     * Health Services 가 없으면 아예 부르지 않는다.
     * 예외는 아래에서 흡수되지만 그 전에 클라이언트가 바인드를 재시도하다 포기하면서
     * ServiceConnection 이 스택 트레이스를 E 레벨로 남긴다. 폰에서는 권한이 없거나 센서가
     * 없을 때마다 경로가 NONE 이 되어 이 해제가 호출되고, 15분 주기 워커까지 같은 경로를
     * 타므로 있지도 않은 서비스를 향한 오류 로그가 계속 쌓인다.
     * 애초에 등록된 적이 없으니 해제할 것도 없다.
     */
    suspend fun unregister() {
        if (!isInstalled()) return

        try {
            passiveMonitoringClient.clearPassiveListenerService()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // 등록된 적이 없으면 실패할 수 있다. 해제가 목적이므로 그대로 둔다.
        }
    }
}
