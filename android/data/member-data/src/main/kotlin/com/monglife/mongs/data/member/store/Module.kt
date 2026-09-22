package com.monglife.mongs.data.member.store

import com.monglife.mongs.application.member.store.port.web.StoreWebPort
import com.monglife.mongs.data.member.store.web.adapter.StoreWebAdapter
import com.monglife.mongs.data.member.store.web.client.StoreWebClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdapterModule {

    @Binds
    @Singleton
    abstract fun bindStoreWebPort(adapter: StoreWebAdapter): StoreWebPort
}

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    @Provides
    @Singleton
    fun provideStoreWebClient(@Named("monglife-mongs") retrofit: Retrofit): StoreWebClient =
        retrofit.create(StoreWebClient::class.java)
}