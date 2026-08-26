package com.monglife.mongs.data.device.persistence.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.google.firebase.messaging.FirebaseMessaging
import com.monglife.core.data.flow.SharedFlowCache
import com.monglife.core.data.mqtt.client.MqttClient
import com.monglife.mongs.application.auth.exception.InvalidLogoutException
import com.monglife.mongs.data.device.persistence.collector.StepCollector
import com.monglife.mongs.data.device.persistence.coordinator.StepCollectionCoordinator
import com.monglife.mongs.data.device.persistence.datastore.DeviceDataStore
import com.monglife.mongs.data.device.persistence.dto.StepRestoreEventDto
import com.monglife.mongs.data.device.persistence.entity.DeviceOptionEntity
import com.monglife.mongs.data.device.persistence.entity.StepStateEntity
import com.monglife.mongs.data.device.persistence.manager.StepSensorManager
import com.monglife.mongs.domain.device.model.DeviceOption
import com.monglife.mongs.domain.device.model.Step
import dagger.hilt.android.qualifiers.ApplicationContext
import com.mongs.data.core.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.suspendCancellableCoroutine
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
    private val stepCollector: StepCollector,
    private val stepCollectionCoordinator: StepCollectionCoordinator,
    private val deviceDataStore: DeviceDataStore,
    private val mqttClient: MqttClient,
) : com.monglife.mongs.application.auth.port.persistence.DevicePersistencePort,
    com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort,
    com.monglife.mongs.application.device.port.persistence.DevicePersistencePort,
    com.monglife.mongs.application.member.feedback.port.persistence.DevicePersistencePort,
    com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort {

    /**
     * 걸음 수 SharedFlow 캐시 (키 없음)
     * 호출할 때마다 새로 만들면 화면 수만큼 센서 리스너가 중복 등록된다.
     */
    private val stepFlowCache = SharedFlowCache<Unit, Step>()

    private val subscribeCounterMap = ConcurrentHashMap<String, AtomicInteger>()

    /**
     * 현재 몽 ID 조회
     */
    override suspend fun getCurrentMongId(): Long? = this.getDeviceOption().currentMongId

    /**
     * 현재 몽 ID Flow 조회
     */
    override suspend fun getCurrentMongIdFlow(): Flow<Long?> =
        this.getDeviceOptionFlow()
            .map { it.currentMongId }
            // 걸음 수와 같은 DataStore 를 쓰므로 값이 같아도 재발행된다.
            // 상위 flatMapLatest 가 몽 구독을 취소/재생성하지 않도록 여기서 걸러 낸다.
            .distinctUntilChanged()

    /**
     * 현재 몽 ID 수정
     */
    override suspend fun setCurrentMongId(mongId: Long): Long {
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

        return mongId
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
     * 걸음 수 수집 시작
     */
    override suspend fun startStepCollection() = stepCollectionCoordinator.synchronize()

    /**
     * 걸음 수 조회
     *
     * 환전 검증에 쓰이므로 확정 잔액만 담는다 (pendingWalkingCount 없음).
     */
    override suspend fun getStep(): Step = deviceDataStore.getStepState().toDomain()

    /**
     * 걸음 수 Flow 조회
     */
    override suspend fun getStepFlow(): Flow<Step> =
        stepFlowCache.getOrCreate(key = Unit) { createStepFlow() }

    /**
     * 걸음 수 차감 (환전)
     */
    override suspend fun consumeWalkingCount(walkingCount: Int): Step {
        stepCollector.consume(walkingCount)
        return this.getStep()
    }

    /**
     * 화면이 열려 있는 동안의 걸음 수 Flow
     *
     * 저장된 지갑에 "아직 지갑에 안 들어온 걸음"을 얹어 준다. Health Services 는 걸음을 배치로
     * 내려주기 때문에 그대로 두면 걷는 중에 화면 숫자가 몇 분씩 멈춰 있는다.
     *
     * 이중 카운트가 나지 않는 이유: 얹는 값은 "지갑이 마지막으로 갱신된 시점 이후 센서가 센 걸음"
     * 이고, 배치가 도착해 지갑이 갱신될 때마다 기준선을 그 시점 센서 값으로 다시 잡기 때문이다.
     * 얹은 값은 저장되지 않고 표시에만 쓰인다.
     *
     * 센서 폴백 모드에서는 같은 Flow 가 적립까지 담당한다 (onEach). 활성 경로가 아닌 입력은
     * DataStore 트랜잭션 안의 게이트가 버리므로, Health Services 모드에서 이 호출은 무해하다.
     */
    private fun createStepFlow(): Flow<Step> = flow {
        // 환전 실패 복구 알림은 화면과 무관하게 도착하지만, MQTT 연결 자체가 앱이 떠 있는 동안만
        // 유지되므로 걸음 화면을 보는 동안 구독한다. 환전 직후가 복구가 도착하는 시점이라
        // 실제로 필요한 구간은 덮인다. 앱이 꺼진 사이 온 복구는 유실된다 - 서버도 retained 를
        // 쓰지 않으므로 이 채널은 최선 노력이다.
        val deviceId = this@DevicePersistenceAdapter.getDeviceId()
        val topic = "${context.getString(R.string.mongs_mqtt_topic)}/device/$deviceId/step/restore"
        val subscribeCount = subscribeCounterMap.getOrPut(deviceId) { AtomicInteger(0) }

        if (subscribeCount.getAndIncrement() == 0) {
            mqttClient.subscribe(
                topic = topic,
                classType = StepRestoreEventDto::class.java,
                onReceive = { responseDto ->
                    stepCollector.restore(
                        restoreWalkingCount = responseDto.result.restoreWalkingCount,
                        eventId = responseDto.result.eventId,
                    )
                }
            )
        }

        try {
            emitAll(stepProgressFlow())
        } finally {
            if (subscribeCount.decrementAndGet() == 0) {
                mqttClient.disSubscribe(topic = topic)
                subscribeCounterMap.remove(deviceId)
            }
        }
    }

    private fun stepProgressFlow(): Flow<Step> = merge(
        deviceDataStore.getStepStateFlow().map { StepSignal.State(it) },
        stepSensorManager.observeTotalWalkingCount()
            .onEach { stepCollector.creditSensorTotal(it) }
            .map { StepSignal.Sensor(it) },
    )
        .scan(StepProgress()) { progress, signal ->
            when (signal) {
                is StepSignal.State -> progress.copy(
                    state = signal.state,
                    sensorBaseline = progress.sensorTotal,
                )

                is StepSignal.Sensor -> progress.copy(
                    sensorTotal = signal.total,
                    sensorBaseline = progress.sensorBaseline ?: signal.total,
                )
            }
        }
        .mapNotNull { progress ->
            progress.state?.toDomain(pendingWalkingCount = progress.pendingWalkingCount())
        }
        .distinctUntilChanged()

    private sealed interface StepSignal {
        data class State(val state: StepStateEntity) : StepSignal
        data class Sensor(val total: Int) : StepSignal
    }

    private data class StepProgress(
        val state: StepStateEntity? = null,
        val sensorBaseline: Int? = null,
        val sensorTotal: Int? = null,
    ) {
        fun pendingWalkingCount(): Int {
            val baseline = sensorBaseline ?: return 0
            val total = sensorTotal ?: return 0
            return (total - baseline).coerceAtLeast(0)
        }
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
