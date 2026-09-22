package com.monglife.mongs.data.member.collection

import com.monglife.mongs.application.member.collection.port.web.CollectionWebPort
import com.monglife.mongs.data.member.collection.web.adapter.CollectionWebAdapter
import com.monglife.mongs.data.member.collection.web.client.CollectionWebClient
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
    abstract fun bindCollectionWebPort(adapter: CollectionWebAdapter): CollectionWebPort
}

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    @Provides
    @Singleton
    fun provideCollectionWebClient(
        @Named("monglife-mongs") retrofit: Retrofit
    ): CollectionWebClient = retrofit.create(CollectionWebClient::class.java)
}