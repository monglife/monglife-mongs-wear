package com.monglife.mongs.application.mong.vo

import com.monglife.mongs.domain.mong.enums.InventoryTypeCode
import com.monglife.mongs.domain.mong.model.Inventory

data class InventoryVo(

    val inventoryId: Long,

    val mongId: Long,

    val inventoryCode: String,

    val inventoryName: String,

    val inventoryTypeCode: InventoryTypeCode,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(inventory: Inventory): InventoryVo {
            return InventoryVo(
                inventoryId = inventory.inventoryId,
                mongId = inventory.mongId,
                inventoryCode = inventory.inventoryCode,
                inventoryName = inventory.inventoryName,
                inventoryTypeCode = inventory.inventoryTypeCode,
            )
        }
    }
}