package com.monglife.mongs.application.mong.usecase.mission

import com.monglife.mongs.application.mong.port.web.MissionWebPort
import com.monglife.mongs.application.mong.port.web.response.GetMissionResponse
import com.monglife.mongs.application.mong.port.web.response.GetMissionRewardResponse
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.domain.mong.enums.MissionCycleCode
import com.monglife.mongs.domain.mong.enums.MissionGoalTypeCode
import com.monglife.mongs.domain.mong.enums.MissionRewardTypeCode
import com.monglife.mongs.domain.mong.enums.MissionStateCode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetMissionsUseCaseTest {

    private lateinit var missionWebPort: MissionWebPort
    private lateinit var getMissionsUseCase: GetMissionsUseCase

    @Before
    fun setUp() {
        missionWebPort = mock()
        getMissionsUseCase = GetMissionsUseCase(missionWebPort = missionWebPort)
    }

    private fun response(
        accountMissionId: Long,
        cycleCode: MissionCycleCode,
        goalCount: Int,
        progressCount: Int,
        stateCode: MissionStateCode,
    ) = GetMissionResponse(
        accountMissionId = accountMissionId,
        missionCode = "MS_T_$accountMissionId",
        cycleCode = cycleCode,
        goalTypeCode = MissionGoalTypeCode.COUNT,
        title = "테스트 미션",
        description = "테스트 설명",
        goalCount = goalCount,
        progressCount = progressCount,
        stateCode = stateCode,
        claimedAt = null,
        rewards = listOf(
            GetMissionRewardResponse(
                rewardTypeCode = MissionRewardTypeCode.INVENTORY,
                rewardCode = "FD000",
                inventoryTypeCode = InventoryTypeCode.FOOD,
                amount = 2,
            )
        ),
    )

    @Test
    fun `주기와 상태를 그대로 보존해 Vo 로 바꾼다`() = runTest {
        whenever(missionWebPort.getMissions()).thenReturn(
            listOf(
                response(1L, MissionCycleCode.DAILY, goalCount = 3, progressCount = 1, stateCode = MissionStateCode.IN_PROGRESS),
                response(2L, MissionCycleCode.WEEKLY, goalCount = 5, progressCount = 5, stateCode = MissionStateCode.CLAIMABLE),
                response(3L, MissionCycleCode.MONTHLY, goalCount = 1, progressCount = 1, stateCode = MissionStateCode.CLAIMED),
            )
        )

        val expected = getMissionsUseCase()

        assertEquals(3, expected.size)
        assertEquals(MissionCycleCode.DAILY, expected[0].cycleCode)
        assertEquals(MissionCycleCode.WEEKLY, expected[1].cycleCode)
        assertEquals(MissionStateCode.CLAIMED, expected[2].stateCode)
    }

    @Test
    fun `수령 가능 여부는 상태 코드로만 판단한다`() = runTest {
        whenever(missionWebPort.getMissions()).thenReturn(
            listOf(
                response(1L, MissionCycleCode.DAILY, goalCount = 3, progressCount = 3, stateCode = MissionStateCode.CLAIMABLE),
                // 목표를 채웠어도 이미 수령했으면 다시 받을 수 없다
                response(2L, MissionCycleCode.DAILY, goalCount = 3, progressCount = 3, stateCode = MissionStateCode.CLAIMED),
            )
        )

        val expected = getMissionsUseCase()

        assertTrue(expected[0].isClaimable)
        assertFalse(expected[1].isClaimable)
    }

    @Test
    fun `진행률은 0 과 1 사이로 잘린다`() = runTest {
        whenever(missionWebPort.getMissions()).thenReturn(
            listOf(
                response(1L, MissionCycleCode.DAILY, goalCount = 4, progressCount = 1, stateCode = MissionStateCode.IN_PROGRESS),
                // 서버가 목표치로 잘라 보내지만, 넘겨 오더라도 바가 삐져나가면 안 된다
                response(2L, MissionCycleCode.WEEKLY, goalCount = 5, progressCount = 9, stateCode = MissionStateCode.CLAIMABLE),
            )
        )

        val expected = getMissionsUseCase()

        assertEquals(0.25f, expected[0].progressRatio)
        assertEquals(1f, expected[1].progressRatio)
    }

    @Test
    fun `보상도 함께 Vo 로 바뀐다`() = runTest {
        whenever(missionWebPort.getMissions()).thenReturn(
            listOf(response(1L, MissionCycleCode.DAILY, goalCount = 1, progressCount = 0, stateCode = MissionStateCode.IN_PROGRESS))
        )

        val expected = getMissionsUseCase().first().rewards

        assertEquals(1, expected.size)
        assertEquals(MissionRewardTypeCode.INVENTORY, expected[0].rewardTypeCode)
        assertEquals("FD000", expected[0].rewardCode)
        assertEquals(InventoryTypeCode.FOOD, expected[0].inventoryTypeCode)
        assertEquals(2, expected[0].amount)
    }
}
