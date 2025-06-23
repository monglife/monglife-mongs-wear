package com.monglife.mongs.data.mong.persistence.adapter

import androidx.datastore.preferences.core.edit
import com.monglife.mongs.application.mong.exception.NotFoundCurrentMongIdException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongOptionException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.data.mong.persistence.datastore.MongDataStore
import com.monglife.mongs.data.mong.persistence.db.MongRoomDB
import com.monglife.mongs.data.mong.persistence.entity.MongEntity
import com.monglife.mongs.data.mong.persistence.entity.MongOptionEntity
import com.monglife.mongs.domain.mong.model.Mong
import com.monglife.mongs.domain.mong.model.MongOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagementPersistenceAdapter @Inject constructor(
    private val roomDB: MongRoomDB,
    private val mongDataStore: MongDataStore,
) : ManagementPersistencePort {

    /**
     * 현재 몽 ID 조회
     */
    @Throws(NotFoundCurrentMongIdException::class)
    override suspend fun getCurrentMongId(): Long =
        mongDataStore.getStore().data.map {
            it[MongDataStore.CURRENT_MONG_ID]?.let { currentMongId ->
                currentMongId.takeIf { it != MongDataStore.CURRENT_MONG_ID_INIT }
            }
        }.first() ?: throw NotFoundCurrentMongIdException()

    /**
     * 현재 몽 ID Flow 조회
     */
    @Throws(NotFoundCurrentMongIdException::class)
    override suspend fun getCurrentMongIdFlow(): Flow<Long?> =
        mongDataStore.getStore().data.map {
            it[MongDataStore.CURRENT_MONG_ID]?.let { currentMongId ->
                currentMongId.takeIf { it != MongDataStore.CURRENT_MONG_ID_INIT }
            }
        }

    /**
     * 현재 몽 ID 수정
     */
    override suspend fun setCurrentMongId(mongId: Long) {
        mongDataStore.getStore().edit {
            it[MongDataStore.CURRENT_MONG_ID] = mongId
        }
    }

    /**
     * 현재 몽 ID 삭제
     */
    override suspend fun deleteCurrentMongId() {
        mongDataStore.getStore().edit {
            it[MongDataStore.CURRENT_MONG_ID] = MongDataStore.CURRENT_MONG_ID_INIT
        }
    }

    /**
     * 몽 옵션 조회
     */
    @Throws(NotFoundMongOptionException::class)
    override suspend fun getMongOption(mongId: Long): MongOption =
        roomDB.mongDao().findMongOptionByMongId(mongId = mongId)?.toDomain()
            ?: throw NotFoundMongOptionException()

    /**
     * 몽 옵션 Flow 객체 조회
     */
    override suspend fun getMongOptionFlow(mongId: Long): Flow<MongOption?> =
        roomDB.mongDao().findMongOptionFlowByMongId(mongId = mongId).map {
            it?.toDomain()
        }

    /**
     * 몽 옵션 영속화
     */
    override suspend fun saveMongOption(mongOption: MongOption): MongOption =
        roomDB.mongDao().save(
            mongOptionEntity = MongOptionEntity(
                mongId = mongOption.mongId,
                graduateCheck = mongOption.graduateCheck,
            )
        ).toDomain()

    /**
     * 몽 옵션 삭제
     */
    override suspend fun deleteMongOption(mongId: Long) {
        roomDB.mongDao().deleteMongOptionByMongId(mongId = mongId)
    }

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
    override suspend fun getMongFlow(mongId: Long): Flow<Mong?> =
        roomDB.mongDao().findMongFlowByMongId(mongId = mongId).map {
            it?.toDomain()
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
            )
        ).toDomain()

    /**
     * 몽 삭제
     */
    override suspend fun deleteMong(mongId: Long) {
        roomDB.mongDao().deleteMongByMongId(mongId = mongId)
    }
}