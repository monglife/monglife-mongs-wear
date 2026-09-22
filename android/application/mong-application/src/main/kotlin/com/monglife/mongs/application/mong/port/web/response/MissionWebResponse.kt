package com.monglife.mongs.application.mong.port.web.response

import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.domain.mong.enums.MissionCycleCode
import com.monglife.mongs.domain.mong.enums.MissionGoalTypeCode
import com.monglife.mongs.domain.mong.enums.MissionRewardTypeCode
import com.monglife.mongs.domain.mong.enums.MissionStateCode
import com.monglife.mongs.domain.mong.model.Mission
import com.monglife.mongs.domain.mong.model.MissionReward
import java.time.LocalDateTime

/**
 * 미션 보상 조회 응답
 */
data class GetMissionRewardResponse(
    val rewardTypeCode: MissionRewardTypeCode,
    val rewardCode: String?,
    val inventoryTypeCode: InventoryTypeCode?,
    val amount: Int,
) {
    fun toDomain(): MissionReward {
        return MissionReward(
            rewardTypeCode = rewardTypeCode,
            rewardCode = rewardCode,
            inventoryTypeCode = inventoryTypeCode,
            amount = amount,
        )
    }
}

/**
 * 미션 조회 응답
 */
data class GetMissionResponse(
    val accountMissionId: Long,
    val missionCode: String,
    val cycleCode: MissionCycleCode,
    val goalTypeCode: MissionGoalTypeCode,
    val title: String,
    val description: String?,
    val goalCount: Int,
    val progressCount: Int,
    val stateCode: MissionStateCode,
    val claimedAt: LocalDateTime?,
    val rewards: List<GetMissionRewardResponse>,
) {
    fun toDomain(): Mission {
        return Mission(
            accountMissionId = accountMissionId,
            missionCode = missionCode,
            cycleCode = cycleCode,
            goalTypeCode = goalTypeCode,
            title = title,
            description = description,
            goalCount = goalCount,
            progressCount = progressCount,
            stateCode = stateCode,
            claimedAt = claimedAt,
            rewards = rewards.map { it.toDomain() },
        )
    }
}

/**
 * 미션 보상 수령 응답
 */
data class ClaimMissionRewardResponse(
    val accountMissionId: Long,
    val mongId: Long,
    val expRatio: Double,
    val payPoint: Int,
)
