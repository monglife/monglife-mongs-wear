package com.monglife.mongs.presentation.view.assets

import com.mongs.presentation.view.mobile.R

/**
 * 도움말 리소스
 */
enum class HelpResourceCode (
    val code: Int
) {
    HP000(R.drawable.icon_logo_not_open),
    HP001(R.drawable.btn_icon_collection),
    HP002(R.drawable.btn_icon_luck_draw),
    HP003(R.drawable.btn_icon_activity),
    HP004(R.drawable.btn_icon_battle),
    HP005(R.drawable.mong_body_ch100),
    HP006(R.drawable.btn_icon_slot_pick),
    HP007(R.drawable.point_icon_pay),
    HP444(R.drawable.mong_none)
    ;

    companion object {
        fun getResource(code: String) = runCatching { HelpResourceCode.valueOf(code) }.getOrDefault(HP444)
        fun getResourceCode(code: String) = getResource(code = code).code
    }
}