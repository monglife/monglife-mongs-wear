package com.monglife.mongs.data.mong.persistence.adapter

import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.domain.mong.model.Mong
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManagementPersistenceAdapter @Inject constructor(

) : ManagementPersistencePort {

    override suspend fun getCurrentMongId(): Long? {
        TODO("Not yet implemented")
    }

    override suspend fun getMong(mongId: Long): Mong {
        TODO("Not yet implemented")
    }

    override suspend fun getMongFlow(mongId: Long): Flow<Mong> {
        TODO("Not yet implemented")
    }

    override suspend fun getMongsFlow(): List<Flow<Mong>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveMong(mong: Mong): Mong {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMong(mongId: Long): Mong {
        TODO("Not yet implemented")
    }
}