package com.monglife.mongs.data.member.player.persistence.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.store by preferencesDataStore(name = "PLAYER")

    companion object {
        val ACCOUNT_ID = longPreferencesKey("accountId")
        val SLOT_COUNT = intPreferencesKey("slotCount")
        val STAR_POINT = intPreferencesKey("starPoint")

        const val SLOT_COUNT_INIT = 0
        const val STAR_POINT_INIT = 0
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            context.store.edit { preferences ->
                if (!preferences.contains(SLOT_COUNT)) preferences[SLOT_COUNT] = SLOT_COUNT_INIT
                if (!preferences.contains(STAR_POINT)) preferences[STAR_POINT] = STAR_POINT_INIT
            }
        }
    }

    /**
     * data store 조회
     */
    fun getStore(): DataStore<Preferences> = context.store
}