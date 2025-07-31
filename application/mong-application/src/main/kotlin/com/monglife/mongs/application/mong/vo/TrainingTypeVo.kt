package com.monglife.mongs.application.mong.vo

import com.monglife.mongs.domain.mong.model.TrainingType

data class TrainingTypeVo(
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
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(trainingType: TrainingType): TrainingTypeVo {
            return TrainingTypeVo(
                trainingCode = trainingType.trainingCode,
                trainingName = trainingType.trainingName,
                payPoint = trainingType.payPoint,
                score = trainingType.score,
                timeout = trainingType.timeout,
                exp = trainingType.exp,
                weight = trainingType.weight,
                strength = trainingType.strength,
                satiety = trainingType.satiety,
                fatigue = trainingType.fatigue,
            )
        }
    }
}
