package com.monglife.mongs.domain.mong.enums

enum class MissionStateCode(
    val description: String,
) {
    IN_PROGRESS("진행 중"),
    CLAIMABLE("수령 가능"),
    CLAIMED("수령 완료"),
    ;
}
