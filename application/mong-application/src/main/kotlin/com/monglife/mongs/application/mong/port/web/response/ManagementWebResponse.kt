package com.monglife.mongs.application.mong.port.web.response

import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import com.monglife.mongs.domain.mong.model.Mong
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 몽 조회 응답 Vo
 */
data class GetMongResponseVo(
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
    fun toDomain(): Mong {
        return Mong(
            mongId = mongId,
            name = name,
            mongCode = mongCode,
            mongName = mongName,
            stateCode = stateCode,
            statusCode = statusCode,
            level = level,
            sleepAt = sleepAt,
            wakeupAt = wakeupAt,
            payPoint = payPoint,
            isSleep = isSleep,
            strengthRatio = strengthRatio,
            healthyRatio = healthyRatio,
            satietyRatio = satietyRatio,
            fatigueRatio = fatigueRatio,
            expRatio = expRatio,
            weight = weight,
            poopCount = poopCount,
            randomDrawTicketCount = randomDrawTicketCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}

/**
 * 몽 생성 응답 Vo
 */
data class CreateMongResponseVo(
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
    fun toDomain(): Mong {
        return Mong(
            mongId = mongId,
            name = name,
            mongCode = mongCode,
            mongName = mongName,
            stateCode = stateCode,
            statusCode = statusCode,
            level = level,
            sleepAt = sleepAt,
            wakeupAt = wakeupAt,
            payPoint = payPoint,
            isSleep = isSleep,
            strengthRatio = strengthRatio,
            healthyRatio = healthyRatio,
            satietyRatio = satietyRatio,
            fatigueRatio = fatigueRatio,
            expRatio = expRatio,
            weight = weight,
            poopCount = poopCount,
            randomDrawTicketCount = randomDrawTicketCount,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}

/**
 * 몽 삭제 응답 Vo
 */
data class DeleteMongResponseVo(
    val mongId: Long,
)

/**
 * 몽 쓰다 듬기 응답 Vo
 */
data class StrokeMongResponseVo(
    val mongId: Long,
    val expRatio: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * 몽 수면/기상 응답 Vo
 */
data class SleepingMongResponseVo(
    val mongId: Long,
    val isSleep: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * 몽 배변 처리 응답 Vo
 */
data class PoopCleanMongResponseVo(
    val mongId: Long,
    val expRatio: Double,
    val poopCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * 몽 진화 응답 Vo
 */
data class EvolutionMongResponseVo(
    val mongId: Long,
    val mongCode: String,
    val expRatio: Double,
    val strengthRatio: Double,
    val healthyRatio: Double,
    val satietyRatio: Double,
    val fatigueRatio: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * 몽 졸업 응답 Vo
 */
data class GraduateMongResponseVo(
    val mongId: Long,
)