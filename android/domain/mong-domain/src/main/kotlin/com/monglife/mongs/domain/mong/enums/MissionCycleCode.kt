package com.monglife.mongs.domain.mong.enums

enum class MissionCycleCode(
    val description: String,
) {
    DAILY("일간 미션"),
    WEEKLY("주간 미션"),
    MONTHLY("월간 미션"),
    ;
}
