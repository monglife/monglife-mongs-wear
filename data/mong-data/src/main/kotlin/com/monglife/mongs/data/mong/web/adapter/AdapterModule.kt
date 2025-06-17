package com.monglife.mongs.data.mong.web.adapter

import com.monglife.mongs.application.mong.port.web.ActivityWebPort
import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
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
    abstract fun bindActivityWebPort(adapter: ActivityWebAdapter): ActivityWebPort

    @Binds
    @Singleton
    abstract fun bindInteractionWebPort(adapter: InteractionWebAdapter): InteractionWebPort

    @Binds
    @Singleton
    abstract fun bindManagementWebPort(adapter: ManagementWebAdapter): ManagementWebPort
}