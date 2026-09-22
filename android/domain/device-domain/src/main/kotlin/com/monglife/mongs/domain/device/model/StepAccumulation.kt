package com.monglife.mongs.domain.device.model

/**
 * 적립 계산 결과
 *
 * @param credited 이번에 지갑에 더할 걸음 수 (항상 0 이상)
 * @param cursor 다음 계산에 쓸 커서
 */
data class StepAccumulation(
    val credited: Int,
    val cursor: StepCursor,
)
