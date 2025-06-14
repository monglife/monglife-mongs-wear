package com.monglife.mongs.application.mong.port.web

import com.monglife.mongs.application.mong.exception.InvalidCreateMongException
import com.monglife.mongs.application.mong.exception.InvalidDeleteMongException
import com.monglife.mongs.application.mong.exception.InvalidEvolutionMongException
import com.monglife.mongs.application.mong.exception.InvalidGraduateMongException
import com.monglife.mongs.application.mong.exception.InvalidPoopCleanMongException
import com.monglife.mongs.application.mong.exception.InvalidSleepingMongException
import com.monglife.mongs.application.mong.exception.InvalidStrokeMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.web.response.CreateMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.DeleteMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.EvolutionMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.GetMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.GraduateMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.PoopCleanMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.SleepingMongResponseVo
import com.monglife.mongs.application.mong.port.web.response.StrokeMongResponseVo
import java.time.LocalTime

interface ManagementWebPort {

    /**
     * 몽 목록 조회
     */
    suspend fun getMongs(): List<GetMongResponseVo>

    /**
     * 몽 조회
     */
    @Throws(NotFoundMongException::class)
    suspend fun getMong(mongId: Long): GetMongResponseVo

    /**
     * 몽 생성
     */
    @Throws(InvalidCreateMongException::class)
    suspend fun createMong(name: String, sleepAt: LocalTime, wakeupAt: LocalTime): CreateMongResponseVo

    /**
     * 몽 삭제
     */
    @Throws(InvalidDeleteMongException::class)
    suspend fun deleteMong(mongId: Long): DeleteMongResponseVo

    /**
     * 몽 쓰다 듬기
     */
    @Throws(InvalidStrokeMongException::class)
    suspend fun strokeMong(mongId: Long): StrokeMongResponseVo

    /**
     * 몽 수면/기상
     */
    @Throws(InvalidSleepingMongException::class)
    suspend fun sleepingMong(mongId: Long): SleepingMongResponseVo

    /**
     * 몽 배변 처리
     */
    @Throws(InvalidPoopCleanMongException::class)
    suspend fun poopCleanMong(mongId: Long): PoopCleanMongResponseVo

    /**
     * 몽 진화
     */
    @Throws(InvalidEvolutionMongException::class)
    suspend fun evolutionMong(mongId: Long): EvolutionMongResponseVo

    /**
     * 몽 졸업
     */
    @Throws(InvalidGraduateMongException::class)
    suspend fun graduateMong(mongId: Long): GraduateMongResponseVo

}