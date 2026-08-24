package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.application.device.exception.InvalidExchangeWalkingCountException
import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.application.device.port.web.request.ExchangeWalkingCountRequest
import com.monglife.mongs.domain.device.model.Step
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class ExchangeWalkingCountUseCaseTest {

    private lateinit var deviceWebPort: DeviceWebPort
    private lateinit var devicePersistencePort: DevicePersistencePort
    private lateinit var exchangeWalkingCountUseCase: ExchangeWalkingCountUseCase

    @Before
    fun setUp() {
        deviceWebPort = mock()
        devicePersistencePort = mock()
        exchangeWalkingCountUseCase = ExchangeWalkingCountUseCase(
            deviceWebPort = deviceWebPort,
            devicePersistencePort = devicePersistencePort,
        )
    }

    @Test
    fun `환전에 성공하면 서버를 부른 뒤 로컬 잔액을 차감한다`() = runTest {
        whenever(devicePersistencePort.getStep()).thenReturn(Step(walkingCount = 3_500))

        exchangeWalkingCountUseCase(ExchangeWalkingCountUseCase.Command(mongId = 1L, exchangeUnits = 3))

        verify(deviceWebPort).exchangeWalkingCount(
            ExchangeWalkingCountRequest(mongId = 1L, walkingCount = 3_000)
        )
        verify(devicePersistencePort).consumeWalkingCount(3_000)
    }

    @Test
    fun `잔액이 모자라면 서버를 부르지 않는다`() = runTest {
        whenever(devicePersistencePort.getStep()).thenReturn(Step(walkingCount = 2_999))

        assertThrows(InvalidExchangeWalkingCountException::class.java) {
            kotlinx.coroutines.runBlocking {
                exchangeWalkingCountUseCase(ExchangeWalkingCountUseCase.Command(mongId = 1L, exchangeUnits = 3))
            }
        }

        verifyNoInteractions(deviceWebPort)
        verify(devicePersistencePort, never()).consumeWalkingCount(any())
    }

    @Test
    fun `실시간으로 얹은 걸음은 환전 잔액에 포함하지 않는다`() = runTest {
        // 확정 잔액은 2999 뿐이고 나머지는 아직 지갑에 안 들어온 걸음이다.
        whenever(devicePersistencePort.getStep())
            .thenReturn(Step(walkingCount = 2_999, pendingWalkingCount = 500))

        assertThrows(InvalidExchangeWalkingCountException::class.java) {
            kotlinx.coroutines.runBlocking {
                exchangeWalkingCountUseCase(ExchangeWalkingCountUseCase.Command(mongId = 1L, exchangeUnits = 3))
            }
        }

        verifyNoInteractions(deviceWebPort)
    }

    @Test
    fun `서버 호출이 실패하면 로컬 잔액을 건드리지 않는다`() = runTest {
        whenever(devicePersistencePort.getStep()).thenReturn(Step(walkingCount = 3_500))
        whenever(deviceWebPort.exchangeWalkingCount(any()))
            .thenThrow(InvalidExchangeWalkingCountException())

        assertThrows(InvalidExchangeWalkingCountException::class.java) {
            kotlinx.coroutines.runBlocking {
                exchangeWalkingCountUseCase(ExchangeWalkingCountUseCase.Command(mongId = 1L, exchangeUnits = 3))
            }
        }

        verify(devicePersistencePort, never()).consumeWalkingCount(eq(3_000))
    }
}
