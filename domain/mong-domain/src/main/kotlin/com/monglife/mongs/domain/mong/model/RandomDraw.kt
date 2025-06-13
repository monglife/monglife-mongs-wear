package com.monglife.mongs.domain.mong.model

import com.monglife.mongs.domain.mong.enums.InventoryTypeCode

class RandomDraw(
    randomDrawCode: String,
    randomDrawName: String,
    inventoryTypeCode: InventoryTypeCode,
) {
    var randomDrawCode: String = randomDrawCode
        private set
    var randomDrawName: String = randomDrawName
        private set
    var inventoryTypeCode: InventoryTypeCode = inventoryTypeCode
        private set
}
