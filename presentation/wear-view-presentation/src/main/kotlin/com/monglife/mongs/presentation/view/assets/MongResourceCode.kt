package com.monglife.mongs.presentation.view.assets

import com.mongs.wear.presentation.view.wear.R

/**
 * 몽 리소스
 */
enum class MongResourceCode(
    val pngCode: Int,
    val gifCode: Int,
    val yOffset: Int,
    val xOffset: Int,
    val hasExpression: Boolean,
) {
    CH000(R.drawable.mong_body_ch000, R.drawable.mong_body_ch000_gif, 0, 0, false),
    CH001(R.drawable.mong_body_ch001, R.drawable.mong_body_ch001_gif, 0, 0, false),
    CH002(R.drawable.mong_body_ch002, R.drawable.mong_body_ch002_gif, 0, 0, false),
    CH003(R.drawable.mong_body_ch003, R.drawable.mong_body_ch003_gif, 0, 0, false),
    CH004(R.drawable.mong_body_ch004, R.drawable.mong_body_ch004_gif, 0, 0, false),
    CH005(R.drawable.mong_body_ch005, R.drawable.mong_body_ch005_gif, 0, 0, false),

    CH100(R.drawable.mong_body_ch100, R.drawable.mong_body_ch100_gif, -4, 0, true),
    CH101(R.drawable.mong_body_ch101, R.drawable.mong_body_ch101_gif, -2, 0, true),
    CH102(R.drawable.mong_body_ch102, R.drawable.mong_body_ch102_gif, -4, 0, true),
    CH104(R.drawable.mong_body_ch104, R.drawable.mong_body_ch104, 0, 0, false),
    CH105(R.drawable.mong_body_ch105, R.drawable.mong_body_ch105, 0, 0, false),

    CH200(R.drawable.mong_body_ch200, R.drawable.mong_body_ch200_gif, -16, 0, true),
    CH201(R.drawable.mong_body_ch201, R.drawable.mong_body_ch201_gif, -13, -11, true),
    CH202(R.drawable.mong_body_ch202, R.drawable.mong_body_ch202_gif, -16, 0, true),
    CH203(R.drawable.mong_body_ch203, R.drawable.mong_body_ch203, 0, 0, false),
    CH204(R.drawable.mong_body_ch204, R.drawable.mong_body_ch204, 0, 0, false),
    CH205(R.drawable.mong_body_ch205, R.drawable.mong_body_ch205, 0, 0, false),

    CH210(R.drawable.mong_body_ch210, R.drawable.mong_body_ch210_gif, -16, 0, true),
    CH211(R.drawable.mong_body_ch211, R.drawable.mong_body_ch211_gif, -13, -11, true),
    CH212(R.drawable.mong_body_ch212, R.drawable.mong_body_ch212_gif, -16, 0, true),
    CH214(R.drawable.mong_body_ch214, R.drawable.mong_body_ch214, 0, 0, false),
    CH215(R.drawable.mong_body_ch215, R.drawable.mong_body_ch215, 0, 0, false),

    CH220(R.drawable.mong_body_ch220, R.drawable.mong_body_ch220_gif, -16, 0, true),
    CH221(R.drawable.mong_body_ch221, R.drawable.mong_body_ch221_gif, -13, -11, true),
    CH222(R.drawable.mong_body_ch222, R.drawable.mong_body_ch222_gif, -16, 0, true),

    CH230(R.drawable.mong_body_ch230, R.drawable.mong_body_ch230_gif, -16, 0, true),
    CH231(R.drawable.mong_body_ch231, R.drawable.mong_body_ch231_gif, -13, -11, true),
    CH232(R.drawable.mong_body_ch232, R.drawable.mong_body_ch232_gif, -16, 0, true),

    CH300(R.drawable.mong_body_ch300, R.drawable.mong_body_ch300_gif, -15, 0, true),
    CH301(R.drawable.mong_body_ch301, R.drawable.mong_body_ch301_gif, -13, -23, true),
    CH302(R.drawable.mong_body_ch302, R.drawable.mong_body_ch302_gif, -8, 0, true),
    CH303(R.drawable.mong_body_ch303, R.drawable.mong_body_ch303, 0, 0, false),
    CH304(R.drawable.mong_body_ch304, R.drawable.mong_body_ch304, 0, 0, false),
    CH305(R.drawable.mong_body_ch305, R.drawable.mong_body_ch305, 0, 0, false),

    CH310(R.drawable.mong_body_ch310, R.drawable.mong_body_ch310_gif, -15, 0, true),
    CH311(R.drawable.mong_body_ch311, R.drawable.mong_body_ch311_gif, -13, -23, true),
    CH312(R.drawable.mong_body_ch312, R.drawable.mong_body_ch312_gif, -8, 0, true),
    CH314(R.drawable.mong_body_ch314, R.drawable.mong_body_ch314, 0, 0, false),
    CH315(R.drawable.mong_body_ch315, R.drawable.mong_body_ch315, 0, 0, false),

    CH320(R.drawable.mong_body_ch320, R.drawable.mong_body_ch320_gif, -15, 0, true),
    CH321(R.drawable.mong_body_ch321, R.drawable.mong_body_ch321_gif, -13, -23, true),
    CH322(R.drawable.mong_body_ch322, R.drawable.mong_body_ch322_gif, -8, 0, true),
    CH324(R.drawable.mong_body_ch324, R.drawable.mong_body_ch324, 0, 0, false),
    CH325(R.drawable.mong_body_ch325, R.drawable.mong_body_ch325, 0, 0, false),

    CH330(R.drawable.mong_body_ch330, R.drawable.mong_body_ch330_gif, -15, 0, true),
    CH331(R.drawable.mong_body_ch331, R.drawable.mong_body_ch331_gif, -13, -23, true),
    CH332(R.drawable.mong_body_ch332, R.drawable.mong_body_ch332_gif, -8, 0, true),
    CH334(R.drawable.mong_body_ch334, R.drawable.mong_body_ch334, 0, 0, false),
    CH335(R.drawable.mong_body_ch335, R.drawable.mong_body_ch335, 0, 0, false),

    CH444(R.drawable.mong_none, R.drawable.mong_none, 0, 0, false),
    ;

    companion object {
        fun getResource(code: String) = runCatching { MongResourceCode.valueOf(code) }.getOrDefault(CH444)
        fun getResourceCode(code: String) = getResource(code = code).gifCode
    }
}

