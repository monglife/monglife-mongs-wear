package com.monglife.mongs.application.mong.port.persistence

import com.monglife.mongs.application.mong.exception.NotFoundCurrentMongIdException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongOptionException
import com.monglife.mongs.domain.mong.model.Mong
import com.monglife.mongs.domain.mong.model.MongOption
import kotlinx.coroutines.flow.Flow

interface ManagementPersistencePort {

    /**
     * 현재 몽 ID 조회
     */
    @Throws(NotFoundCurrentMongIdException::class)
    suspend fun getCurrentMongId(): Long

    /**
     * 현재 몽 ID Flow 조회
     */
    suspend fun getCurrentMongIdFlow(): Flow<Long?>

    /**
     * 현재 몽 ID 수정
     */
    suspend fun setCurrentMongId(mongId: Long)

    /**
     * 현재 몽 ID 삭제
     */
    suspend fun deleteCurrentMongId()

    /**
     * 몽 옵션 조회
     */
    @Throws(NotFoundMongOptionException::class)
    suspend fun getMongOption(mongId: Long): MongOption

    /**
     * 몽 옵션 Flow 객체 조회
     */
    suspend fun getMongOptionFlow(mongId: Long): Flow<MongOption?>

    /**
     * 몽 옵션 영속화
     */
    suspend fun saveMongOption(mongOption: MongOption): MongOption

    /**
     * 몽 옵션 삭제
     */
    suspend fun deleteMongOption(mongId: Long)

    /**
     * 몽 조회
     */
    @Throws(NotFoundMongException::class)
    suspend fun getMong(mongId: Long): Mong

    /**
     * 몽 라이브 객체 조회
     */
    suspend fun getMongFlow(mongId: Long): Flow<Mong?>

    /**
     * 몽 영속화
     */
    suspend fun saveMong(mong: Mong): Mong

    /**
     * 몽 삭제
     */
    suspend fun deleteMong(mongId: Long)
}