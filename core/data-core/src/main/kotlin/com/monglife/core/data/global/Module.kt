package com.monglife.core.data.global

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.monglife.core.data.global.adapter.GsonLocalDateTimeFormatAdapter
import com.monglife.core.data.global.adapter.GsonLocalTimeAdapter
import com.monglife.core.data.persistence.datastore.SessionDataStore
import com.monglife.core.data.web.client.AuthWebClient
import com.monglife.core.data.web.interceptor.AuthorizationInterceptor
import com.monglife.core.data.web.interceptor.HttpLogInterceptor
import com.mongs.wear.data.core.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

/**
 * Gson Module
 */
@Module
@InstallIn(SingletonComponent::class)
object GsonModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, GsonLocalDateTimeFormatAdapter())
        .registerTypeAdapter(LocalTime::class.java, GsonLocalTimeAdapter())
        .create()
}

/**
 * Retrofit Client Module
 */
@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

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

    /**
     * 인증, 인가 제외 Http Client
     */
    @Provides
    @Singleton
    @Named("no-authorization")
    fun provideOkhttp3Client(
        @ApplicationContext context: Context,
        httpLogInterceptor: HttpLogInterceptor,
    ): OkHttpClient =  OkHttpClient.Builder()
        .connectTimeout(
            context.getString(R.string.mongs_gateway_api_connect_time_out).toLong(),
            TimeUnit.SECONDS
        )
        .readTimeout(context.getString(R.string.mongs_gateway_api_read_time_out).toLong(), TimeUnit.SECONDS)
        .writeTimeout(context.getString(R.string.mongs_gateway_api_write_time_out).toLong(), TimeUnit.SECONDS)
        .addInterceptor(httpLogInterceptor)
        .build()

    /**
     * 인증, 인가 Http Client
     */
    @Provides
    @Singleton
    @Named("authorization")
    fun provideOkhttp3ClientWithAuthorization(
        @ApplicationContext context: Context,
        httpLogInterceptor: HttpLogInterceptor,
        authorizationInterceptor: AuthorizationInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(
            context.getString(R.string.mongs_gateway_api_connect_time_out).toLong(),
            TimeUnit.SECONDS
        )
        .readTimeout(context.getString(R.string.mongs_gateway_api_read_time_out).toLong(), TimeUnit.SECONDS)
        .writeTimeout(context.getString(R.string.mongs_gateway_api_write_time_out).toLong(), TimeUnit.SECONDS)
        .addInterceptor(authorizationInterceptor)
        .addInterceptor(httpLogInterceptor)
        .build()

    /**
     * Discovery Common Retrofit
     */
    @Provides
    @Singleton
    @Named("monglife-discovery")
    fun provideRetrofit(
        @ApplicationContext context: Context,
        gson: Gson,
        @Named("no-authorization") okHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(context.getString(R.string.discovery_common_api_url))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()

    /**
     * Mongs Retrofit
     */
    @Provides
    @Singleton
    @Named("monglife-mongs")
    fun provideRetrofitWithAuthorization(
        @ApplicationContext context: Context,
        gson: Gson,
        @Named("authorization") okHttpClient: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(context.getString(R.string.mongs_gateway_api_url))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
}