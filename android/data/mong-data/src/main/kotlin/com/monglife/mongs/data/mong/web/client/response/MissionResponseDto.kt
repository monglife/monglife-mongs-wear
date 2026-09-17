package com.monglife.mongs.data.mong.web.client.response

import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.domain.mong.enums.MissionCycleCode
import com.monglife.mongs.domain.mong.enums.MissionGoalTypeCode
import com.monglife.mongs.domain.mong.enums.MissionRewardTypeCode
import com.monglife.mongs.domain.mong.enums.MissionStateCode
import java.time.LocalDateTime

/**
 * 미션 리워드 조회 응답 Dto
 */
data class GetMissionRewardResponseDto(
    val rewardTypeCode: MissionRewardTypeCode,
    val rewardCode: String?,
    val inventoryTypeCode: InventoryTypeCode?,
    val amount: Int,
)

/**
 * 미션 조회 응답 Dto
 */
data class GetMissionResponseDto(
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
    val rewards: List<GetMissionRewardResponseDto>,
)

/**
 * 미션 리워드 수령 응답 Dto
 */
data class ClaimMissionRewardResponseDto(
    val accountMissionId: Long,
    val mongId: Long,
    val expRatio: Double,
    val payPoint: Int,
    val updatedAt: LocalDateTime?,
)
