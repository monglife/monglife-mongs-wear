package com.monglife.mongs.data.mong.persistence.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MongDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.store by preferencesDataStore(name = "MONG")

    companion object {
        val CURRENT_MONG_ID = longPreferencesKey("currentMongId")

        const val CURRENT_MONG_ID_INIT = -1L
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            context.store.edit { preferences ->
                if (!preferences.contains(CURRENT_MONG_ID)) preferences[CURRENT_MONG_ID] = CURRENT_MONG_ID_INIT
            }
        }
    }

    /**
     * data store 조회
     */
    fun getStore(): DataStore<Preferences> = context.store
}