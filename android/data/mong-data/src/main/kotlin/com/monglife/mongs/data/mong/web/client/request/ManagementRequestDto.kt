package com.monglife.mongs.data.mong.web.client.request

import java.time.LocalTime

/**
 * 몽 생성 요청 Dto
 */
data class CreateMongRequestDto(
    val name: String,
    val sleepAt: LocalTime,
    val wakeupAt: LocalTime,
)