package com.monglife.mongs.data.mong.persistence.dto

import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import java.time.LocalDateTime

data class ManagementEventDto(
    val mongId: Long,
    val name: String,
    val mongCode: String,
    val mongName: String,
    val payPoint: Int,
    val stateCode: MongStateCode,
    val statusCode: MongStatusCode,
    val isSleep: Boolean,
    val weight: Double,
    val expRatio: Double,
    val strengthRatio: Double,
    val satietyRatio: Double,
    val healthyRatio: Double,
    val fatigueRatio: Double,
    val poopCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)