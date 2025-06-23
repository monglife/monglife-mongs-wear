package com.monglife.mongs.data.core.client

import android.content.Context
import com.google.gson.Gson
import com.mongs.wear.data.core.R
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
     * Auth API Client Provider
     */
    @Provides
    @Singleton
    fun provideAuthWebClient(
        @ApplicationContext context: Context,
        gson: Gson,
        @Named("no-authorization") okHttpClient: OkHttpClient,
    ): AuthWebClient = Retrofit.Builder()
        .baseUrl(context.getString(R.string.discovery_common_api_url))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
        .create(AuthWebClient::class.java)
}