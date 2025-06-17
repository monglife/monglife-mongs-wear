package com.monglife.mongs.application.mong.vo

import com.monglife.mongs.domain.mong.enums.MongStateCode
import com.monglife.mongs.domain.mong.enums.MongStatusCode
import com.monglife.mongs.domain.mong.model.Mong
import java.time.LocalDateTime
import java.time.LocalTime

data class MongVo(
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
    val isCurrent: Boolean,
    val graduateCheck: Boolean,
) {
    companion object {

        /**
         * 도메인 Vo 변환
         */
        fun of(mong: Mong): MongVo {
            return MongVo(
                mongId = mong.mongId,
                name = mong.name,
                mongCode = mong.mongCode,
                mongName = mong.mongName,
                stateCode = mong.stateCode,
                statusCode = mong.statusCode,
                level = mong.level,
                sleepAt = mong.sleepAt,
                wakeupAt = mong.wakeupAt,
                payPoint = mong.payPoint,
                isSleep = mong.isSleep,
                strengthRatio = mong.strengthRatio,
                healthyRatio = mong.healthyRatio,
                satietyRatio = mong.satietyRatio,
                fatigueRatio = mong.fatigueRatio,
                expRatio = mong.expRatio,
                weight = mong.weight,
                poopCount = mong.poopCount,
                randomDrawTicketCount = mong.randomDrawTicketCount,
                createdAt = mong.createdAt,
                updatedAt = mong.updatedAt,
                isCurrent = mong.isCurrent,
                graduateCheck = mong.graduateCheck,
            )
        }
    }
}