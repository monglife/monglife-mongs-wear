package com.monglife.mongs.domain.mong.model

class TrainingType(
    trainingTypeId: Long,
    trainingCode: String,
    trainingName: String,
    payPoint: Int,
    score: Int,
    timeout: Int,
    exp: Double,
    weight: Double,
    strength: Double,
    satiety: Double,
    fatigue: Double,
) {
    var trainingTypeId: Long = trainingTypeId
        private set
    var trainingCode: String = trainingCode
        private set
    var trainingName: String = trainingName
        private set
    var payPoint: Int = payPoint
        private set
    var score: Int = score
        private set
    var timeout: Int = timeout
        private set
    var exp: Double = exp
        private set
    var weight: Double = weight
        private set
    var strength: Double = strength
        private set
    var satiety: Double = satiety
        private set
    var fatigue: Double = fatigue
        private set
}