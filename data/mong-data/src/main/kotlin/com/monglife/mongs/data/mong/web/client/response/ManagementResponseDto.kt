package com.monglife.mongs.data.mong.web.client.response

import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * 몽 조회 응답 Dto
 */
data class GetMongResponseDto(
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
)

/**
 * 몽 생성 응답 Dto
 */
data class CreateMongResponseDto(
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
)

/**
 * 몽 삭제 응답 Dto
 */
data class DeleteMongResponseDto(
    val mongId: Long,
)

/**
 * 몽 쓰다 듬기 응답 Dto
 */
data class StrokeMongResponseDto(
    val mongId: Long,
    val expRatio: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * 몽 수면/기상 응답 Dto
 */
data class SleepWakeupResponseDto(
    val mongId: Long,
    val isSleep: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * 몽 배변 처리 응답 Dto
 */
data class PoopCleanMongResponseDto(
    val mongId: Long,
    val expRatio: Double,
    val poopCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

/**
 * 몽 진화 응답 Dto
 */
data class EvolutionMongResponseDto(
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
 * 몽 졸업 응답 Dto
 */
data class GraduateMongResponseDto(
    val mongId: Long,
)