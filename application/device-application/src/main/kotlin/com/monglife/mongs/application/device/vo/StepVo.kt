package com.monglife.mongs.application.device.vo

import com.monglife.mongs.domain.device.model.Step

/**
 * 화면에 보여 줄 걸음 수
 *
 * @param walkingCount 표시용 걸음 수 (확정 잔액 + 아직 지갑에 안 들어온 실시간 걸음)
 * @param exchangeableWalkingCount 환전에 쓸 수 있는 확정 잔액
 * @param available 걸음 수집이 동작 중인지. false 면 숫자 대신 "-" 를 그린다.
 */
data class StepVo(
    val walkingCount: Int,
    val exchangeableWalkingCount: Int,
    val available: Boolean,
) {
    companion object {
        fun of(step: Step): StepVo = StepVo(
            walkingCount = step.getCurrentWalkingCount(),
            exchangeableWalkingCount = step.walkingCount,
            available = step.available,
        )
    }
}
