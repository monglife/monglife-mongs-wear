package com.monglife.mongs.application.mong.port.web

import com.monglife.mongs.application.mong.exception.InvalidCreateMongException
import com.monglife.mongs.application.mong.exception.InvalidDeleteMongException
import com.monglife.mongs.application.mong.exception.InvalidEvolutionMongException
import com.monglife.mongs.application.mong.exception.InvalidGraduateMongException
import com.monglife.mongs.application.mong.exception.InvalidPoopCleanMongException
import com.monglife.mongs.application.mong.exception.InvalidSleepingMongException
import com.monglife.mongs.application.mong.exception.InvalidStrokeMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.web.response.CreateMongResponse
import com.monglife.mongs.application.mong.port.web.response.DeleteMongResponse
import com.monglife.mongs.application.mong.port.web.response.EvolutionMongResponse
import com.monglife.mongs.application.mong.port.web.response.GetMongResponse
import com.monglife.mongs.application.mong.port.web.response.GraduateMongResponse
import com.monglife.mongs.application.mong.port.web.response.PoopCleanMongResponse
import com.monglife.mongs.application.mong.port.web.response.SleepingMongResponse
import com.monglife.mongs.application.mong.port.web.response.StrokeMongResponse
import java.time.LocalTime

interface ManagementWebPort {

    /**
     * 몽 목록 조회
     */
    suspend fun getMongs(): List<GetMongResponse>

    /**
     * 몽 조회
     */
    @Throws(NotFoundMongException::class)
    suspend fun getMong(mongId: Long): GetMongResponse

    /**
     * 몽 생성
     */
    @Throws(InvalidCreateMongException::class)
    suspend fun createMong(name: String, sleepAt: LocalTime, wakeupAt: LocalTime): CreateMongResponse

    /**
     * 몽 삭제
     */
    @Throws(InvalidDeleteMongException::class)
    suspend fun deleteMong(mongId: Long): DeleteMongResponse

    /**
     * 몽 쓰다 듬기
     */
    @Throws(InvalidStrokeMongException::class)
    suspend fun strokeMong(mongId: Long): StrokeMongResponse

    /**
     * 몽 수면/기상
     */
    @Throws(InvalidSleepingMongException::class)
    suspend fun sleepingMong(mongId: Long): SleepingMongResponse

    /**
     * 몽 배변 처리
     */
    @Throws(InvalidPoopCleanMongException::class)
    suspend fun poopCleanMong(mongId: Long): PoopCleanMongResponse

    /**
     * 몽 진화
     */
    @Throws(InvalidEvolutionMongException::class)
    suspend fun evolutionMong(mongId: Long): EvolutionMongResponse

    /**
     * 몽 졸업
     */
    @Throws(InvalidGraduateMongException::class)
    suspend fun graduateMong(mongId: Long): GraduateMongResponse

}