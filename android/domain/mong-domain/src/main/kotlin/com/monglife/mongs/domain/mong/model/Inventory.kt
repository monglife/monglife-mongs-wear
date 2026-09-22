package com.monglife.mongs.domain.mong.model

import com.monglife.mongs.domain.mong.enums.InventoryTypeCode

class Inventory(
    inventoryId: Long,
    mongId: Long,
    inventoryCode: String,
    inventoryName: String,
    inventoryTypeCode: InventoryTypeCode,
) {
    var inventoryId: Long = inventoryId
        private set
    var mongId: Long = mongId
        private set
    var inventoryCode: String = inventoryCode
        private set
    var inventoryName: String = inventoryName
        private set
    var inventoryTypeCode: InventoryTypeCode = inventoryTypeCode
        private set
}