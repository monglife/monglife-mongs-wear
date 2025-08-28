package com.monglife.mongs.presentation.view.assets

import com.mongs.presentation.view.mobile.R

/**
 * 음식 리소스
 */
enum class FoodResourceCode (
    val code: Int
) {
    FD000(R.drawable.food_fd000),
    FD010(R.drawable.food_fd010),
    FD011(R.drawable.food_fd011),
    FD012(R.drawable.food_fd012),
    FD020(R.drawable.food_fd020),
    FD021(R.drawable.food_fd021),
    FD022(R.drawable.food_fd022),
    FD030(R.drawable.food_fd030),
    FD444(R.drawable.mong_none),
    ;

    companion object {
        fun getResource(code: String) = runCatching { FoodResourceCode.valueOf(code) }.getOrDefault(FD444)
        fun getResourceCode(code: String) = getResource(code = code).code
    }
}