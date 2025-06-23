package com.monglife.mongs.data.mong.web.client

import android.content.Context
import com.google.gson.Gson
import com.mongs.wear.data.mong.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    /**
     * Activity API Client Provider
     */
    @Provides
    @Singleton
    fun provideActivityWebClient(
        @ApplicationContext context: Context,
        gson: Gson,
        @Named("authorization") okHttpClient: OkHttpClient,
    ): ActivityWebClient = Retrofit.Builder()
        .baseUrl(context.getString(R.string.mongs_gateway_api_url) + "character/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
        .create(ActivityWebClient::class.java)

    /**
     * Interaction API Client Provider
     */
    @Provides
    @Singleton
    fun provideInteractionWebClient(
        @ApplicationContext context: Context,
        gson: Gson,
        @Named("authorization") okHttpClient: OkHttpClient,
    ): InteractionWebClient = Retrofit.Builder()
        .baseUrl(context.getString(R.string.mongs_gateway_api_url) + "character/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
        .create(InteractionWebClient::class.java)

    /**
     * Management API Client Provider
     */
    @Provides
    @Singleton
    fun provideManagementWebClient(
        @ApplicationContext context: Context,
        gson: Gson,
        @Named("authorization") okHttpClient: OkHttpClient,
    ): ManagementWebClient = Retrofit.Builder()
        .baseUrl(context.getString(R.string.mongs_gateway_api_url) + "character/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
        .create(ManagementWebClient::class.java)
}