package com.monglife.mongs.data.mong.persistence.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import com.monglife.mongs.domain.mong.model.Mong
import java.time.LocalDateTime
import java.time.LocalTime

@Entity("mong")
data class MongEntity(
    @PrimaryKey
    val mongId: Long,
    val name: String,
    val mongCode: String,
    val mongName: String,
    val stateCode: MongStateCode,
    val statusCode: MongStatusCode,
    val level: Int,
    val sleepAt: LocalTime,
    val wakeupAt: LocalTime,
    val payPoint: Int,
    val isSleep: Boolean,
    val strengthRatio: Double,
    val healthyRatio: Double,
    val satietyRatio: Double,
    val fatigueRatio: Double,
    val expRatio: Double,
    val weight: Double,
    val poopCount: Int,
    val randomDrawTicketCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    /**
     * 엔티티 도메인 변환
     */
    fun toDomain(): Mong = Mong(
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
    )
}