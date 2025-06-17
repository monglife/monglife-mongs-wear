package com.monglife.mongs.application.mong.port.web.response

import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import com.monglife.mongs.domain.mong.model.TrainingType

/**
 * 훈련 조회 응답
 */
data class GetTrainingResponse(
    val trainingTypeId: Long,
    val trainingCode: String,
    val trainingName: String,
    val payPoint: Int,
    val score: Int,
    val timeout: Int,
    val exp: Double,
    val weight: Double,
    val strength: Double,
    val satiety: Double,
    val fatigue: Double,
) {
    fun toDomain(): TrainingType {
        return TrainingType(
            trainingTypeId = this.trainingTypeId,
            trainingCode = this.trainingCode,
            trainingName = this.trainingName,
            payPoint = this.payPoint,
            score = this.score,
            timeout = this.timeout,
            exp = this.exp,
            weight = this.weight,
            strength = this.strength,
            satiety = this.satiety,
            fatigue = this.fatigue,
        )
    }
}

/**
 * 훈련 완료 응답
 */
data class TrainingResponse(
    val isSuccess: Boolean,
    val rewardPayPoint: Int,
    val score: Int,
    val mongId: Long,
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

