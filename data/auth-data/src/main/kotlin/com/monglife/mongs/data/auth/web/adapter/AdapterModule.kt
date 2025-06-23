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

    /**
     * Bind AuthWebPort for AuthApplication
     */
    @Binds
    @Singleton
    abstract fun bindAuthWebPort(adapter: AuthWebAdapter): AuthWebPort

    /**
     * Bind UserDeviceWebPort for AuthApplication
     */
    @Binds
    @Singleton
    abstract fun bindUserDeviceWebPort(adapter: UserDeviceWebAdapter): UserDeviceWebPort
}