package com.monglife.mongs.data.device.persistence.coordinator

import android.content.Context
import androidx.work.WorkManager
import com.monglife.mongs.data.device.persistence.collector.StepCollector
import com.monglife.mongs.data.device.persistence.datastore.DeviceDataStore
import com.monglife.mongs.data.device.persistence.entity.StepStateEntity
import com.monglife.mongs.data.device.persistence.manager.HealthServicesStepManager
import com.monglife.mongs.data.device.persistence.manager.StepSensorManager
import com.monglife.mongs.domain.device.model.StepCursor
import com.monglife.mongs.domain.device.model.StepSource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * flush 가 앱 진입 경로에서만 도는지 검증한다.
 *
 * 이 분기가 중요한 이유는 공식 KDoc 이 flush 를 스로틀 대상으로 못박았기 때문이다. 15분 워커와
 * 부팅 리시버까지 flush 하면 정작 화면을 열었을 때 스로틀에 걸려 아무 일도 안 일어날 수 있다.
 *
 * 목 Context 의 checkSelfPermission 은 0(PERMISSION_GRANTED)을 돌려주므로 권한은 통과 상태다.
 */
class StepCollectionCoordinatorTest {

    private lateinit var context: Context
    private lateinit var deviceDataStore: DeviceDataStore
    private lateinit var healthServicesStepManager: HealthServicesStepManager
    private lateinit var stepSensorManager: StepSensorManager
    private lateinit var stepCollector: StepCollector
    private lateinit var workManager: WorkManager
    private lateinit var stepCollectionCoordinator: StepCollectionCoordinator

    @Before
    fun setUp() {
        context = mock()
        deviceDataStore = mock()
        healthServicesStepManager = mock()
        stepSensorManager = mock()
        stepCollector = mock()
        workManager = mock()
        stepCollectionCoordinator = StepCollectionCoordinator(
            context = context,
            deviceDataStore = deviceDataStore,
            healthServicesStepManager = healthServicesStepManager,
            stepSensorManager = stepSensorManager,
            stepCollector = stepCollector,
            workManager = workManager,
        )
    }

    /**
     * 이미 HEALTH_STEPS 로 해석이 끝난 상태. resolveSource 가 capability 를 다시 묻지 않는다.
     */
    private suspend fun givenRegisteredHealthSteps(registered: Boolean = true) {
        whenever(deviceDataStore.getStepState()).thenReturn(
            StepStateEntity(
                balance = 0,
                source = StepSource.HEALTH_STEPS,
                cursor = StepCursor.EMPTY,
            )
        )
        whenever(healthServicesStepManager.register(StepSource.HEALTH_STEPS)).thenReturn(registered)
    }

    @Test
    fun `앱 진입 경로는 등록 뒤에 flush 를 부른다`() = runTest {
        givenRegisteredHealthSteps()
        whenever(healthServicesStepManager.flush()).thenReturn(true)

        stepCollectionCoordinator.synchronize(flush = true)

        // 순서가 뒤집히면 등록된 리스너가 없어 flush 가 no-op 이 된다.
        inOrder(healthServicesStepManager) {
            verify(healthServicesStepManager).register(StepSource.HEALTH_STEPS)
            verify(healthServicesStepManager).flush()
        }
    }

    @Test
    fun `백그라운드 경로는 flush 를 부르지 않는다`() = runTest {
        givenRegisteredHealthSteps()

        stepCollectionCoordinator.synchronize()

        verify(healthServicesStepManager).register(StepSource.HEALTH_STEPS)
        verify(healthServicesStepManager, never()).flush()
    }

    @Test
    fun `등록에 실패하면 flush 를 건너뛴다`() = runTest {
        givenRegisteredHealthSteps(registered = false)

        stepCollectionCoordinator.synchronize(flush = true)

        verify(healthServicesStepManager, never()).flush()
    }

    @Test
    fun `flush 가 실패해도 주기 워커 예약까지 간다`() = runTest {
        givenRegisteredHealthSteps()
        whenever(healthServicesStepManager.flush()).thenReturn(false)

        stepCollectionCoordinator.synchronize(flush = true)

        // flush 는 전달을 앞당기는 힌트일 뿐이라 동기화를 중단시키면 안 된다.
        verify(workManager).enqueueUniquePeriodicWork(eq("STEP_MAINTENANCE_WORKER"), any(), any())
    }

    @Test
    fun `센서 폴백 경로에서는 flush 를 부르지 않는다`() = runTest {
        whenever(deviceDataStore.getStepState()).thenReturn(
            StepStateEntity(
                balance = 0,
                source = StepSource.SENSOR,
                cursor = StepCursor.EMPTY,
            )
        )
        whenever(stepSensorManager.readTotalWalkingCount()).thenReturn(1_234)

        stepCollectionCoordinator.synchronize(flush = true)

        // Health Services 등록 자체가 없는 경로다. flush 할 대상이 없다.
        verify(healthServicesStepManager, never()).flush()
        verify(healthServicesStepManager).unregister()
        verify(stepCollector).creditSensorTotal(1_234)
    }
}
