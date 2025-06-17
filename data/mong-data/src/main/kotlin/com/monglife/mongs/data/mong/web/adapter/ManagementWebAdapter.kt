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
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagementWebAdapter @Inject constructor(
    
) : ManagementWebPort {

    /**
     * 몽 목록 조회
     */
    override suspend fun getMongs(): List<GetMongResponse> {
        TODO("Not yet implemented")
    }

    /**
     * 몽 조회
     */
    @Throws(NotFoundMongException::class)
    override suspend fun getMong(mongId: Long): GetMongResponse {
        TODO("Not yet implemented")
    }

    /**
     * 몽 생성
     */
    @Throws(InvalidCreateMongException::class)
    override suspend fun createMong(
        name: String,
        sleepAt: LocalTime,
        wakeupAt: LocalTime
    ): CreateMongResponse {
        TODO("Not yet implemented")
    }

    /**
     * 몽 삭제
     */
    @Throws(InvalidDeleteMongException::class)
    override suspend fun deleteMong(mongId: Long): DeleteMongResponse {
        TODO("Not yet implemented")
    }

    /**
     * 몽 쓰다 듬기
     */
    @Throws(InvalidStrokeMongException::class)
    override suspend fun strokeMong(mongId: Long): StrokeMongResponse {
        TODO("Not yet implemented")
    }

    /**
     * 몽 수면/기상
     */
    @Throws(InvalidSleepingMongException::class)
    override suspend fun sleepingMong(mongId: Long): SleepingMongResponse {
        TODO("Not yet implemented")
    }

    /**
     * 몽 배변 처리
     */
    @Throws(InvalidPoopCleanMongException::class)
    override suspend fun poopCleanMong(mongId: Long): PoopCleanMongResponse {
        TODO("Not yet implemented")
    }

    /**
     * 몽 진화
     */
    @Throws(InvalidEvolutionMongException::class)
    override suspend fun evolutionMong(mongId: Long): EvolutionMongResponse {
        TODO("Not yet implemented")
    }

    /**
     * 몽 졸업
     */
    @Throws(InvalidGraduateMongException::class)
    override suspend fun graduateMong(mongId: Long): GraduateMongResponse {
        TODO("Not yet implemented")
    }
}