package com.monglife.mongs.domain.device.model

import org.junit.Assert.assertEquals
import org.junit.Test

class StepAccumulatorHealthTest {

    private val bootMark = 1_700_000_000_000L

    private fun sample(start: Long, end: Long, steps: Long) =
        HealthStepSample(steps = steps, startDurationFromBootMillis = start, endDurationFromBootMillis = end)

    // --- DataType.STEPS (구간 delta) ---

    @Test
    fun `커서가 비어 있으면 배치를 전부 적립한다`() {
        val result = StepAccumulator.applyDeltaSamples(
            cursor = StepCursor.EMPTY,
            bootMark = bootMark,
            samples = listOf(sample(0, 60_000, 100), sample(60_000, 120_000, 200)),
        )

        assertEquals(300, result.credited)
        assertEquals(120_000L, result.cursor.lastEndDurationFromBootMillis)
    }

    @Test
    fun `같은 배치가 다시 와도 두 번 적립하지 않는다`() {
        val samples = listOf(sample(0, 60_000, 100), sample(60_000, 120_000, 200))
        val first = StepAccumulator.applyDeltaSamples(StepCursor.EMPTY, bootMark, samples)

        val second = StepAccumulator.applyDeltaSamples(first.cursor, bootMark, samples)

        assertEquals(0, second.credited)
        assertEquals(first.cursor.lastEndDurationFromBootMillis, second.cursor.lastEndDurationFromBootMillis)
    }

    @Test
    fun `과거 구간이 섞여 오면 커서 이후 구간만 적립한다`() {
        val first = StepAccumulator.applyDeltaSamples(
            StepCursor.EMPTY,
            bootMark,
            listOf(sample(0, 60_000, 100)),
        )

        val second = StepAccumulator.applyDeltaSamples(
            cursor = first.cursor,
            bootMark = bootMark,
            samples = listOf(sample(0, 60_000, 100), sample(60_000, 120_000, 250)),
        )

        assertEquals(250, second.credited)
    }

    @Test
    fun `순서가 뒤섞여 와도 커서는 가장 늦은 구간으로 간다`() {
        val result = StepAccumulator.applyDeltaSamples(
            cursor = StepCursor.EMPTY,
            bootMark = bootMark,
            samples = listOf(sample(60_000, 120_000, 200), sample(0, 60_000, 100)),
        )

        assertEquals(300, result.credited)
        assertEquals(120_000L, result.cursor.lastEndDurationFromBootMillis)
    }

    @Test
    fun `재부팅되면 경과 시간 커서를 비우고 다시 적립한다`() {
        val first = StepAccumulator.applyDeltaSamples(
            StepCursor.EMPTY,
            bootMark,
            listOf(sample(0, 600_000, 500)),
        )

        // 부팅 시각이 하루 밀렸다 = 재부팅. 경과 시간은 0 부터 다시 시작한다.
        val rebooted = StepAccumulator.applyDeltaSamples(
            cursor = first.cursor,
            bootMark = bootMark + 86_400_000L,
            samples = listOf(sample(0, 60_000, 70)),
        )

        assertEquals(70, rebooted.credited)
    }

    @Test
    fun `부팅 시각이 임계치 안에서 흔들리는 것은 재부팅이 아니다`() {
        val first = StepAccumulator.applyDeltaSamples(
            StepCursor.EMPTY,
            bootMark,
            listOf(sample(0, 600_000, 500)),
        )

        // NTP 보정으로 부팅 시각 추정치가 20초 흔들린 상황
        val jittered = StepAccumulator.applyDeltaSamples(
            cursor = first.cursor,
            bootMark = bootMark + 20_000L,
            samples = listOf(sample(0, 600_000, 500)),
        )

        assertEquals(0, jittered.credited)
    }

    @Test
    fun `배치 상한을 넘는 값은 잘라 낸다`() {
        val result = StepAccumulator.applyDeltaSamples(
            cursor = StepCursor.EMPTY,
            bootMark = bootMark,
            samples = listOf(sample(0, 60_000, 999_999_999L)),
        )

        assertEquals(StepAccumulator.MAX_CREDIT_PER_BATCH, result.credited)
    }

    // --- DataType.STEPS_DAILY (일일 누계) ---

    @Test
    fun `일일 누계는 처음 본 창에서 적립하지 않는다`() {
        // 낮에 앱을 처음 켜면 누계에 켜기 전 걸음이 전부 들어 있다. 기준선으로만 써야 한다.
        val result = StepAccumulator.applyDailySamples(
            cursor = StepCursor.EMPTY,
            bootMark = bootMark,
            samples = listOf(sample(-3_600_000, 60_000, 8_000)),
        )

        assertEquals(0, result.credited)
        assertEquals(8_000, result.cursor.dailyValue)
    }

    @Test
    fun `일일 누계는 같은 창에서 증가분만 적립한다`() {
        val first = StepAccumulator.applyDailySamples(
            StepCursor.EMPTY,
            bootMark,
            listOf(sample(-3_600_000, 60_000, 8_000)),
        )

        val second = StepAccumulator.applyDailySamples(
            cursor = first.cursor,
            bootMark = bootMark,
            samples = listOf(sample(-3_600_000, 120_000, 8_450)),
        )

        assertEquals(450, second.credited)
        assertEquals(8_450, second.cursor.dailyValue)
    }

    @Test
    fun `자정을 넘겨 창이 바뀌면 누계 전체가 새 걸음이다`() {
        val first = StepAccumulator.applyDailySamples(
            StepCursor.EMPTY,
            bootMark,
            listOf(sample(-3_600_000, 60_000, 8_000)),
        )

        val nextDay = StepAccumulator.applyDailySamples(
            cursor = first.cursor,
            bootMark = bootMark,
            samples = listOf(sample(50_000_000, 50_060_000, 120)),
        )

        assertEquals(120, nextDay.credited)
    }

    @Test
    fun `일일 누계도 같은 배치 재전달에는 적립하지 않는다`() {
        val samples = listOf(sample(-3_600_000, 60_000, 8_000), sample(-3_600_000, 120_000, 8_450))
        val first = StepAccumulator.applyDailySamples(StepCursor.EMPTY, bootMark, samples)

        val second = StepAccumulator.applyDailySamples(first.cursor, bootMark, samples)

        assertEquals(0, second.credited)
    }

    @Test
    fun `일일 누계가 줄어들면 적립하지 않는다`() {
        val first = StepAccumulator.applyDailySamples(
            StepCursor.EMPTY,
            bootMark,
            listOf(sample(-3_600_000, 60_000, 8_000)),
        )
        val second = StepAccumulator.applyDailySamples(
            first.cursor,
            bootMark,
            listOf(sample(-3_600_000, 120_000, 8_450)),
        )

        val decreased = StepAccumulator.applyDailySamples(
            cursor = second.cursor,
            bootMark = bootMark,
            samples = listOf(sample(-3_600_000, 180_000, 8_100)),
        )

        assertEquals(0, decreased.credited)
    }
}
