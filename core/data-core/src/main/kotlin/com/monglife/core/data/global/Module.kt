package com.monglife.core.data.global

import android.content.Context
import android.content.pm.ApplicationInfo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.monglife.core.data.global.adapter.GsonLocalDateTimeFormatAdapter
import com.monglife.core.data.global.adapter.GsonLocalTimeAdapter
import com.monglife.core.data.persistence.datastore.SessionDataStore
import com.monglife.core.data.web.client.AuthWebClient
import com.monglife.core.data.web.interceptor.AuthorizationInterceptor
import com.monglife.core.data.web.interceptor.HttpLogInterceptor
import com.mongs.data.core.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
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
     * 디버그 빌드 여부
     * HttpLogInterceptor 는 Authorization 헤더와 요청/응답 바디 전문을 로그로 남기므로
     * 릴리스 빌드에서는 절대 붙이지 않는다.
     */
    private fun Context.isDebuggable(): Boolean =
        (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    /**
     * 커넥션 풀 / Dispatcher 공유
     * 클라이언트마다 새로 만들면 커넥션 풀과 스레드풀이 두 벌씩 생긴다.
     */
    @Provides
    @Singleton
    fun provideConnectionPool(): ConnectionPool = ConnectionPool()

    @Provides
    @Singleton
    fun provideDispatcher(): Dispatcher = Dispatcher()

    private fun OkHttpClient.Builder.applyCommon(
        context: Context,
        connectionPool: ConnectionPool,
        dispatcher: Dispatcher,
    ): OkHttpClient.Builder = this
        .connectionPool(connectionPool)
        .dispatcher(dispatcher)
        .connectTimeout(
            context.getString(R.string.mongs_gateway_api_connect_time_out).toLong(),
            TimeUnit.SECONDS
        )
        .readTimeout(context.getString(R.string.mongs_gateway_api_read_time_out).toLong(), TimeUnit.SECONDS)
        .writeTimeout(context.getString(R.string.mongs_gateway_api_write_time_out).toLong(), TimeUnit.SECONDS)

    /**
     * 인증, 인가 제외 Http Client
     */
    @Provides
    @Singleton
    @Named("no-authorization")
    fun provideOkhttp3Client(
        @ApplicationContext context: Context,
        httpLogInterceptor: HttpLogInterceptor,
        connectionPool: ConnectionPool,
        dispatcher: Dispatcher,
    ): OkHttpClient = OkHttpClient.Builder()
        .applyCommon(context = context, connectionPool = connectionPool, dispatcher = dispatcher)
        .apply {
            if (context.isDebuggable()) {
                addInterceptor(httpLogInterceptor)
            }
        }
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
        connectionPool: ConnectionPool,
        dispatcher: Dispatcher,
    ): OkHttpClient = OkHttpClient.Builder()
        .applyCommon(context = context, connectionPool = connectionPool, dispatcher = dispatcher)
        .addInterceptor(authorizationInterceptor)
        .apply {
            if (context.isDebuggable()) {
                addInterceptor(httpLogInterceptor)
            }
        }
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