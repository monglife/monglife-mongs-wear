package com.monglife.mongs.data.mong.persistence.adapter

import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.domain.mong.model.Mong
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagementPersistenceAdapter @Inject constructor(

) : ManagementPersistencePort {

    /**
     * 현재 몽 조회
     */
    override suspend fun getCurrentMongId(): Long? {
        TODO("Not yet implemented")
    }

    /**
     * 몽 조회
     */
    @Throws(NotFoundMongException::class)
    override suspend fun getMong(mongId: Long): Mong {
        TODO("Not yet implemented")
    }

    /**
     * 몽 라이브 객체 조회
     */
    @Throws(NotFoundMongException::class)
    override suspend fun getMongFlow(mongId: Long): Flow<Mong> {
        TODO("Not yet implemented")
    }

    /**
     * 현재 몽 라이브 객체 목록 조회
     */
    override suspend fun getMongsFlow(): List<Flow<Mong>> {
        TODO("Not yet implemented")
    }

    /**
     * 몽 영속화
     */
    override suspend fun saveMong(mong: Mong): Mong {
        TODO("Not yet implemented")
    }

    /**
     * 몽 삭제
     */
    override suspend fun deleteMong(mongId: Long): Mong {
        TODO("Not yet implemented")
    }
}