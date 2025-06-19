package com.monglife.mongs.data.mong.web.client.request

/**
 * 훈련 완료 요청 Dto
 */
data class TrainingEndRequestDto(
    val trainingCode: String,
    val mongId: Long,
    val score: Int,
)