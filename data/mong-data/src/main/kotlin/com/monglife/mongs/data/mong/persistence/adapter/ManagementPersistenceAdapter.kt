package com.monglife.mongs.data.mong.persistence.adapter

import android.util.Log
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongOptionException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.data.mong.persistence.db.MongRoomDB
import com.monglife.mongs.data.mong.persistence.entity.MongEntity
import com.monglife.mongs.data.mong.persistence.entity.MongOptionEntity
import com.monglife.mongs.domain.mong.model.Mong
import com.monglife.mongs.domain.mong.model.MongOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagementPersistenceAdapter @Inject constructor(
    private val roomDB: MongRoomDB,
) : ManagementPersistencePort {

    private val subscribeCounterMap = HashMap<Long, AtomicInteger>()
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
     * 몽 Flow 객체 조회
     */
    override suspend fun getMongFlow(mongId: Long): Flow<Mong?> = flow {

        val subscribeCount = subscribeCounterMap.getOrPut(mongId) { AtomicInteger(0) }

        if (subscribeCount.getAndIncrement() == 0) {
            // TODO: MQTT 구독
            Log.d("TEST", "subscribe mongId: $mongId")
        }

        try {
            emitAll(roomDB.mongDao().findMongFlowByMongId(mongId = mongId).map {
                it?.toDomain()
            })
        } finally {
            // TODO: MQTT 구독 해제
            if (subscribeCount.decrementAndGet() == 0) {
                Log.d("TEST", "disSubscribe mongId: $mongId")
                subscribeCounterMap.remove(mongId)
            }
        }
    }.shareIn(
        scope = applicationScope,
        started = SharingStarted.WhileSubscribed(),
        replay = 1,
    )

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

    /**
     * MongId 가 존재하지 않는 몽 삭제
     */
    override suspend fun deleteMongIfNotExistsMongIds(mongIds: List<Long>) {
        roomDB.mongDao().findAllNotExistsMongIds(mongIds = mongIds).forEach {
            roomDB.mongDao().deleteMongByMongId(mongId = it)
        }
    }
}