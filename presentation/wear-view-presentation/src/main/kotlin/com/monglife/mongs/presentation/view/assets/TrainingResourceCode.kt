package com.monglife.mongs.presentation.view.assets

import com.mongs.presentation.view.wear.R

/**
 * 훈련 리소스
 */
enum class TrainingResourceCode (
    val iconCode: Int,
    val borderCode: Int,
    val routerPath: String,
) {
    TR000(R.drawable.btn_icon_runner, R.drawable.btn_border_green, "${RouterPath.TrainingPlay.route}/TR000"),
    TR001(R.drawable.btn_icon_basketball, R.drawable.btn_border_orange, "${RouterPath.TrainingPlay.route}/TR001"),
    TR002(R.drawable.btn_icon_rock_pager_scissors, R.drawable.btn_border_orange, "${RouterPath.TrainingPlay.route}/TR002"),
    TR003(R.drawable.btn_icon_soccer, R.drawable.btn_border_purple, "${RouterPath.TrainingPlay.route}/TR003"),
    TR004(R.drawable.btn_icon_cham, R.drawable.btn_border_yellow, "${RouterPath.TrainingPlay.route}/TR004"),
    TR444(R.drawable.mong_none, R.drawable.btn_border_purple, RouterPath.TrainingNested.route)
    ;

    companion object {
        fun getResource(code: String) = runCatching { TrainingResourceCode.valueOf(code) }.getOrDefault(TR444)
        fun getResourceCode(code: String) = getResource(code = code).iconCode
    }
}