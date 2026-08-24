package com.monglife.mongs.domain.device.model

/**
 * Health Services 의 `IntervalDataPoint<Long>` 을 도메인으로 옮긴 값
 *
 * 도메인 모듈은 의존성이 없어야 하므로 androidx 타입을 그대로 들이지 않는다.
 * Duration 은 전부 "부팅 이후 경과 밀리초" 다 - 벽시계가 아니다.
 */
data class HealthStepSample(
    val steps: Long,
    val startDurationFromBootMillis: Long,
    val endDurationFromBootMillis: Long,
)
