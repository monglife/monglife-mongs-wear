package com.monglife.mongs.data.device.persistence.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import androidx.datastore.preferences.core.edit
import com.google.firebase.messaging.FirebaseMessaging
import com.monglife.mongs.application.auth.exception.InvalidLogoutException
import com.monglife.mongs.application.device.exception.NotFoundDeviceOptionException
import com.monglife.mongs.data.device.persistence.datastore.DeviceDataStore
import com.monglife.mongs.data.device.persistence.manager.StepSensorManager
import com.monglife.mongs.domain.device.model.DeviceOption
import com.monglife.mongs.domain.device.model.Step
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
    private val deviceDataStore: DeviceDataStore
) : com.monglife.mongs.application.auth.port.persistence.DevicePersistencePort,
    com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort,
    com.monglife.mongs.application.device.port.persistence.DevicePersistencePort,
    com.monglife.mongs.application.member.feedback.port.persistence.DevicePersistencePort {

    companion object {
        private val DATE_FORMATER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
    }

    /**
     * 걸음 수 조회
     */
    override suspend fun getStep(): Step {
        val totalWalkingCount = stepSensorManager.getTotalWalkingCount()
        val deviceBootedAt = this.getBootedAt()

        return deviceDataStore.getStore().data.map {
            Step(
                totalWalkingCount = totalWalkingCount,
                deviceBootedAt = deviceBootedAt,
                walkingCount = it[DeviceDataStore.WALKING_COUNT] ?: 0,
                consumedWalkingCount = it[DeviceDataStore.CONSUME_WALKING_COUNT] ?: 0,
            )
        }.first()
    }

    /**
     * 걸음 수 라이브 객체 조회
     */
    override suspend fun getStepFlow(): Flow<Step> {
        val totalWalkingCountFlow = stepSensorManager.getTotalWalkingCountFlow()
        val deviceBootedAt = this.getBootedAt()
        val walkingCountFlow =
            deviceDataStore.getStore().data.map { it[DeviceDataStore.WALKING_COUNT] ?: 0 }
        val consumedWalkingCountFlow =
            deviceDataStore.getStore().data.map { it[DeviceDataStore.CONSUME_WALKING_COUNT] ?: 0 }

        return combine(
            totalWalkingCountFlow,
            walkingCountFlow,
            consumedWalkingCountFlow
        ) { totalWalkingCount, walkingCount, consumedWalkingCount ->
            Step(
                totalWalkingCount = totalWalkingCount,
                deviceBootedAt = deviceBootedAt,
                walkingCount = walkingCount,
                consumedWalkingCount = consumedWalkingCount,
            )
        }
    }

    /**
     * 걸음 수 로컬 동기화
     */
    override suspend fun saveStep(step: Step): Step {
        deviceDataStore.getStore().edit { preferences ->
            preferences[DeviceDataStore.WALKING_COUNT] = step.walkingCount
            preferences[DeviceDataStore.CONSUME_WALKING_COUNT] = step.consumedWalkingCount
        }

        val totalWalkingCount = stepSensorManager.getTotalWalkingCount()
        val deviceBootedAt = this.getBootedAt()

        return deviceDataStore.getStore().data.map {
            Step(
                totalWalkingCount = totalWalkingCount,
                deviceBootedAt = deviceBootedAt,
                walkingCount = it[DeviceDataStore.WALKING_COUNT] ?: 0,
                consumedWalkingCount = it[DeviceDataStore.CONSUME_WALKING_COUNT] ?: 0,
            )
        }.first()
    }

    /**
     * 기기 옵션 조회
     */
    @Throws(NotFoundDeviceOptionException::class)
    override suspend fun getDeviceOption(): DeviceOption = deviceDataStore.getStore().data.map {
        DeviceOption(
            backgroundMapCode = it[DeviceDataStore.BACKGROUND_MAP_CODE] ?: "",
            notificationOption = it[DeviceDataStore.NOTIFICATION_OPTION] ?: true,
            soundVolume = it[DeviceDataStore.SOUND_VOLUME] ?: 1f,
            mongInteractionDialogOpen = it[DeviceDataStore.MONG_INTERACTION_DIALOG_OPEN] ?: true,
        )
    }.first()

    /**
     * 기기 옵션 라이브 객체 조회
     */
    @Throws(NotFoundDeviceOptionException::class)
    override suspend fun getDeviceOptionFlow(): Flow<DeviceOption> =
        deviceDataStore.getStore().data.map {
            DeviceOption(
                backgroundMapCode = it[DeviceDataStore.BACKGROUND_MAP_CODE] ?: "",
                notificationOption = it[DeviceDataStore.NOTIFICATION_OPTION] ?: true,
                soundVolume = it[DeviceDataStore.SOUND_VOLUME] ?: 1f,
                mongInteractionDialogOpen = it[DeviceDataStore.MONG_INTERACTION_DIALOG_OPEN]
                    ?: true,
            )
        }

    /**
     * 기기 옵션 수정
     */
    override suspend fun saveDeviceOption(deviceOption: DeviceOption): DeviceOption {
        deviceDataStore.getStore().edit { preferences ->
            preferences[DeviceDataStore.BACKGROUND_MAP_CODE] = deviceOption.backgroundMapCode
            preferences[DeviceDataStore.NOTIFICATION_OPTION] = deviceOption.notificationOption
            preferences[DeviceDataStore.SOUND_VOLUME] = deviceOption.soundVolume
            preferences[DeviceDataStore.MONG_INTERACTION_DIALOG_OPEN] =
                deviceOption.mongInteractionDialogOpen
        }

        return deviceDataStore.getStore().data.map {
            DeviceOption(
                backgroundMapCode = it[DeviceDataStore.BACKGROUND_MAP_CODE] ?: "",
                notificationOption = it[DeviceDataStore.NOTIFICATION_OPTION] ?: true,
                soundVolume = it[DeviceDataStore.SOUND_VOLUME] ?: 1f,
                mongInteractionDialogOpen = it[DeviceDataStore.MONG_INTERACTION_DIALOG_OPEN]
                    ?: true,
            )
        }.first()
    }

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
        val uptimeMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime()
        val deviceBootedDt =
            Instant.ofEpochMilli(uptimeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        return LocalDateTime.parse(deviceBootedDt.format(DATE_FORMATER), DATE_FORMATER)
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