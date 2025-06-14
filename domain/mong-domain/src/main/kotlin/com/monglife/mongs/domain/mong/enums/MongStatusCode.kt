package com.monglife.mongs.domain.mong.enums

enum class MongStatusCode(
    val message: String,
) {
    NORMAL("정상 컨디션"),
    SOMNOLENCE("졸림"),
    HUNGRY("배고픔"),
    SICK("아픔"),
    ;
}