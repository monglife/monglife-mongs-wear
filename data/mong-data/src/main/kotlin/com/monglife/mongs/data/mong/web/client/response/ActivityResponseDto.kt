package com.monglife.mongs.data.mong.web.client.response

import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode

/**
 * 훈련 조회 응답 Dto
 */
data class GetTrainingResponseDto(
    val trainingTypeId: Long,
    val trainingCode: String,
    val trainingName: String,
    val payPoint: Int,
    val score: Int,
    val timeout: Int,
    val exp: Double,
    val strength: Double,
    val weight: Double,
    val satiety: Double,
    val fatigue: Double,
)

/**
 * 훈련 완료 응답 Dto
 */
data class TrainingEndResponseDto(
    val mongId: Long,
    val isSuccess: Boolean,
    val rewardPayPoint: Int,
    val score: Int,
    val payPoint: Int,
    val expRatio: Double,
    val strengthRatio: Double,
    val healthyRatio: Double,
    val satietyRatio: Double,
    val fatigueRatio: Double,
    val weight: Double,
    val stateCode: MongStateCode,
    val statusCode: MongStatusCode,
)