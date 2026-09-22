package com.monglife.mongs.domain.mong.enums

enum class MissionRewardTypeCode(
    val description: String,
) {
    EXP("경험치"),
    PAY_POINT("페이 포인트"),
    STAR_POINT("스타 포인트"),
    INVENTORY("인벤토리 아이템"),
    ;
}
