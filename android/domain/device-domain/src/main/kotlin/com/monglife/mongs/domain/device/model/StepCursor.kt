package com.monglife.mongs.domain.device.model

import kotlin.math.abs

/**
 * 걸음 수 적립 커서
 *
 * 같은 걸음을 두 번 적립하지 않기 위한 상태다. 세 수집 경로가 각자 쓰는 칸을 나눠 갖고,
 * 재부팅 감지에 쓰는 [bootMarkMillis] 만 공유한다.
 *
 * @param bootMarkMillis 부팅 시각. `System.currentTimeMillis() - SystemClock.elapsedRealtime()` 을
 *        [BOOT_MARK_GRANULARITY_MILLIS] 단위로 절삭한 값이다. 절삭이 필요한 이유는 이 뺄셈이
 *        호출할 때마다 수십 ms 씩 흔들리기 때문이다(NTP 보정, 스케줄러 지터). 절삭 없이 비교하면
 *        매 호출이 "재부팅"으로 오판된다. -1 = 미초기화.
 * @param lastEndDurationFromBootMillis Health Services 커서. 부팅 이후 경과 시간이라 벽시계와
 *        무관하게 같은 부팅 세션 안에서 정확하고 단조 증가한다. -1 = 미초기화.
 * @param dailyWindowStartFromBootMillis STEPS_DAILY 의 "오늘" 창을 식별하는 키(창의 시작 시각).
 *        "오늘"은 대개 부팅보다 먼저 시작하므로 이 값은 음수가 정상이다. 그래서 미초기화 센티널로
 *        -1 을 쓸 수 없고 [DAILY_WINDOW_UNSET] 을 따로 둔다.
 * @param dailyValue 그 창에서 마지막으로 관측한 일일 누계.
 * @param lastSensorTotal TYPE_STEP_COUNTER 폴백의 마지막 관측 누계. -1 = 미초기화.
 */
data class StepCursor(
    val bootMarkMillis: Long,
    val lastEndDurationFromBootMillis: Long,
    val dailyWindowStartFromBootMillis: Long,
    val dailyValue: Int,
    val lastSensorTotal: Int,
) {
    companion object {
        const val BOOT_MARK_GRANULARITY_MILLIS = 10_000L

        /** 일일 창 미초기화 센티널. 실제 창 시작 시각이 음수일 수 있어 -1 은 쓸 수 없다. */
        const val DAILY_WINDOW_UNSET = Long.MIN_VALUE

        /** 절삭 후에도 남는 흔들림을 흡수하는 폭. 이 값을 넘게 벌어지면 실제 재부팅으로 본다. */
        const val REBOOT_THRESHOLD_MILLIS = 30_000L

        val EMPTY = StepCursor(
            bootMarkMillis = -1L,
            lastEndDurationFromBootMillis = -1L,
            dailyWindowStartFromBootMillis = DAILY_WINDOW_UNSET,
            dailyValue = 0,
            lastSensorTotal = -1,
        )

        fun bootMarkOf(currentTimeMillis: Long, elapsedRealtimeMillis: Long): Long =
            Math.floorDiv(currentTimeMillis - elapsedRealtimeMillis, BOOT_MARK_GRANULARITY_MILLIS) *
                    BOOT_MARK_GRANULARITY_MILLIS
    }

    fun isUninitialized(): Boolean = bootMarkMillis < 0

    fun isRebooted(bootMark: Long): Boolean =
        !isUninitialized() && abs(bootMark - bootMarkMillis) > REBOOT_THRESHOLD_MILLIS

    /**
     * 부팅 세션이 바뀌었으면 세션에 매인 값들을 비운다.
     * 경과 시간 기반 커서와 센서 누계는 재부팅과 함께 0 부터 다시 시작하므로 그대로 두면 비교가 무의미하다.
     */
    fun resetForNewBootSession(bootMark: Long): StepCursor = StepCursor(
        bootMarkMillis = bootMark,
        lastEndDurationFromBootMillis = -1L,
        dailyWindowStartFromBootMillis = DAILY_WINDOW_UNSET,
        dailyValue = 0,
        lastSensorTotal = -1,
    )

    fun isDailyWindowUnset(): Boolean = dailyWindowStartFromBootMillis == DAILY_WINDOW_UNSET
}
