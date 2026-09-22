package com.monglife.mongs.domain.device.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StepAccumulatorSensorTest {

    private val bootMark = 1_700_000_000_000L

    @Test
    fun `이 기기에서 처음 읽은 누계는 기준선으로만 쓴다`() {
        // 센서 누계에는 앱 설치 이전 걸음이 들어 있다.
        val result = StepAccumulator.applySensorTotal(StepCursor.EMPTY, bootMark, sensorTotal = 52_130)

        assertEquals(0, result.credited)
        assertEquals(52_130, result.cursor.lastSensorTotal)
    }

    @Test
    fun `기준선을 잡은 뒤에는 증가분만 적립한다`() {
        val first = StepAccumulator.applySensorTotal(StepCursor.EMPTY, bootMark, 52_130)

        val second = StepAccumulator.applySensorTotal(first.cursor, bootMark, 52_460)

        assertEquals(330, second.credited)
        assertEquals(52_460, second.cursor.lastSensorTotal)
    }

    @Test
    fun `같은 값을 다시 읽으면 적립하지 않는다`() {
        val first = StepAccumulator.applySensorTotal(StepCursor.EMPTY, bootMark, 52_130)

        val second = StepAccumulator.applySensorTotal(first.cursor, bootMark, 52_130)

        assertEquals(0, second.credited)
    }

    @Test
    fun `재부팅 후 누계는 전부 사용자가 걸은 걸음이다`() {
        val first = StepAccumulator.applySensorTotal(StepCursor.EMPTY, bootMark, 52_130)

        // 카운터가 0 부터 다시 시작한 뒤 240 걸음
        val rebooted = StepAccumulator.applySensorTotal(first.cursor, bootMark + 86_400_000L, 240)

        assertEquals(240, rebooted.credited)
        assertEquals(240, rebooted.cursor.lastSensorTotal)
    }

    @Test
    fun `부팅 시각은 그대로인데 센서가 역행하면 기준선만 다시 잡는다`() {
        val first = StepAccumulator.applySensorTotal(StepCursor.EMPTY, bootMark, 52_130)

        val backwards = StepAccumulator.applySensorTotal(first.cursor, bootMark, 10)

        assertEquals(0, backwards.credited)
        assertEquals(10, backwards.cursor.lastSensorTotal)
    }

    @Test
    fun `증가분이 배치 상한을 넘으면 잘라 낸다`() {
        val first = StepAccumulator.applySensorTotal(StepCursor.EMPTY, bootMark, 0)

        val huge = StepAccumulator.applySensorTotal(first.cursor, bootMark, Int.MAX_VALUE)

        assertEquals(StepAccumulator.MAX_CREDIT_PER_BATCH, huge.credited)
    }

    @Test
    fun `부팅 시각 절삭은 같은 부팅 세션을 같은 값으로 만든다`() {
        val a = StepCursor.bootMarkOf(currentTimeMillis = 1_700_000_005_000L, elapsedRealtimeMillis = 5_000L)
        val b = StepCursor.bootMarkOf(currentTimeMillis = 1_700_000_012_300L, elapsedRealtimeMillis = 12_300L)

        assertEquals(a, b)
    }
}
