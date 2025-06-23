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

    /**
     * Bind ActivityWebPort for MongApplication
     */
    @Binds
    @Singleton
    abstract fun bindActivityWebPort(adapter: ActivityWebAdapter): ActivityWebPort

    /**
     * Bind InteractionWebPort for MongApplication
     */
    @Binds
    @Singleton
    abstract fun bindInteractionWebPort(adapter: InteractionWebAdapter): InteractionWebPort

    /**
     * Bind ManagementWebPort for MongApplication
     */
    @Binds
    @Singleton
    abstract fun bindManagementWebPort(adapter: ManagementWebAdapter): ManagementWebPort
}