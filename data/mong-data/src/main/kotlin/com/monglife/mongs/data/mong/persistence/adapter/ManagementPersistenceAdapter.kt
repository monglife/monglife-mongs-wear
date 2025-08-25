package com.monglife.mongs.data.mong.persistence.adapter

import android.content.Context
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.data.mong.persistence.db.MongRoomDB
import com.monglife.mongs.data.mong.persistence.dto.ManagementEventDto
import com.monglife.mongs.data.mong.persistence.entity.MongEntity
import com.monglife.mongs.data.mong.persistence.entity.MongOptionEntity
import com.monglife.mongs.domain.mong.model.Mong
import com.monglife.mongs.domain.mong.model.MongOption
import com.mongs.data.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManagementPersistenceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val roomDB: MongRoomDB,
    private val mqttClient: MqttClient,
) : ManagementPersistencePort {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val subscribeCounterMap = ConcurrentHashMap<Long, AtomicInteger>()

    /**
     * 몽 옵션 조회
     */
    override suspend fun getMongOption(mongId: Long): MongOption? =
        roomDB.mongDao().findMongOptionByMongId(mongId = mongId)?.toDomain()

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
    override suspend fun getMong(mongId: Long): Mong? =
        roomDB.mongDao().findMongByMongId(mongId = mongId)?.toDomain()

    /**
     * 몽 Flow 객체 조회
     */
    override suspend fun getMongFlow(mongId: Long): Flow<Mong?> = flow {

        val subscribeCount = subscribeCounterMap.getOrPut(mongId) { AtomicInteger(0) }
        val topic = "${context.getString(R.string.mongs_mqtt_topic)}/mong/management/$mongId"

        if (subscribeCount.getAndIncrement() == 0) {
            mqttClient.subscribe(
                topic = topic,
                classType = ManagementEventDto::class.java,
                onReceive = { responseDto ->
                    roomDB.mongDao().findMongByMongId(mongId = responseDto.result.mongId)
                        ?.let { mongEntity ->
                            roomDB.mongDao().save(
                                mongEntity = MongEntity(
                                    mongId = responseDto.result.mongId,
                                    name = responseDto.result.name,
                                    mongCode = responseDto.result.mongCode,
                                    mongName = responseDto.result.mongName,
                                    stateCode = responseDto.result.stateCode,
                                    statusCode = responseDto.result.statusCode,
                                    level = mongEntity.level,
                                    sleepAt = mongEntity.sleepAt,
                                    wakeupAt = mongEntity.wakeupAt,
                                    payPoint = responseDto.result.payPoint,
                                    isSleep = responseDto.result.isSleep,
                                    strengthRatio = responseDto.result.strengthRatio,
                                    healthyRatio = responseDto.result.healthyRatio,
                                    satietyRatio = responseDto.result.satietyRatio,
                                    fatigueRatio = responseDto.result.fatigueRatio,
                                    expRatio = responseDto.result.expRatio,
                                    weight = responseDto.result.weight,
                                    poopCount = responseDto.result.poopCount,
                                    randomDrawTicketCount = mongEntity.randomDrawTicketCount,
                                    createdAt = mongEntity.createdAt,
                                    updatedAt = mongEntity.updatedAt,
                                )
                            )
                        }
                }
            )
        }

        try {
            emitAll(roomDB.mongDao().findMongFlowByMongId(mongId = mongId).map { it?.toDomain() })
        } finally {
            if (subscribeCount.decrementAndGet() == 0) {
                mqttClient.disSubscribe(topic = topic)
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