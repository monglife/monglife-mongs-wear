package com.monglife.mongs.data.device.persistence.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monglife.mongs.data.device.persistence.entity.DeviceOptionEntity
import com.monglife.mongs.data.device.persistence.entity.StepEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.store by preferencesDataStore(name = "DEVICE")

    companion object {
        private val WALKING_COUNT = intPreferencesKey("walkingCount")
        private val CONSUME_WALKING_COUNT = intPreferencesKey("consumeWalkingCount")
        private val CURRENT_MONG_ID = longPreferencesKey("currentMongId")
        private val BACKGROUND_MAP_CODE = stringPreferencesKey("backgroundMapCode")
        private val NOTIFICATION_OPTION = booleanPreferencesKey("notificationOption")
        private val SOUND_VOLUME = floatPreferencesKey("soundVolume")
        private val INIT_NOTIFICATION_DIALOG_OPEN = booleanPreferencesKey("initNotificationDialogOpen")
    }

    /**
     * Step 조회
     */
    suspend fun getStep(): StepEntity? = context.store.data.map {
        if (it.contains(WALKING_COUNT) && it.contains(CONSUME_WALKING_COUNT)) {
            StepEntity(
                walkingCount = it[WALKING_COUNT]!!,
                consumedWalkingCount = it[CONSUME_WALKING_COUNT]!!
            )
        } else {
            null
        }
    }.first()

    /**
     * Step 조회
     */
    fun getStepFlow(): Flow<StepEntity?> = context.store.data.map {
        if (it.contains(WALKING_COUNT) && it.contains(CONSUME_WALKING_COUNT)) {
            StepEntity(
                walkingCount = it[WALKING_COUNT]!!,
                consumedWalkingCount = it[CONSUME_WALKING_COUNT]!!
            )
        } else {
            null
        }
    }

    /**
     * Step 저장
     */
    suspend fun saveStep(stepEntity: StepEntity): StepEntity = context.store.let { store ->
        store.edit { preferences ->
            preferences[WALKING_COUNT] = stepEntity.walkingCount
            preferences[CONSUME_WALKING_COUNT] = stepEntity.consumedWalkingCount
        }

        store.data.map {
            StepEntity(
                walkingCount = it[WALKING_COUNT]!!,
                consumedWalkingCount = it[CONSUME_WALKING_COUNT]!!
            )
        }.first()
    }

    /**
     * Step 삭제
     */
    suspend fun deleteStep(): StepEntity = context.store.let { store ->
        val stepEntity = store.data.map {
            StepEntity(
                walkingCount = it[WALKING_COUNT]!!,
                consumedWalkingCount = it[CONSUME_WALKING_COUNT]!!
            )
        }

        store.edit { preferences ->
            if (preferences.contains(WALKING_COUNT)) preferences.remove(WALKING_COUNT)
            if (preferences.contains(CONSUME_WALKING_COUNT)) preferences.remove(CONSUME_WALKING_COUNT)
        }

        stepEntity.first()
    }

    /**
     * DeviceOption 조회
     */
    suspend fun getDeviceOption(): DeviceOptionEntity? = context.store.data.map {
        if (
            it.contains(CURRENT_MONG_ID) &&
            it.contains(BACKGROUND_MAP_CODE) &&
            it.contains(NOTIFICATION_OPTION) &&
            it.contains(SOUND_VOLUME) &&
            it.contains(INIT_NOTIFICATION_DIALOG_OPEN)
        ) {
            DeviceOptionEntity(
                currentMongId = if (it[CURRENT_MONG_ID]!! == -1L) null else it[CURRENT_MONG_ID]!!,
                backgroundMapCode = it[BACKGROUND_MAP_CODE]!!.ifBlank { null },
                notificationOption = it[NOTIFICATION_OPTION]!!,
                soundVolume = it[SOUND_VOLUME]!!,
                initNotificationDialogOpen = it[INIT_NOTIFICATION_DIALOG_OPEN]!!,
            )
        } else {
            null
        }
    }.first()

    /**
     * DeviceOption 조회
     */
    fun getDeviceOptionFlow(): Flow<DeviceOptionEntity?> = context.store.data.map {
        if (
            it.contains(CURRENT_MONG_ID) &&
            it.contains(BACKGROUND_MAP_CODE) &&
            it.contains(NOTIFICATION_OPTION) &&
            it.contains(SOUND_VOLUME) &&
            it.contains(INIT_NOTIFICATION_DIALOG_OPEN)
        ) {
            DeviceOptionEntity(
                currentMongId = if (it[CURRENT_MONG_ID]!! == -1L) null else it[CURRENT_MONG_ID]!!,
                backgroundMapCode = it[BACKGROUND_MAP_CODE]!!.ifBlank { null },
                notificationOption = it[NOTIFICATION_OPTION]!!,
                soundVolume = it[SOUND_VOLUME]!!,
                initNotificationDialogOpen = it[INIT_NOTIFICATION_DIALOG_OPEN]!!,
            )
        } else {
            null
        }
    }

    /**
     * DeviceOption 저장
     */
    suspend fun saveDeviceOption(deviceOptionEntity: DeviceOptionEntity): DeviceOptionEntity = context.store.let { store ->
        store.edit { preferences ->
            deviceOptionEntity.currentMongId
                ?.let { preferences[CURRENT_MONG_ID] = it }
                ?:run { preferences[CURRENT_MONG_ID] = -1 }
            deviceOptionEntity.backgroundMapCode
                ?.let { preferences[BACKGROUND_MAP_CODE] = it }
                ?:run { preferences[BACKGROUND_MAP_CODE] = "" }
            preferences[NOTIFICATION_OPTION] = deviceOptionEntity.notificationOption
            preferences[SOUND_VOLUME] = deviceOptionEntity.soundVolume
            preferences[INIT_NOTIFICATION_DIALOG_OPEN] = deviceOptionEntity.initNotificationDialogOpen
        }

        store.data.map {
            DeviceOptionEntity(
                currentMongId = if (it[CURRENT_MONG_ID]!! == -1L) null else it[CURRENT_MONG_ID]!!,
                backgroundMapCode = it[BACKGROUND_MAP_CODE]!!.ifBlank { null },
                notificationOption = it[NOTIFICATION_OPTION]!!,
                soundVolume = it[SOUND_VOLUME]!!,
                initNotificationDialogOpen = it[INIT_NOTIFICATION_DIALOG_OPEN]!!,
            )
        }.first()
    }

    /**
     * DeviceOption 삭제
     */
    suspend fun deleteDeviceOption(): DeviceOptionEntity = context.store.let { store ->
        val deviceOptionEntity = store.data.map {
            DeviceOptionEntity(
                currentMongId = if (it[CURRENT_MONG_ID]!! == -1L) null else it[CURRENT_MONG_ID]!!,
                backgroundMapCode = it[BACKGROUND_MAP_CODE]!!.ifBlank { null },
                notificationOption = it[NOTIFICATION_OPTION]!!,
                soundVolume = it[SOUND_VOLUME]!!,
                initNotificationDialogOpen = it[INIT_NOTIFICATION_DIALOG_OPEN]!!,
            )
        }

        store.edit { preferences ->
            preferences[CURRENT_MONG_ID] = -1
            preferences[BACKGROUND_MAP_CODE] = ""
            preferences[NOTIFICATION_OPTION] = true
            preferences[SOUND_VOLUME] = 1f
            preferences[INIT_NOTIFICATION_DIALOG_OPEN] = true
        }

        deviceOptionEntity.first()
    }
}