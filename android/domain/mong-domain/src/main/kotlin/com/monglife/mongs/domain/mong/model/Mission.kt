package com.monglife.mongs.domain.mong.model

import com.monglife.mongs.domain.mong.enums.MissionCycleCode
import com.monglife.mongs.domain.mong.enums.MissionGoalTypeCode
import com.monglife.mongs.domain.mong.enums.MissionStateCode
import java.time.LocalDateTime

class Mission(
    accountMissionId: Long,
    missionCode: String,
    cycleCode: MissionCycleCode,
    goalTypeCode: MissionGoalTypeCode,
    title: String,
    description: String?,
    goalCount: Int,
    progressCount: Int,
    stateCode: MissionStateCode,
    claimedAt: LocalDateTime?,
    rewards: List<MissionReward>,
) {
    /** 리워드 수령 시 서버에 넘기는 값 */
    var accountMissionId: Long = accountMissionId
        private set
    var missionCode: String = missionCode
        private set
    var cycleCode: MissionCycleCode = cycleCode
        private set
    var goalTypeCode: MissionGoalTypeCode = goalTypeCode
        private set
    var title: String = title
        private set
    var description: String? = description
        private set
    var goalCount: Int = goalCount
        private set
    var progressCount: Int = progressCount
        private set
    var stateCode: MissionStateCode = stateCode
        private set
    var claimedAt: LocalDateTime? = claimedAt
        private set
    var rewards: List<MissionReward> = rewards
        private set

    /**
     * 진행률. 목표치가 0 인 미션은 서버가 만들지 않지만, 0 나눗셈은 막아 둔다
     */
    fun progressRatio(): Float =
        if (goalCount <= 0) 0f else (progressCount.toFloat() / goalCount).coerceIn(0f, 1f)

    /**
     * 수령 가능 여부
     */
    fun isClaimable(): Boolean = stateCode == MissionStateCode.CLAIMABLE
}
