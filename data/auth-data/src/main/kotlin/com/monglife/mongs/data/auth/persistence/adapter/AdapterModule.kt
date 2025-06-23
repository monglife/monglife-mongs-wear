package com.monglife.mongs.data.auth.persistence.adapter

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdapterModule {

    /**
     * Bind AuthPersistencePort for AuthApplication
     */
    @Binds
    @Singleton
    abstract fun bindAuthPersistencePortForAuthApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort

    /**
     * Bind AuthPersistencePort for DeviceApplication
     */
    @Binds
    @Singleton
    abstract fun bindAuthPersistencePortForDeviceApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.device.port.persistence.AuthPersistencePort

    /**
     * Bind AuthPersistencePort for MemberApplication
     */
    @Binds
    @Singleton
    abstract fun bindAuthPersistencePortForPlayerApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.member.player.port.persistence.AuthPersistencePort
}