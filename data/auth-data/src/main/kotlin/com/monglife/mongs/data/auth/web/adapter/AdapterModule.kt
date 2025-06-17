package com.monglife.mongs.data.auth.web.adapter

import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.mongs.application.auth.port.web.UserDeviceWebPort
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
    abstract fun bindAuthWebPort(adapter: AuthWebAdapter): AuthWebPort

    @Binds
    @Singleton
    abstract fun bindUserDeviceWebPort(adapter: UserDeviceWebAdapter): UserDeviceWebPort
}