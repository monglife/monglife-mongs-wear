package com.monglife.mongs.data.mong.persistence.adapter

import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.data.mong.persistence.db.MongRoomDB
import com.monglife.mongs.data.mong.persistence.entity.MongEntity
import com.monglife.mongs.domain.mong.model.Mong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagementPersistenceAdapter @Inject constructor(
    private val roomDB: MongRoomDB,
) : ManagementPersistencePort {

    /**
     * 현재 몽 조회
     */
    override suspend fun getCurrentMongId(): Long? =
        roomDB.mongDao().findMongByIsCurrentTrue()?.mongId

    /**
     * 몽 조회
     */
    @Throws(NotFoundMongException::class)
    override suspend fun getMong(mongId: Long): Mong =
        roomDB.mongDao().findMongByMongId(mongId = mongId)?.toDomain()
            ?: throw NotFoundMongException()

    /**
     * 몽 라이브 객체 조회
     */
    @Throws(NotFoundMongException::class)
    override suspend fun getMongFlow(mongId: Long): Flow<Mong> =
        roomDB.mongDao().findMongFlowByMongId(mongId = mongId).map {
            it?.toDomain() ?: throw NotFoundMongException()
        }

    /**
     * 현재 몽 라이브 객체 목록 조회
     */
    override suspend fun getMongsFlow(): Flow<List<Mong>> =
        roomDB.mongDao().findAllMongFlow().map {
            it.map { mongEntity -> mongEntity.toDomain() }
        }

    /**
     * 몽 영속화
     */
    override suspend fun saveMong(mong: Mong): Mong =
        roomDB.mongDao().save(
            mongEntity = MongEntity(
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
        ).toDomain()

    /**
     * 몽 삭제
     */
    override suspend fun deleteMong(mongId: Long) {
        roomDB.mongDao().deleteByMongId(mongId = mongId)
    }
}