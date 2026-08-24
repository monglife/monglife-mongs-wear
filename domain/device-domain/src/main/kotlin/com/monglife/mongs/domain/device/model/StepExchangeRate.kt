package com.monglife.mongs.domain.device.model

/**
 * 걸음 수 환전 환율
 *
 * 1000 걸음 = 100 payPoint. 이 값이 ViewModel 과 View 에 흩어져 있으면 화면에 표시한 금액과
 * 실제 요청 금액이 어긋날 수 있어 한곳에 모은다.
 */
object StepExchangeRate {

    const val WALKING_COUNT_PER_UNIT = 1_000
    const val PAY_POINT_PER_UNIT = 100

    /** 잔액으로 환전 가능한 최대 단위 수 */
    fun maxExchangeableUnits(walkingCount: Int): Int =
        (walkingCount / WALKING_COUNT_PER_UNIT).coerceAtLeast(0)

    fun payPointOf(units: Int): Int = units.coerceAtLeast(0) * PAY_POINT_PER_UNIT

    fun walkingCountOf(units: Int): Int = units.coerceAtLeast(0) * WALKING_COUNT_PER_UNIT
}
