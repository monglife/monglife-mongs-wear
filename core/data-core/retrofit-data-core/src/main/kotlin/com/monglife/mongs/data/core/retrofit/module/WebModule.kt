package com.monglife.mongs.data.core.retrofit.module

import com.google.gson.Gson
import com.monglife.mongs.data.core.retrofit.client.AuthWebClient
import com.monglife.mongs.data.core.retrofit.interceptor.AuthorizationInterceptor
import com.monglife.mongs.data.core.retrofit.interceptor.HttpLogInterceptor
import com.monglife.mongs.data.core.retrofit.datastore.SessionDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebModule {

    /**
     * 인증/인가 Interceptor Provider
     */
    @Provides
    @Singleton
    fun provideAuthorizationInterceptor(authWebClient: AuthWebClient, sessionDataStore: SessionDataStore) : AuthorizationInterceptor =
        AuthorizationInterceptor(authWebClient = authWebClient, sessionDataStore = sessionDataStore)

    /**
     * Http Logging Interceptor Provider
     */
    @Provides
    @Singleton
    fun provideHttpLogInterceptor(gson: Gson) : HttpLogInterceptor = HttpLogInterceptor(gson = gson)
}