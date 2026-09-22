package com.monglife.mongs.domain.device.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StepRestoreTest {

    @Test
    fun `처음 보는 복구는 반영하고 이력에 남긴다`() {
        val result = StepRestore.apply(emptyList(), restoreWalkingCount = 1_000, eventId = "a")

        assertEquals(1_000, result.restoredWalkingCount)
        assertEquals(listOf("a"), result.appliedEventIds)
    }

    @Test
    fun `이미 반영한 복구는 무시한다`() {
        val result = StepRestore.apply(listOf("a", "b"), restoreWalkingCount = 1_000, eventId = "b")

        assertEquals(0, result.restoredWalkingCount)
        assertEquals(listOf("a", "b"), result.appliedEventIds)
    }

    @Test
    fun `식별자가 없으면 반영하지 않는다`() {
        // 중복을 가려낼 수 없으면 이중 적립보다 반영하지 않는 쪽이 안전하다.
        val result = StepRestore.apply(emptyList(), restoreWalkingCount = 1_000, eventId = "")

        assertEquals(0, result.restoredWalkingCount)
    }

    @Test
    fun `0 이하의 복구 금액은 무시한다`() {
        assertEquals(0, StepRestore.apply(emptyList(), 0, "a").restoredWalkingCount)
        assertEquals(0, StepRestore.apply(emptyList(), -1_000, "a").restoredWalkingCount)
    }

    @Test
    fun `이력은 상한 개수만큼만 남기고 오래된 것부터 버린다`() {
        val full = (1..StepRestore.MAX_APPLIED_EVENT_IDS).map { "id-$it" }

        val result = StepRestore.apply(full, restoreWalkingCount = 1_000, eventId = "new")

        assertEquals(StepRestore.MAX_APPLIED_EVENT_IDS, result.appliedEventIds.size)
        assertEquals("id-2", result.appliedEventIds.first())
        assertEquals("new", result.appliedEventIds.last())
    }
}
