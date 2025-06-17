package com.monglife.mongs.data.member.store.web.adapter

import com.monglife.mongs.application.member.store.port.web.StoreWebPort
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
    abstract fun bindStoreWebPort(adapter: StoreWebAdapter): StoreWebPort
}