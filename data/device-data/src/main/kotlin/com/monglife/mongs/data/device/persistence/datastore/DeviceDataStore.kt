package com.monglife.mongs.data.device.persistence.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.store by preferencesDataStore(name = "DEVICE")

    companion object {
        val WALKING_COUNT = intPreferencesKey("walkingCount")
        val CONSUME_WALKING_COUNT = intPreferencesKey("consumeWalkingCount")
        val BACKGROUND_MAP_CODE = stringPreferencesKey("backgroundMapCode")
        val NOTIFICATION_OPTION = booleanPreferencesKey("notificationOption")
        val SOUND_VOLUME = floatPreferencesKey("soundVolume")
        val MONG_INTERACTION_DIALOG_OPEN = booleanPreferencesKey("mongInteractionDialogOpen")

        const val WALKING_COUNT_INIT_VALUE = 0
        const val CONSUME_WALKING_COUNT_INIT_VALUE = 0
        const val BACKGROUND_MAP_CODE_INIT_VALUE = ""
        const val NOTIFICATION_OPTION_INIT_VALUE = true
        const val SOUND_VOLUME_INIT_VALUE = 0f
        const val MONG_INTERACTION_DIALOG_OPEN_INIT_VALUE = true
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            context.store.edit { preferences ->
                if (!preferences.contains(WALKING_COUNT)) preferences[WALKING_COUNT] =
                    WALKING_COUNT_INIT_VALUE
                if (!preferences.contains(CONSUME_WALKING_COUNT)) preferences[CONSUME_WALKING_COUNT] =
                    CONSUME_WALKING_COUNT_INIT_VALUE
                if (!preferences.contains(BACKGROUND_MAP_CODE)) preferences[BACKGROUND_MAP_CODE] =
                    BACKGROUND_MAP_CODE_INIT_VALUE
                if (!preferences.contains(NOTIFICATION_OPTION)) preferences[NOTIFICATION_OPTION] =
                    NOTIFICATION_OPTION_INIT_VALUE
                if (!preferences.contains(SOUND_VOLUME)) preferences[SOUND_VOLUME] =
                    SOUND_VOLUME_INIT_VALUE
                if (!preferences.contains(MONG_INTERACTION_DIALOG_OPEN)) preferences[MONG_INTERACTION_DIALOG_OPEN] =
                    MONG_INTERACTION_DIALOG_OPEN_INIT_VALUE
            }
        }
    }

    /**
     * data store 조회
     */
    fun getStore(): DataStore<Preferences> = context.store
}