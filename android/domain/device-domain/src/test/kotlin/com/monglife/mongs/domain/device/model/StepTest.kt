package com.monglife.mongs.domain.device.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StepTest {

    @Test
    fun `표시 걸음 수는 확정 잔액과 실시간 걸음의 합이다`() {
        val step = Step(walkingCount = 1_200, pendingWalkingCount = 45)

        assertEquals(1_245, step.getCurrentWalkingCount())
    }

    @Test
    fun `환전 가능 판정에는 실시간 걸음을 포함하지 않는다`() {
        // 아직 지갑에 안 들어온 걸음까지 환전하면 배치가 끝내 안 왔을 때 잔액이 밀린다.
        val step = Step(walkingCount = 999, pendingWalkingCount = 500)

        assertFalse(step.canConsume(1_000))
        assertTrue(step.canConsume(999))
    }

    @Test
    fun `0 이하는 환전할 수 없다`() {
        val step = Step(walkingCount = 5_000)

        assertFalse(step.canConsume(0))
        assertFalse(step.canConsume(-1_000))
    }
}
