package com.monglife.mongs.application.mong.port.persistence

import kotlinx.coroutines.flow.Flow

interface DevicePersistencePort {

    /**
     * 현재 몽 ID 조회
     */
    suspend fun getCurrentMongId(): Long?

    /**
     * 현재 몽 ID Flow 조회
     */
    suspend fun getCurrentMongIdFlow(): Flow<Long?>

    /**
     * 현재 몽 ID 수정
     */
    suspend fun setCurrentMongId(mongId: Long): Long

    /**
     * 현재 몽 ID 삭제
     */
    suspend fun deleteCurrentMongId()
}