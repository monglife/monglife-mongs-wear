package com.monglife.mongs.presentation.view.assets

import com.mongs.presentation.view.mobile.R

/**
 * 몽 리소스
 */
enum class MongResourceCode(
    val pngCode: Int,
    val gifCode: Int,
    val hasExpression: Boolean,
    val sadGifCode: Int,
    val depressedGifCode: Int,
    val sulkyGifCode: Int,
    val happyGifCode: Int,
    val eatingGifCode: Int,
    val sleepingGifCode: Int,
    val smileGifCode: Int,
    val yOffset: Int,
    val xOffset: Int,
) {    
    CH000(R.drawable.mong_body_ch000, R.drawable.mong_body_ch000_gif, false, -1, -1, -1, -1, -1, -1, -1,0, 0),
    CH001(R.drawable.mong_body_ch001, R.drawable.mong_body_ch001_gif, false, -1, -1, -1, -1, -1, -1, -1,0, 0),
    CH002(R.drawable.mong_body_ch002, R.drawable.mong_body_ch002_gif, false, -1, -1, -1, -1, -1, -1, -1,0, 0),
    CH003(R.drawable.mong_body_ch003, R.drawable.mong_body_ch003_gif, false, -1, -1, -1, -1, -1, -1, -1,0, 0),
    CH004(R.drawable.mong_body_ch004, R.drawable.mong_body_ch004_gif, false, -1, -1, -1, -1, -1, -1, -1,0, 0),
    CH005(R.drawable.mong_body_ch005, R.drawable.mong_body_ch005_gif, false, -1, -1, -1, -1, -1, -1, -1,0, 0),

    CH100(R.drawable.mong_body_ch100, R.drawable.mong_body_ch100_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-4, 0),
    CH101(R.drawable.mong_body_ch101, R.drawable.mong_body_ch101_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-2, 0),
    CH102(R.drawable.mong_body_ch102, R.drawable.mong_body_ch102_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-4, 0),
    CH104(R.drawable.mong_body_ch104, R.drawable.mong_body_ch104, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),
    CH105(R.drawable.mong_body_ch105, R.drawable.mong_body_ch105, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),

    CH200(R.drawable.mong_body_ch200, R.drawable.mong_body_ch200_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-16, 0),
    CH201(R.drawable.mong_body_ch201, R.drawable.mong_body_ch201_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-13, -11),
    CH202(R.drawable.mong_body_ch202, R.drawable.mong_body_ch202_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-16, 0),
    CH203(R.drawable.mong_body_ch203, R.drawable.mong_body_ch203, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),
    CH204(R.drawable.mong_body_ch204, R.drawable.mong_body_ch204, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),
    CH205(R.drawable.mong_body_ch205, R.drawable.mong_body_ch205, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),

    CH210(R.drawable.mong_body_ch210, R.drawable.mong_body_ch210_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-16, 0),
    CH211(R.drawable.mong_body_ch211, R.drawable.mong_body_ch211_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-13, -11),
    CH212(R.drawable.mong_body_ch212, R.drawable.mong_body_ch212_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-16, 0),
    CH214(R.drawable.mong_body_ch214, R.drawable.mong_body_ch214, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),
    CH215(R.drawable.mong_body_ch215, R.drawable.mong_body_ch215, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),

    CH220(R.drawable.mong_body_ch220, R.drawable.mong_body_ch220_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-16, 0),
    CH221(R.drawable.mong_body_ch221, R.drawable.mong_body_ch221_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-13, -11),
    CH222(R.drawable.mong_body_ch222, R.drawable.mong_body_ch222_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-16, 0),

    CH230(R.drawable.mong_body_ch230, R.drawable.mong_body_ch230_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-16, 0),
    CH231(R.drawable.mong_body_ch231, R.drawable.mong_body_ch231_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-13, -11),
    CH232(R.drawable.mong_body_ch232, R.drawable.mong_body_ch232_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-16, 0),

    CH300(R.drawable.mong_body_ch300, R.drawable.mong_body_ch300_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-15, 0),
    CH301(R.drawable.mong_body_ch301, R.drawable.mong_body_ch301_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-13, -23),
    CH302(R.drawable.mong_body_ch302, R.drawable.mong_body_ch302_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-8, 0),
    CH303(R.drawable.mong_body_ch303, R.drawable.mong_body_ch303, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),
    CH304(R.drawable.mong_body_ch304, R.drawable.mong_body_ch304, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),
    CH305(R.drawable.mong_body_ch305, R.drawable.mong_body_ch305, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),

    CH310(R.drawable.mong_body_ch310, R.drawable.mong_body_ch310_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-15, 0),
    CH311(R.drawable.mong_body_ch311, R.drawable.mong_body_ch311_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-13, -23),
    CH312(R.drawable.mong_body_ch312, R.drawable.mong_body_ch312_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-8, 0),
    CH314(R.drawable.mong_body_ch314, R.drawable.mong_body_ch314, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),
    CH315(R.drawable.mong_body_ch315, R.drawable.mong_body_ch315, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),

    CH320(R.drawable.mong_body_ch320, R.drawable.mong_body_ch320_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-15, 0),
    CH321(R.drawable.mong_body_ch321, R.drawable.mong_body_ch321_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-13, -23),
    CH322(R.drawable.mong_body_ch322, R.drawable.mong_body_ch322_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-8, 0),
    CH324(R.drawable.mong_body_ch324, R.drawable.mong_body_ch324, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),
    CH325(R.drawable.mong_body_ch325, R.drawable.mong_body_ch325, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),

    CH330(R.drawable.mong_body_ch330, R.drawable.mong_body_ch330_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-15, 0),
    CH331(R.drawable.mong_body_ch331, R.drawable.mong_body_ch331_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-13, -23),
    CH332(R.drawable.mong_body_ch332, R.drawable.mong_body_ch332_gif, true, R.drawable.mong_face_sad, R.drawable.mong_face_depressed, R.drawable.mong_face_sulky, R.drawable.mong_face_happy, R.drawable.mong_face_eating, R.drawable.mong_face_sleeping, R.drawable.mong_face_smile,-8, 0),
    CH334(R.drawable.mong_body_ch334, R.drawable.mong_body_ch334, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),
    CH335(R.drawable.mong_body_ch335, R.drawable.mong_body_ch335, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),

    CH444(R.drawable.mong_none, R.drawable.mong_none, false, -1, -1, -1, -1, -1, -1, -1, 0, 0),
    ;

    companion object {
        fun getResource(code: String) = runCatching { MongResourceCode.valueOf(code) }.getOrDefault(CH444)
        fun getResourceCode(code: String) = getResource(code = code).gifCode
    }
}

