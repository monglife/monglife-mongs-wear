package com.monglife.mongs.data.core.retrofit.module

import android.content.Context
import com.google.gson.Gson
import com.monglife.mongs.data.core.retrofit.client.AuthWebClient
import com.monglife.mongs.data.core.retrofit.interceptor.HttpLogInterceptor
import com.mongs.wear.core.data.retrofit.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    /**
     * Auth API Client Provider
     */
    @Provides
    @Singleton
    fun provideAuthWebClient(@ApplicationContext context: Context, gson: Gson, httpLogInterceptor: HttpLogInterceptor): AuthWebClient {

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(context.getString(R.string.api_connect_time_out).toLong(), TimeUnit.SECONDS)
            .readTimeout(context.getString(R.string.api_read_time_out).toLong(), TimeUnit.SECONDS)
            .writeTimeout(context.getString(R.string.api_write_time_out).toLong(), TimeUnit.SECONDS)
            .addInterceptor(httpLogInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(context.getString(R.string.api_url))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()

        return retrofit.create(AuthWebClient::class.java)
    }
}