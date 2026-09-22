package com.monglife.mongs.data.device.persistence.entity

import com.monglife.mongs.domain.device.model.Step
import com.monglife.mongs.domain.device.model.StepCursor
import com.monglife.mongs.domain.device.model.StepSource

/**
 * DataStore 에 저장되는 걸음 수 상태 전부
 *
 * source/cursor 는 도메인 타입을 그대로 쓴다. 둘 다 프레임워크 의존이 없는 순수 값이라
 * 여기서 한 번 더 원시 타입으로 풀어 놓으면 매핑 실수만 늘어난다.
 */
data class StepStateEntity(
    val balance: Int,
    val source: StepSource,
    val cursor: StepCursor,
) {
    fun toDomain(pendingWalkingCount: Int = 0): Step = Step(
        walkingCount = balance,
        pendingWalkingCount = pendingWalkingCount,
        available = source.isCollecting(),
    )

    companion object {
        val EMPTY = StepStateEntity(
            balance = 0,
            source = StepSource.UNRESOLVED,
            cursor = StepCursor.EMPTY,
        )
    }
}
