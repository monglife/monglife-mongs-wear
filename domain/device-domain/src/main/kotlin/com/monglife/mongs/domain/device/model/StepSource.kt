package com.monglife.mongs.domain.device.model

/**
 * 걸음 수 수집 경로
 *
 * 동시에 두 경로가 적립하면 그대로 이중 카운트가 되므로, 언제나 정확히 하나만 활성이다.
 */
enum class StepSource {
    /** 아직 어떤 경로를 쓸지 결정하지 않음 */
    UNRESOLVED,

    /** Health Services passive - DataType.STEPS (구간 delta) */
    HEALTH_STEPS,

    /** Health Services passive - DataType.STEPS_DAILY (자정부터의 일일 누계) */
    HEALTH_DAILY_STEPS,

    /** Sensor.TYPE_STEP_COUNTER 폴링 */
    SENSOR,

    /** 수집 불가 (권한 없음 / 센서 없음 / Health Services 미가용) */
    NONE,
    ;

    fun isCollecting(): Boolean = this != UNRESOLVED && this != NONE

    fun isHealthServices(): Boolean = this == HEALTH_STEPS || this == HEALTH_DAILY_STEPS
}
