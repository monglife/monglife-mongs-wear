package com.monglife.mongs.application.auth.usecase

import com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.auth.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.mongs.application.auth.port.web.response.VerifyAppVersionResponse
import com.monglife.mongs.domain.auth.model.Session
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetAppEntryStateUseCaseTest {

    private lateinit var authWebPort: AuthWebPort
    private lateinit var devicePersistencePort: DevicePersistencePort
    private lateinit var authPersistencePort: AuthPersistencePort
    private lateinit var getAppEntryStateUseCase: GetAppEntryStateUseCase

    @Before
    fun setUp() {
        authWebPort = mock()
        devicePersistencePort = mock()
        authPersistencePort = mock()
        getAppEntryStateUseCase = GetAppEntryStateUseCase(
            authWebPort = authWebPort,
            devicePersistencePort = devicePersistencePort,
            authPersistencePort = authPersistencePort,
        )
    }

    private suspend fun givenResponse(
        mustUpdate: Boolean = false,
        underMaintenance: Boolean = false,
        maintenanceMessage: String? = null,
        maintenanceEndAt: String? = null,
    ) {
        whenever(devicePersistencePort.getAppPackageName()).thenReturn("com.mongs.wear")
        whenever(devicePersistencePort.getBuildVersion()).thenReturn("2.2.1")
        whenever(authWebPort.verifyAppVersion(appPackageName = "com.mongs.wear", buildVersion = "2.2.1"))
            .thenReturn(
                VerifyAppVersionResponse(
                    appPackageName = "com.mongs.wear",
                    buildVersion = "2.2.1",
                    mustUpdate = mustUpdate,
                    underMaintenance = underMaintenance,
                    maintenanceMessage = maintenanceMessage,
                    maintenanceStartAt = "2026-09-21T02:00:00",
                    maintenanceEndAt = maintenanceEndAt,
                )
            )
    }

    @Test
    fun `아무 문제가 없으면 그대로 들어간다`() = runTest {
        givenResponse()

        assertEquals(AppEntryState.Normal, getAppEntryStateUseCase())
        verify(authPersistencePort, never()).deleteSession()
    }

    @Test
    fun `강제 업데이트면 세션을 지우고 NeedUpdate`() = runTest {
        givenResponse(mustUpdate = true)
        whenever(authPersistencePort.getSession()).thenReturn(mock<Session>())

        assertEquals(AppEntryState.NeedUpdate, getAppEntryStateUseCase())
        verify(authPersistencePort).deleteSession()
    }

    @Test
    fun `점검 중이면 문구와 종료 시각을 그대로 넘긴다`() = runTest {
        givenResponse(
            underMaintenance = true,
            maintenanceMessage = "서버 점검 중입니다.",
            maintenanceEndAt = "2026-09-21T04:00:00",
        )

        assertEquals(
            AppEntryState.Maintenance(message = "서버 점검 중입니다.", endAt = "2026-09-21T04:00:00"),
            getAppEntryStateUseCase(),
        )
    }

    @Test
    fun `점검은 세션을 건드리지 않는다 - 끝나면 이어서 쓴다`() = runTest {
        givenResponse(underMaintenance = true, maintenanceMessage = "점검")

        getAppEntryStateUseCase()

        verify(authPersistencePort, never()).deleteSession()
    }

    @Test
    fun `업데이트와 점검이 겹치면 업데이트가 이긴다`() = runTest {
        givenResponse(mustUpdate = true, underMaintenance = true, maintenanceMessage = "점검")
        whenever(authPersistencePort.getSession()).thenReturn(mock<Session>())

        assertEquals(AppEntryState.NeedUpdate, getAppEntryStateUseCase())
    }

    @Test
    fun `종료 미정이면 endAt 이 null 이다`() = runTest {
        givenResponse(underMaintenance = true, maintenanceMessage = "긴급 점검", maintenanceEndAt = null)

        assertEquals(
            AppEntryState.Maintenance(message = "긴급 점검", endAt = null),
            getAppEntryStateUseCase(),
        )
    }
}
