package com.monglife.mongs.domain.mong.model

import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import java.time.LocalDateTime
import java.time.LocalTime

class Mong(
    mongId: Long,
    name: String,
    mongCode: String,
    mongName: String,
    stateCode: MongStateCode,
    statusCode: MongStatusCode,
    level: Int,
    sleepAt: LocalTime,
    wakeupAt: LocalTime,
    payPoint: Int,
    isSleep: Boolean,
    strengthRatio: Double,
    healthyRatio: Double,
    satietyRatio: Double,
    fatigueRatio: Double,
    expRatio: Double,
    weight: Double,
    poopCount: Int,
    randomDrawTicketCount: Int,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
) {
    var mongId: Long = mongId
        private set
    var name: String = name
        private set
    var mongCode: String = mongCode
        private set
    var mongName: String = mongName
        private set
    var stateCode: MongStateCode = stateCode
        private set
    var statusCode: MongStatusCode = statusCode
        private set
    var level: Int = level
        private set
    var sleepAt: LocalTime = sleepAt
        private set
    var wakeupAt: LocalTime = wakeupAt
        private set
    var payPoint: Int = payPoint
        private set
    var isSleep: Boolean = isSleep
        private set
    var strengthRatio: Double = strengthRatio
        private set
    var healthyRatio: Double = healthyRatio
        private set
    var satietyRatio: Double = satietyRatio
        private set
    var fatigueRatio: Double = fatigueRatio
        private set
    var expRatio: Double = expRatio
        private set
    var weight: Double = weight
        private set
    var poopCount: Int = poopCount
        private set
    var randomDrawTicketCount: Int = randomDrawTicketCount
        private set
    var createdAt: LocalDateTime = createdAt
        private set
    var updatedAt: LocalDateTime = updatedAt
        private set

    /**
     * 진화
     */
    fun evolution(
        mongCode: String,
        expRatio: Double,
        strengthRatio: Double,
        healthyRatio: Double,
        satietyRatio: Double,
        fatigueRatio: Double,
        stateCode: MongStateCode,
        statusCode: MongStatusCode,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ) {
        this.mongCode = mongCode
        this.expRatio = expRatio
        this.strengthRatio = strengthRatio
        this.healthyRatio = healthyRatio
        this.satietyRatio = satietyRatio
        this.fatigueRatio = fatigueRatio
        this.stateCode = stateCode
        this.statusCode = statusCode
        this.createdAt = createdAt
        this.updatedAt = updatedAt
    }

    /**
     * 몽 섭취
     */
    fun feed(
        payPoint: Int,
        expRatio: Double,
        strengthRatio: Double,
        healthyRatio: Double,
        satietyRatio: Double,
        fatigueRatio: Double,
        weight: Double,
        stateCode: MongStateCode,
        statusCode: MongStatusCode,
    ) {
        this.payPoint = payPoint
        this.expRatio = expRatio
        this.strengthRatio = strengthRatio
        this.healthyRatio = healthyRatio
        this.satietyRatio = satietyRatio
        this.fatigueRatio = fatigueRatio
        this.weight = weight
        this.stateCode = stateCode
        this.statusCode = statusCode
    }

    /**
     * 배변 처리
     */
    fun poopClean(
        mongId: Long,
        expRatio: Double,
        poopCount: Int,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ) {
        this.mongId = mongId
        this.expRatio = expRatio
        this.poopCount = poopCount
        this.createdAt = createdAt
        this.updatedAt = updatedAt
    }

    /**
     * 수면, 기상
     */
    fun sleeping(
        mongId: Long,
        isSleep: Boolean,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ) {
        this.mongId = mongId
        this.isSleep = isSleep
        this.createdAt = createdAt
        this.updatedAt = updatedAt
    }

    /**
     * 쓰다듬기
     */
    fun stroke(
        mongId: Long,
        expRatio: Double,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ) {
        this.mongId = mongId
        this.expRatio = expRatio
        this.createdAt = createdAt
        this.updatedAt = updatedAt
    }

    /**
     * 훈련
     */
    fun training(
        mongId: Long,
        payPoint: Int,
        expRatio: Double,
        strengthRatio: Double,
        healthyRatio: Double,
        satietyRatio: Double,
        fatigueRatio: Double,
        weight: Double,
        stateCode: MongStateCode,
        statusCode: MongStatusCode,
    ) {
        this.mongId = mongId
        this.payPoint = payPoint
        this.expRatio = expRatio
        this.strengthRatio = strengthRatio
        this.healthyRatio = healthyRatio
        this.satietyRatio = satietyRatio
        this.fatigueRatio = fatigueRatio
        this.weight = weight
        this.stateCode = stateCode
        this.statusCode = statusCode
    }

    /**
     * 인벤토리 소비
     */
    fun consumeInventory(
        payPoint: Int,
        expRatio: Double,
        strengthRatio: Double,
        healthyRatio: Double,
        satietyRatio: Double,
        fatigueRatio: Double,
        weight: Double,
        stateCode: MongStateCode,
        statusCode: MongStatusCode,
    ) {
        this.payPoint = payPoint
        this.expRatio = expRatio
        this.strengthRatio = strengthRatio
        this.healthyRatio = healthyRatio
        this.satietyRatio = satietyRatio
        this.fatigueRatio = fatigueRatio
        this.weight = weight
        this.stateCode = stateCode
        this.statusCode = statusCode
    }

    /**
     * 랜덤 뽑기 티켓 구매
     */
    fun buyRandomDrawTicket(
        payPoint: Int,
        randomDrawTicketCount: Int,
    ) {
        this.payPoint = payPoint
        this.randomDrawTicketCount = randomDrawTicketCount
    }

    /**
     * 업데이트
     */
    fun update(
        name: String,
        mongCode: String,
        mongName: String,
        stateCode: MongStateCode,
        statusCode: MongStatusCode,
        level: Int,
        sleepAt: LocalTime,
        wakeupAt: LocalTime,
        payPoint: Int,
        isSleep: Boolean,
        strengthRatio: Double,
        healthyRatio: Double,
        satietyRatio: Double,
        fatigueRatio: Double,
        expRatio: Double,
        weight: Double,
        poopCount: Int,
        randomDrawTicketCount: Int,
        createdAt: LocalDateTime,
        updatedAt: LocalDateTime,
    ) {
        this.name = name
        this.mongCode = mongCode
        this.mongName = mongName
        this.stateCode = stateCode
        this.statusCode = statusCode
        this.level = level
        this.sleepAt = sleepAt
        this.wakeupAt = wakeupAt
        this.payPoint = payPoint
        this.isSleep = isSleep
        this.strengthRatio = strengthRatio
        this.healthyRatio = healthyRatio
        this.satietyRatio = satietyRatio
        this.fatigueRatio = fatigueRatio
        this.expRatio = expRatio
        this.weight = weight
        this.poopCount = poopCount
        this.randomDrawTicketCount = randomDrawTicketCount
        this.createdAt = createdAt
        this.updatedAt = updatedAt
    }
}