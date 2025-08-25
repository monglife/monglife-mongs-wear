package com.monglife.mongs.presentation.view.assets

import com.mongs.presentation.view.wear.R

/**
 * 간식 리소스
 */
enum class SnackResourceCode (
    val code: Int,
) {
    SN000(R.drawable.snack_sn000),
    SN001(R.drawable.snack_sn001),
    SN002(R.drawable.snack_sn002),
    SN010(R.drawable.snack_sn010),
    SN011(R.drawable.snack_sn011),
    SN012(R.drawable.snack_sn012),
    SN013(R.drawable.snack_sn013),
    SN444(R.drawable.mong_none),
    ;

    companion object {
        fun getResource(code: String) = runCatching { SnackResourceCode.valueOf(code) }.getOrDefault(SN444)
        fun getResourceCode(code: String) = getResource(code = code).code
    }
}