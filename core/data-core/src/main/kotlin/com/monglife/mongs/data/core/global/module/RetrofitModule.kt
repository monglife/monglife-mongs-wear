package com.monglife.mongs.data.core.global.module

import android.content.Context
import com.google.gson.Gson
import com.monglife.mongs.data.core.persistence.datastore.SessionDataStore
import com.monglife.mongs.data.core.web.client.AuthWebClient
import com.monglife.mongs.data.core.web.interceptor.AuthorizationInterceptor
import com.monglife.mongs.data.core.web.interceptor.HttpLogInterceptor
import com.mongs.wear.data.core.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

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
        .addInterceptor(httpLogInterceptor)
        .addInterceptor(authorizationInterceptor)
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