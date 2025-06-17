package com.monglife.mongs.data.member.collection.web.adapter

import com.monglife.mongs.application.member.collection.port.web.CollectionWebPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdapterModule {

    @Binds
    @Singleton
    abstract fun bindCollectionWebPort(adapter: CollectionWebAdapter): CollectionWebPort
}