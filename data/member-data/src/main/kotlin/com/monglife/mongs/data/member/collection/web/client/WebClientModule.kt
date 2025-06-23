package com.monglife.mongs.data.member.collection.web.client

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
     * Collection API Client Provider
     */
    @Provides
    @Singleton
    fun provideCollectionWebClient(
        @ApplicationContext context: Context,
        gson: Gson,
        @Named("authorization") okHttpClient: OkHttpClient,
    ): CollectionWebClient = Retrofit.Builder()
        .baseUrl(context.getString(R.string.mongs_gateway_api_url) + "user/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
        .create(CollectionWebClient::class.java)
}