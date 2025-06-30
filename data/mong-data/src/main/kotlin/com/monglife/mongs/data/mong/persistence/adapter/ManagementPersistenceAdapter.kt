package com.monglife.mongs.data.mong.persistence.adapter

import android.content.Context
import android.util.Log
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.exception.NotFoundMongOptionException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.data.core.mqtt.client.MqttClient
import com.monglife.mongs.data.mong.persistence.db.MongRoomDB
import com.monglife.mongs.data.mong.persistence.entity.MongEntity
import com.monglife.mongs.data.mong.persistence.entity.MongOptionEntity
import com.monglife.mongs.domain.mong.model.Mong
import com.monglife.mongs.domain.mong.model.MongOption
import com.mongs.wear.data.core.R
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ManagementPersistenceAdapterModule {
    @Binds
    @Singleton
    abstract fun bindManagementPersistencePort(adapter: ManagementPersistenceAdapter): ManagementPersistencePort
}

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

        val topic = "${context.getString(R.string.mongs_mqtt_topic)}/mong/management/$mongId"
        val subscribeCount = subscribeCounterMap.getOrPut(mongId) { AtomicInteger(0) }

        if (subscribeCount.getAndIncrement() == 0) {
            mqttClient.subscribe(topic = topic, callback = object : MqttCallback {
                override fun connectionLost(cause: Throwable?) {}
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    // TODO: MQTT 수신 업데이트 구현
                    Log.d("TEST", "${message}")
                }
            })
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