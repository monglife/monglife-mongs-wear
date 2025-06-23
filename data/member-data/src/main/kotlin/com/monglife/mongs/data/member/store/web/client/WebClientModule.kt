package com.monglife.mongs.data.member.store.web.client

import android.content.Context
import com.google.gson.Gson
import com.mongs.wear.data.member.R
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
     * Store API Client Provider
     */
    @Provides
    @Singleton
    fun provideStoreWebClient(
        @ApplicationContext context: Context,
        gson: Gson,
        @Named("authorization") okHttpClient: OkHttpClient,
    ): StoreWebClient = Retrofit.Builder()
        .baseUrl(context.getString(R.string.mongs_gateway_api_url) + "user/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
        .create(StoreWebClient::class.java)
}