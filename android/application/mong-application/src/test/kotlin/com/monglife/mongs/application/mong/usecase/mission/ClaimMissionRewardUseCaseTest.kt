package com.monglife.mongs.application.mong.usecase.mission

import com.monglife.mongs.application.mong.exception.InvalidClaimMissionRewardException
import com.monglife.mongs.application.mong.port.web.MissionWebPort
import com.monglife.mongs.application.mong.port.web.response.ClaimMissionRewardResponse
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ClaimMissionRewardUseCaseTest {

    private lateinit var missionWebPort: MissionWebPort
    private lateinit var claimMissionRewardUseCase: ClaimMissionRewardUseCase

    @Before
    fun setUp() {
        missionWebPort = mock()
        claimMissionRewardUseCase = ClaimMissionRewardUseCase(missionWebPort = missionWebPort)
    }

    @Test
    fun `사용자 미션 ID 와 대상 몽 ID 를 그대로 넘긴다`() = runTest {
        whenever(missionWebPort.claimMissionReward(accountMissionId = 7L, mongId = 3L))
            .thenReturn(ClaimMissionRewardResponse(accountMissionId = 7L, mongId = 3L, expRatio = 12.5, payPoint = 870))

        claimMissionRewardUseCase(
            ClaimMissionRewardUseCase.Command(accountMissionId = 7L, mongId = 3L)
        )

        verify(missionWebPort).claimMissionReward(accountMissionId = 7L, mongId = 3L)
    }

    @Test
    fun `수령에 실패하면 예외가 그대로 올라온다`() = runTest {
        whenever(missionWebPort.claimMissionReward(accountMissionId = 7L, mongId = 3L))
            .thenThrow(InvalidClaimMissionRewardException())

        assertThrows(InvalidClaimMissionRewardException::class.java) {
            runBlocking {
                claimMissionRewardUseCase(
                    ClaimMissionRewardUseCase.Command(accountMissionId = 7L, mongId = 3L)
                )
            }
        }
    }
}
