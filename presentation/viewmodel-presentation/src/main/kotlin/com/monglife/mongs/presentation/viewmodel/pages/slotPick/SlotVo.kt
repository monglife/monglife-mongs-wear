package com.monglife.mongs.presentation.viewmodel.pages.slotPick

import com.monglife.mongs.application.mong.vo.MongVo

/**
 * 슬롯 Vo
 */
data class SlotVo(
    val type: SlotType,
    val mongVo: MongVo? = null,
) {
    enum class SlotType {
        EXISTS,
        EMPTY,
        BUY,
    }
}