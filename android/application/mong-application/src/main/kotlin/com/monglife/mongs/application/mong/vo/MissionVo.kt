package com.monglife.mongs.application.mong.vo

import com.monglife.mongs.domain.mong.enums.MissionCycleCode
import com.monglife.mongs.domain.mong.enums.MissionGoalTypeCode
import com.monglife.mongs.domain.mong.enums.MissionStateCode
import com.monglife.mongs.domain.mong.model.Mission

data class MissionVo(
    val accountMissionId: Long,
    val missionCode: String,
    val cycleCode: MissionCycleCode,
    val goalTypeCode: MissionGoalTypeCode,
    val title: String,
    val description: String?,
    val goalCount: Int,
    val progressCount: Int,
    val stateCode: MissionStateCode,
    val progressRatio: Float,
    val isClaimable: Boolean,
    val rewards: List<MissionRewardVo>,
) {
    companion object {
        /**
         * 도메인 Vo 변환
         */
        fun of(mission: Mission): MissionVo {
            return MissionVo(
                accountMissionId = mission.accountMissionId,
                missionCode = mission.missionCode,
                cycleCode = mission.cycleCode,
                goalTypeCode = mission.goalTypeCode,
                title = mission.title,
                description = mission.description,
                goalCount = mission.goalCount,
                progressCount = mission.progressCount,
                stateCode = mission.stateCode,
                progressRatio = mission.progressRatio(),
                isClaimable = mission.isClaimable(),
                rewards = mission.rewards.map { MissionRewardVo.of(missionReward = it) },
            )
        }
    }
}
