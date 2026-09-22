package com.monglife.mongs.application.mong.vo

import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.domain.mong.enums.MissionRewardTypeCode
import com.monglife.mongs.domain.mong.model.MissionReward

data class MissionRewardVo(
    val rewardTypeCode: MissionRewardTypeCode,
    val rewardCode: String?,
    val inventoryTypeCode: InventoryTypeCode?,
    val amount: Int,
) {
    companion object {
        /**
         * 도메인 Vo 변환
         */
        fun of(missionReward: MissionReward): MissionRewardVo {
            return MissionRewardVo(
                rewardTypeCode = missionReward.rewardTypeCode,
                rewardCode = missionReward.rewardCode,
                inventoryTypeCode = missionReward.inventoryTypeCode,
                amount = missionReward.amount,
            )
        }
    }
}
