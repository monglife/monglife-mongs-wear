package com.monglife.mongs.data.core.retrofit.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val Context.store by preferencesDataStore(name = "SESSION")

    companion object {
        val ACCOUNT_ID = longPreferencesKey("accountId")
        val ACCESS_TOKEN = stringPreferencesKey("accessToken")
        val REFRESH_TOKEN = stringPreferencesKey("refreshToken")

        const val ACCOUNT_INIT_VALUE = Long.MIN_VALUE
        const val ACCESS_TOKEN_INIT_VALUE = ""
        const val REFRESH_TOKEN_INIT_VALUE = ""
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            context.store.edit { preferences ->
                if (!preferences.contains(ACCOUNT_ID)) preferences[ACCOUNT_ID] = ACCOUNT_INIT_VALUE
                if (!preferences.contains(ACCESS_TOKEN)) preferences[ACCESS_TOKEN] =
                    ACCESS_TOKEN_INIT_VALUE
                if (!preferences.contains(REFRESH_TOKEN)) preferences[REFRESH_TOKEN] =
                    REFRESH_TOKEN_INIT_VALUE
            }
        }
    }

    /**
     * data store 조회
     */
    fun getStore(): DataStore<Preferences> = context.store
}