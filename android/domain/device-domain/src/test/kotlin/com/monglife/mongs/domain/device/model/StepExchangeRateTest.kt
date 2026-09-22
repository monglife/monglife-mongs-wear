package com.monglife.mongs.domain.device.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StepExchangeRateTest {

    @Test
    fun `1000 걸음이 모여야 한 단위를 환전할 수 있다`() {
        assertEquals(0, StepExchangeRate.maxExchangeableUnits(999))
        assertEquals(1, StepExchangeRate.maxExchangeableUnits(1_999))
        assertEquals(3, StepExchangeRate.maxExchangeableUnits(3_000))
    }

    @Test
    fun `단위당 100 payPoint 를 지급한다`() {
        assertEquals(0, StepExchangeRate.payPointOf(0))
        assertEquals(300, StepExchangeRate.payPointOf(3))
    }

    @Test
    fun `단위 수를 걸음 수로 환산한다`() {
        assertEquals(3_000, StepExchangeRate.walkingCountOf(3))
        assertEquals(0, StepExchangeRate.walkingCountOf(-1))
    }
}
