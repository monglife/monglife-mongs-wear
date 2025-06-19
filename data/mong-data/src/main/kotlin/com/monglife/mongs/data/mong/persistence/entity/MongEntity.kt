package com.monglife.mongs.data.mong.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import com.monglife.mongs.domain.mong.model.Mong
import java.time.LocalDateTime
import java.time.LocalTime

@Entity("mong")
class MongEntity(
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
    isCurrent: Boolean,
    graduateCheck: Boolean,
) {
    @PrimaryKey
    val mongId: Long = mongId

    val name: String = name

    val mongCode: String = mongCode

    val mongName: String = mongName

    val stateCode: MongStateCode = stateCode

    val statusCode: MongStatusCode = statusCode

    val level: Int = level

    val sleepAt: LocalTime = sleepAt

    val wakeupAt: LocalTime = wakeupAt

    val payPoint: Int = payPoint

    val isSleep: Boolean = isSleep

    val strengthRatio: Double = strengthRatio

    val healthyRatio: Double = healthyRatio

    val satietyRatio: Double = satietyRatio

    val fatigueRatio: Double = fatigueRatio

    val expRatio: Double = expRatio

    val weight: Double = weight

    val poopCount: Int = poopCount

    val randomDrawTicketCount: Int = randomDrawTicketCount

    val createdAt: LocalDateTime = createdAt

    val updatedAt: LocalDateTime = updatedAt

    val isCurrent: Boolean = isCurrent

    val graduateCheck: Boolean = graduateCheck

    /**
     * 엔티티 도메인 변환
     */
    fun toDomain(): Mong {
        return Mong(
            mongId = this.mongId,
            name = this.name,
            mongCode = this.mongCode,
            mongName = this.mongName,
            stateCode = this.stateCode,
            statusCode = this.statusCode,
            level = this.level,
            sleepAt = this.sleepAt,
            wakeupAt = this.wakeupAt,
            payPoint = this.payPoint,
            isSleep = this.isSleep,
            strengthRatio = this.strengthRatio,
            healthyRatio = this.healthyRatio,
            satietyRatio = this.satietyRatio,
            fatigueRatio = this.fatigueRatio,
            expRatio = this.expRatio,
            weight = this.weight,
            poopCount = this.poopCount,
            randomDrawTicketCount = this.randomDrawTicketCount,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            isCurrent = this.isCurrent,
            graduateCheck = this.graduateCheck,
        )
    }
}