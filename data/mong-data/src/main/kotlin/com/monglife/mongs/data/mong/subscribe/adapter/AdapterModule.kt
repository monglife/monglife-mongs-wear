package com.monglife.mongs.data.mong.subscribe.adapter

import com.monglife.mongs.application.mong.port.subscribe.ManagementSubscribePort
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
    abstract fun bindManagementSubscribePort(adapter: ManagementSubscribeAdapter): ManagementSubscribePort
}