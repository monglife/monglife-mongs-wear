package com.monglife.mongs.data.device.persistence.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.mongs.application.auth.exception.InvalidLogoutException
import com.monglife.mongs.data.device.persistence.datastore.DeviceDataStore
import com.monglife.mongs.data.device.persistence.dto.DeviceEventDto
import com.monglife.mongs.data.device.persistence.entity.DeviceOptionEntity
import com.monglife.mongs.data.device.persistence.entity.StepEntity
import com.monglife.mongs.data.device.persistence.manager.StepSensorManager
import com.monglife.mongs.domain.device.model.DeviceOption
import com.monglife.mongs.domain.device.model.Step
import com.mongs.wear.data.core.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@SuppressLint("HardwareIds")
@Singleton
class DevicePersistenceAdapter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseMessaging: FirebaseMessaging,
    private val stepSensorManager: StepSensorManager,
    private val deviceDataStore: DeviceDataStore,
    private val mqttClient: MqttClient,
) : com.monglife.mongs.application.auth.port.persistence.DevicePersistencePort,
    com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort,
    com.monglife.mongs.application.device.port.persistence.DevicePersistencePort,
    com.monglife.mongs.application.member.feedback.port.persistence.DevicePersistencePort,
    com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val subscribeCounterMap = ConcurrentHashMap<String, AtomicInteger>()

    /**
     * 현재 몽 ID 조회
     */
    override suspend fun getCurrentMongId(): Long? = this.getDeviceOption().currentMongId

    /**
     * 현재 몽 ID Flow 조회
     */
    override suspend fun getCurrentMongIdFlow(): Flow<Long?> = this.getDeviceOptionFlow().map { it.currentMongId }

    /**
     * 현재 몽 ID 수정
     */
    override suspend fun setCurrentMongId(mongId: Long) {
        this.getDeviceOption().let { deviceOptionEntity ->
            this.saveDeviceOption(
                DeviceOption(
                    currentMongId = mongId,
                    backgroundMapCode = deviceOptionEntity.backgroundMapCode,
                    notificationOption = deviceOptionEntity.notificationOption,
                    soundVolume = deviceOptionEntity.soundVolume,
                    initNotificationDialogOpen = deviceOptionEntity.initNotificationDialogOpen,
                )
            )
        }
    }

    /**
     * 현재 몽 ID 삭제
     */
    override suspend fun deleteCurrentMongId() {
        this.getDeviceOption().let { deviceOptionEntity ->
            this.saveDeviceOption(
                DeviceOption(
                    currentMongId = null,
                    backgroundMapCode = deviceOptionEntity.backgroundMapCode,
                    notificationOption = deviceOptionEntity.notificationOption,
                    soundVolume = deviceOptionEntity.soundVolume,
                    initNotificationDialogOpen = deviceOptionEntity.initNotificationDialogOpen,
                )
            )
        }
    }

    /**
     * 걸음 수 조회
     */
    override suspend fun getStep(): Step {
        val stepEntity = deviceDataStore.getStep() ?: deviceDataStore.saveStep(
            StepEntity(
                walkingCount = 0,
                consumedWalkingCount = 0
            )
        )

        val totalWalkingCount = stepSensorManager.getTotalWalkingCount()
        val deviceBootedAt = this.getBootedAt()

        return Step(
            totalWalkingCount = totalWalkingCount,
            deviceBootedAt = deviceBootedAt,
            walkingCount = stepEntity.walkingCount,
            consumedWalkingCount = stepEntity.consumedWalkingCount,
        )
    }

    /**
     * 걸음 수 Flow 객체 조회
     */
    override suspend fun getStepFlow(): Flow<Step> = flow {
        val deviceId = this@DevicePersistenceAdapter.getDeviceId()

        deviceDataStore.getStep() ?: run {
            deviceDataStore.saveStep(
                StepEntity(
                    walkingCount = 0,
                    consumedWalkingCount = 0
                )
            )
        }

        val subscribeCount = subscribeCounterMap.getOrPut(deviceId) { AtomicInteger(0) }
        val topic = "${context.getString(R.string.mongs_mqtt_topic)}/device/$deviceId"

        if (subscribeCount.getAndIncrement() == 0) {
            mqttClient.subscribe(topic = topic,
                classType = DeviceEventDto::class.java,
                onReceive = { responseDto ->
                    deviceDataStore.getStep() ?: run {
                        deviceDataStore.saveStep(
                            StepEntity(
                                walkingCount = responseDto.result.walkingCount,
                                consumedWalkingCount = responseDto.result.consumeWalkingCount,
                            )
                        )
                    }
                }
            )
        }

        try {
            emitAll(
                combine(
                    deviceDataStore.getStepFlow(),
                    stepSensorManager.getTotalWalkingCountFlow(),
                ) { stepEntity, totalWalkingCount ->
                    Step(
                        totalWalkingCount = totalWalkingCount,
                        deviceBootedAt = this@DevicePersistenceAdapter.getBootedAt(),
                        walkingCount = stepEntity?.walkingCount ?: 0,
                        consumedWalkingCount = stepEntity?.consumedWalkingCount ?: 0,
                    )
                })
        } finally {
            if (subscribeCount.decrementAndGet() == 0) {
                mqttClient.disSubscribe(topic = topic)
                subscribeCounterMap.remove(deviceId)
            }
        }
    }.shareIn(
        scope = applicationScope,
        started = SharingStarted.WhileSubscribed(),
        replay = 1,
    )

    /**
     * 걸음 수 로컬 동기화
     */
    override suspend fun saveStep(step: Step): Step {
        val totalWalkingCount = stepSensorManager.getTotalWalkingCount()
        val deviceBootedAt = this.getBootedAt()

        val stepEntity = deviceDataStore.saveStep(
            stepEntity = StepEntity(
                walkingCount = step.walkingCount,
                consumedWalkingCount = step.consumedWalkingCount
            )
        )

        return Step(
            totalWalkingCount = totalWalkingCount,
            deviceBootedAt = deviceBootedAt,
            walkingCount = stepEntity.walkingCount,
            consumedWalkingCount = stepEntity.consumedWalkingCount,
        )
    }

    /**
     * 기기 옵션 조회
     */
    override suspend fun getDeviceOption(): DeviceOption {
        val deviceOptionEntity = deviceDataStore.getDeviceOption() ?: deviceDataStore.saveDeviceOption(
            DeviceOptionEntity(
                currentMongId = null,
                backgroundMapCode = null,
                notificationOption = false,
                soundVolume = 1f,
                initNotificationDialogOpen = true,
            )
        )

        return deviceOptionEntity.toDomain()
    }

    /**
     * 기기 옵션 라이브 객체 조회
     */
    override suspend fun getDeviceOptionFlow(): Flow<DeviceOption> {
        deviceDataStore.getDeviceOption() ?: run {
            deviceDataStore.saveDeviceOption(
                DeviceOptionEntity(
                    currentMongId = null,
                    backgroundMapCode = null,
                    notificationOption = false,
                    soundVolume = 1f,
                    initNotificationDialogOpen = true,
                )
            )
        }

        return deviceDataStore.getDeviceOptionFlow().map {
            DeviceOption(
                currentMongId = it?.currentMongId,
                backgroundMapCode = it?.backgroundMapCode,
                notificationOption = it?.notificationOption ?: false,
                soundVolume = it?.soundVolume ?: 1f,
                initNotificationDialogOpen = it?.initNotificationDialogOpen ?: true,
            )
        }
    }

    /**
     * 기기 옵션 수정
     */
    override suspend fun saveDeviceOption(deviceOption: DeviceOption): DeviceOption =
        deviceDataStore.saveDeviceOption(
            DeviceOptionEntity(
                currentMongId = deviceOption.currentMongId,
                backgroundMapCode = deviceOption.backgroundMapCode,
                notificationOption = deviceOption.notificationOption,
                soundVolume = deviceOption.soundVolume,
                initNotificationDialogOpen = deviceOption.initNotificationDialogOpen,
            )
        ).toDomain()

    /**
     * 기기 ID 조회
     */
    override suspend fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    /**
     * 기기명 조회
     */
    override suspend fun getDeviceName(): String {
        return android.os.Build.MODEL
    }

    /**
     * 앱 패키지명 조회
     */
    override suspend fun getAppPackageName(): String {
        return context.packageName.takeIf { it.isNotEmpty() } ?: ""
    }

    /**
     * 앱 버전 조회
     */
    override suspend fun getBuildVersion(): String {
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "0.0.0"
    }

    /**
     * FCM 토큰 조회
     */
    override suspend fun getFcmToken(): String {
        return firebaseMessaging.getTokenSuspend()
    }

    /**
     * 기기 부팅 시간 조회
     */
    private fun getBootedAt(): LocalDateTime {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
        val uptimeMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime()
        val deviceBootedDt =
            Instant.ofEpochMilli(uptimeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        return LocalDateTime.parse(deviceBootedDt.format(dateFormatter), dateFormatter)
    }

    /**
     * FCM 토큰 조회 인라인 함수
     */
    private suspend fun FirebaseMessaging.getTokenSuspend(): String =
        suspendCancellableCoroutine { cont ->
            getToken().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cont.resume(task.result ?: "")
                } else {
                    cont.resumeWithException(task.exception ?: InvalidLogoutException())
                }
            }
        }
}