package com.monglife.mongs.domain.device.model

/**
 * 걸음 수 적립 계산
 *
 * 세 수집 경로가 각각 다른 모양의 입력을 주지만, 지갑에 더할 값을 뽑아내는 규칙은 전부 여기 모여 있다.
 * 순수 함수라 JVM 단위 테스트로 전부 덮을 수 있고, 실제로 이중 카운트/유실 버그가 날 수 있는 지점도
 * 여기뿐이다.
 */
object StepAccumulator {

    /**
     * 한 번에 인정하는 걸음 수 상한.
     * 센서 이상값이나 깨진 배치가 지갑을 오염시키는 것을 막는 안전판이다.
     * 15분 주기 폴링 기준으로도 사람이 이만큼 걸을 수는 없다.
     */
    const val MAX_CREDIT_PER_BATCH = 100_000

    /**
     * Health Services `DataType.STEPS` - 구간 delta
     *
     * 멱등한 이유: `endDurationFromBoot` 는 같은 부팅 세션에서 단조 증가하고, 같은 데이터포인트가
     * 다시 내려와도 값이 동일하다. 따라서 "커서 이하는 이미 반영됐다"로 판정하면 재전달된 배치는
     * 0 을 적립한다. Health Services 가 at-least-once 인지 문서에 명시가 없어 at-least-once 로 가정했다.
     */
    fun applyDeltaSamples(
        cursor: StepCursor,
        bootMark: Long,
        samples: List<HealthStepSample>,
    ): StepAccumulation {
        var current = cursor.forBootSession(bootMark)
        var credited = 0L

        samples.sortedBy { it.endDurationFromBootMillis }.forEach { sample ->
            if (sample.endDurationFromBootMillis <= current.lastEndDurationFromBootMillis) return@forEach
            if (sample.steps > 0L) credited += sample.steps
            current = current.copy(lastEndDurationFromBootMillis = sample.endDurationFromBootMillis)
        }

        return StepAccumulation(credited = clamp(credited), cursor = current)
    }

    /**
     * Health Services `DataType.STEPS_DAILY` - 자정부터의 일일 누계
     *
     * 모든 Wear OS 기기가 지원해야 하는 타입이라 delta 타입인 STEPS 를 못 쓸 때의 대안이다.
     * 값이 누계라서 그대로 더하면 안 되고, 같은 "오늘" 창 안에서는 증가분만 인정해야 한다.
     *
     * 창을 처음 관측했을 때 적립을 0 으로 두는 것이 중요하다. 낮에 앱을 처음 켰다면 그 시점의
     * 일일 누계에는 앱을 켜기 전에 걸은 걸음이 전부 들어 있는데, 그것까지 지갑에 넣으면 안 된다.
     * 자정을 넘겨 창이 바뀐 경우는 그 사이 계속 등록되어 있었다는 뜻이므로 값 전체가 새 걸음이다.
     */
    fun applyDailySamples(
        cursor: StepCursor,
        bootMark: Long,
        samples: List<HealthStepSample>,
    ): StepAccumulation {
        var current = cursor.forBootSession(bootMark)
        var credited = 0L

        samples.sortedBy { it.endDurationFromBootMillis }.forEach { sample ->
            if (sample.endDurationFromBootMillis <= current.lastEndDurationFromBootMillis) return@forEach

            val total = sample.steps.coerceAtLeast(0L)
            val window = sample.startDurationFromBootMillis

            credited += when {
                // 이 창을 처음 본다 - 기준선만 잡고 적립하지 않는다.
                current.isDailyWindowUnset() -> 0L
                // 같은 창 - 지난번 관측 이후 늘어난 만큼만.
                current.dailyWindowStartFromBootMillis == window -> (total - current.dailyValue).coerceAtLeast(0L)
                // 창이 바뀌었다(자정 넘김) - 오늘 걸은 만큼 전부.
                else -> total
            }

            current = current.copy(
                lastEndDurationFromBootMillis = sample.endDurationFromBootMillis,
                dailyWindowStartFromBootMillis = window,
                dailyValue = total.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
            )
        }

        return StepAccumulation(credited = clamp(credited), cursor = current)
    }

    /**
     * `Sensor.TYPE_STEP_COUNTER` 폴백 - 부팅 이후 누계
     *
     * 재부팅되면 카운터가 0 부터 다시 시작하므로, 재부팅 후 관측한 누계는 전부 사용자가 실제로 걸은
     * 걸음이다. 반대로 앱이 이 기기에서 처음 도는 경우의 누계는 설치 이전 걸음까지 포함하므로
     * 기준선으로만 쓰고 적립하지 않는다.
     */
    fun applySensorTotal(
        cursor: StepCursor,
        bootMark: Long,
        sensorTotal: Int,
    ): StepAccumulation {
        val uninitialized = cursor.isUninitialized()
        val current = cursor.forBootSession(bootMark)

        val credited = when {
            // 이 기기에서 처음 - 지금 값은 설치 이전 걸음이므로 기준선으로만 쓴다.
            uninitialized -> 0L
            // 재부팅 직후 첫 관측 - 커서가 비워졌고, 부팅 이후 누계는 전부 새 걸음이다.
            current.lastSensorTotal < 0 -> sensorTotal.toLong()
            // 센서 역행. 부팅 마크는 그대로인데 값이 줄었다면 비정상이므로 기준선만 다시 잡는다.
            sensorTotal < current.lastSensorTotal -> 0L
            else -> sensorTotal.toLong() - current.lastSensorTotal
        }

        return StepAccumulation(
            credited = clamp(credited),
            cursor = current.copy(lastSensorTotal = sensorTotal.coerceAtLeast(0)),
        )
    }

    private fun StepCursor.forBootSession(bootMark: Long): StepCursor = when {
        isRebooted(bootMark) -> resetForNewBootSession(bootMark)
        isUninitialized() -> StepCursor.EMPTY.copy(bootMarkMillis = bootMark)
        else -> copy(bootMarkMillis = bootMark)
    }

    private fun clamp(credited: Long): Int =
        credited.coerceIn(0L, MAX_CREDIT_PER_BATCH.toLong()).toInt()
}
