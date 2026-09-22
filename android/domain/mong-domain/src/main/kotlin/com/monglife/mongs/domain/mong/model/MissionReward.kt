package com.monglife.mongs.domain.mong.model

import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.domain.mong.enums.MissionRewardTypeCode

class MissionReward(
    rewardTypeCode: MissionRewardTypeCode,
    rewardCode: String?,
    inventoryTypeCode: InventoryTypeCode?,
    amount: Int,
) {
    var rewardTypeCode: MissionRewardTypeCode = rewardTypeCode
        private set

    /** INVENTORY 일 때만 채워진다 */
    var rewardCode: String? = rewardCode
        private set

    /** INVENTORY 일 때만 채워진다 */
    var inventoryTypeCode: InventoryTypeCode? = inventoryTypeCode
        private set

    var amount: Int = amount
        private set
}
