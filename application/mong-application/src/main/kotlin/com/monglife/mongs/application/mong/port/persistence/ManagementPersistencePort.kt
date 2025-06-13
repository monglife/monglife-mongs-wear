package com.monglife.mongs.application.mong.port.persistence

import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.domain.mong.model.Mong
import kotlinx.coroutines.flow.Flow

interface ManagementPersistencePort {

    /**
     * 현재 몽 조회
     */
    suspend fun getCurrentMongId(): Long?

    /**
     * 몽 조회
     */
    @Throws(NotFoundMongException::class)
    suspend fun getMong(mongId: Long): Mong

    /**
     * 몽 라이브 객체 조회
     */
    @Throws(NotFoundMongException::class)
    suspend fun getMongFlow(mongId: Long): Flow<Mong>

    /**
     * 현재 몽 라이브 객체 목록 조회
     */
    suspend fun getMongsFlow(): List<Flow<Mong>>

    /**
     * 몽 영속화
     */
    suspend fun saveMong(mong: Mong): Mong

    /**
     * 몽 삭제
     */
    suspend fun deleteMong(mongId: Long): Mong
}