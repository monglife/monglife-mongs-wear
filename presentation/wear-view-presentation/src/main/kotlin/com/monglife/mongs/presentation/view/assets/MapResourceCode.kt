package com.monglife.mongs.presentation.view.assets

import com.mongs.wear.presentation.view.wear.R

/**
 * 맵 리소스
 */
enum class MapResourceCode (
    val code: Int
) {
    MP000(R.drawable.map_mp000),
    MP001(R.drawable.map_mp001),
    MP002(R.drawable.map_mp002),
    MP003(R.drawable.map_mp003),
    MP004(R.drawable.map_mp004),
    MP005(R.drawable.map_mp005),
    MP006(R.drawable.map_mp006),
    MP007(R.drawable.map_mp007),
    MP008(R.drawable.map_mp008),
    MP009(R.drawable.map_mp009),
    MP010(R.drawable.map_mp010),
    MP011(R.drawable.map_mp011),
    MP012(R.drawable.map_mp012),
    MP013(R.drawable.map_mp013),
    MP014(R.drawable.map_mp014),
    MP015(R.drawable.map_mp015),
    MP016(R.drawable.map_mp016),
    MP017(R.drawable.map_mp017),
    MP018(R.drawable.map_mp018),
    MP019(R.drawable.map_mp019),
    MP020(R.drawable.map_mp020),
    MP023(R.drawable.map_mp023),
    MP026(R.drawable.map_mp026),
    MP027(R.drawable.map_mp027),
    MP028(R.drawable.map_mp028),
    MP029(R.drawable.map_mp029),
    MP030(R.drawable.map_mp030),
    MP031(R.drawable.map_mp031),
    MP032(R.drawable.map_mp032),
    MP037(R.drawable.map_mp037),
    MP042(R.drawable.map_mp042),
    MP444(R.drawable.map_mp000),
    ;

    companion object {
        fun getResource(code: String) = runCatching { MapResourceCode.valueOf(code) }.getOrDefault(MP444)
        fun getResourceCode(code: String) = getResource(code = code).code
    }
}