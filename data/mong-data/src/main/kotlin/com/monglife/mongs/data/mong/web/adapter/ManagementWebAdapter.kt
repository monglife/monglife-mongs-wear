package com.monglife.mongs.data.mong.web.adapter

import com.monglife.mongs.application.mong.exception.InvalidCreateMongException
import com.monglife.mongs.application.mong.exception.InvalidDeleteMongException
import com.monglife.mongs.application.mong.exception.InvalidEvolutionMongException
import com.monglife.mongs.application.mong.exception.InvalidGraduateMongException
import com.monglife.mongs.application.mong.exception.InvalidPoopCleanMongException
import com.monglife.mongs.application.mong.exception.InvalidSleepingMongException
import com.monglife.mongs.application.mong.exception.InvalidStrokeMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.port.web.response.CreateMongResponse
import com.monglife.mongs.application.mong.port.web.response.DeleteMongResponse
import com.monglife.mongs.application.mong.port.web.response.EvolutionMongResponse
import com.monglife.mongs.application.mong.port.web.response.GetMongResponse
import com.monglife.mongs.application.mong.port.web.response.GraduateMongResponse
import com.monglife.mongs.application.mong.port.web.response.PoopCleanMongResponse
import com.monglife.mongs.application.mong.port.web.response.SleepingMongResponse
import com.monglife.mongs.application.mong.port.web.response.StrokeMongResponse
import com.monglife.mongs.data.mong.web.client.ManagementWebClient
import com.monglife.mongs.data.mong.web.client.request.CreateMongRequestDto
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagementWebAdapter @Inject constructor(
    private val managementWebClient: ManagementWebClient,
) : ManagementWebPort {

    /**
     * 몽 목록 조회
     */
    override suspend fun getMongs(): List<GetMongResponse> =
        managementWebClient.getMongs().let { response ->

            val body = response.takeIf { it.isSuccessful }?.body()

            body?.result?.map {
                GetMongResponse(
                    mongId = it.mongId,
                    name = it.name,
                    mongCode = it.mongCode,
                    mongName = it.mongName,
                    stateCode = it.stateCode,
                    statusCode = it.statusCode,
                    level = it.level,
                    sleepAt = it.sleepAt,
                    wakeupAt = it.wakeupAt,
                    payPoint = it.payPoint,
                    isSleep = it.isSleep,
                    strengthRatio = it.strengthRatio,
                    healthyRatio = it.healthyRatio,
                    satietyRatio = it.satietyRatio,
                    fatigueRatio = it.fatigueRatio,
                    expRatio = it.expRatio,
                    weight = it.weight,
                    poopCount = it.poopCount,
                    randomDrawTicketCount = it.randomDrawTicketCount,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            } ?: emptyList()
        }

    /**
     * 몽 조회
     */
    @Throws(NotFoundMongException::class)
    override suspend fun getMong(mongId: Long): GetMongResponse =
        managementWebClient.getMong(mongId = mongId).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body() ?: throw NotFoundMongException()

            GetMongResponse(
                mongId = body.result.mongId,
                name = body.result.name,
                mongCode = body.result.mongCode,
                mongName = body.result.mongName,
                stateCode = body.result.stateCode,
                statusCode = body.result.statusCode,
                level = body.result.level,
                sleepAt = body.result.sleepAt,
                wakeupAt = body.result.wakeupAt,
                payPoint = body.result.payPoint,
                isSleep = body.result.isSleep,
                strengthRatio = body.result.strengthRatio,
                healthyRatio = body.result.healthyRatio,
                satietyRatio = body.result.satietyRatio,
                fatigueRatio = body.result.fatigueRatio,
                expRatio = body.result.expRatio,
                weight = body.result.weight,
                poopCount = body.result.poopCount,
                randomDrawTicketCount = body.result.randomDrawTicketCount,
                createdAt = body.result.createdAt,
                updatedAt = body.result.updatedAt,
            )
        }

    /**
     * 몽 생성
     */
    @Throws(InvalidCreateMongException::class)
    override suspend fun createMong(
        name: String,
        sleepAt: LocalTime,
        wakeupAt: LocalTime
    ): CreateMongResponse = managementWebClient.createMong(
        createMongRequestDto = CreateMongRequestDto(
            name = name,
            sleepAt = sleepAt,
            wakeupAt = wakeupAt,
        )
    ).let { response ->

        val body = response.takeIf { it.isSuccessful }?.body() ?: throw InvalidCreateMongException()

        CreateMongResponse(
            mongId = body.result.mongId,
            name = body.result.name,
            mongCode = body.result.mongCode,
            mongName = body.result.mongName,
            stateCode = body.result.stateCode,
            statusCode = body.result.statusCode,
            level = body.result.level,
            sleepAt = body.result.sleepAt,
            wakeupAt = body.result.wakeupAt,
            payPoint = body.result.payPoint,
            isSleep = body.result.isSleep,
            strengthRatio = body.result.strengthRatio,
            healthyRatio = body.result.healthyRatio,
            satietyRatio = body.result.satietyRatio,
            fatigueRatio = body.result.fatigueRatio,
            expRatio = body.result.expRatio,
            weight = body.result.weight,
            poopCount = body.result.poopCount,
            randomDrawTicketCount = body.result.randomDrawTicketCount,
            createdAt = body.result.createdAt,
            updatedAt = body.result.updatedAt,
        )
    }

    /**
     * 몽 삭제
     */
    @Throws(InvalidDeleteMongException::class)
    override suspend fun deleteMong(mongId: Long): DeleteMongResponse =
        managementWebClient.deleteMong(mongId = mongId).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body() ?: throw InvalidDeleteMongException()

            DeleteMongResponse(
                mongId = body.result.mongId,
            )
        }

    /**
     * 몽 쓰다 듬기
     */
    @Throws(InvalidStrokeMongException::class)
    override suspend fun strokeMong(mongId: Long): StrokeMongResponse =
        managementWebClient.strokeMong(mongId = mongId).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body() ?: throw InvalidStrokeMongException()

            StrokeMongResponse(
                mongId = body.result.mongId,
                expRatio = body.result.expRatio,
                createdAt = body.result.createdAt,
                updatedAt = body.result.updatedAt,
            )
        }

    /**
     * 몽 수면/기상
     */
    @Throws(InvalidSleepingMongException::class)
    override suspend fun sleepingMong(mongId: Long): SleepingMongResponse =
        managementWebClient.sleepMong(mongId = mongId).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body() ?: throw InvalidSleepingMongException()

            SleepingMongResponse(
                mongId = body.result.mongId,
                isSleep = body.result.isSleep,
                createdAt = body.result.createdAt,
                updatedAt = body.result.updatedAt,
            )
        }

    /**
     * 몽 배변 처리
     */
    @Throws(InvalidPoopCleanMongException::class)
    override suspend fun poopCleanMong(mongId: Long): PoopCleanMongResponse =
        managementWebClient.poopCleanMong(mongId = mongId).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body() ?: throw InvalidPoopCleanMongException()

            PoopCleanMongResponse(
                mongId = body.result.mongId,
                expRatio = body.result.expRatio,
                poopCount = body.result.poopCount,
                createdAt = body.result.createdAt,
                updatedAt = body.result.updatedAt,
            )
        }

    /**
     * 몽 진화
     */
    @Throws(InvalidEvolutionMongException::class)
    override suspend fun evolutionMong(mongId: Long): EvolutionMongResponse =
        managementWebClient.evolutionMong(mongId = mongId).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body() ?: throw InvalidEvolutionMongException()

            EvolutionMongResponse(
                mongId = body.result.mongId,
                mongCode = body.result.mongCode,
                expRatio = body.result.expRatio,
                strengthRatio = body.result.strengthRatio,
                healthyRatio = body.result.healthyRatio,
                satietyRatio = body.result.satietyRatio,
                fatigueRatio = body.result.fatigueRatio,
                createdAt = body.result.createdAt,
                updatedAt = body.result.updatedAt,
            )
        }

    /**
     * 몽 졸업
     */
    @Throws(InvalidGraduateMongException::class)
    override suspend fun graduateMong(mongId: Long): GraduateMongResponse =
        managementWebClient.graduateMong(mongId = mongId).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body() ?: throw InvalidGraduateMongException()

            GraduateMongResponse(
                mongId = body.result.mongId,
            )
        }
}