package com.monglife.mongs.data.auth.persistence.adapter

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
    abstract fun bindAuthPersistencePortForAuthApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort

    @Binds
    @Singleton
    abstract fun bindAuthPersistencePortForDeviceApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.device.port.persistence.AuthPersistencePort

    @Binds
    @Singleton
    abstract fun bindAuthPersistencePortForPlayerApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.member.player.port.persistence.AuthPersistencePort

    @Binds
    @Singleton
    abstract fun bindAuthPersistencePortForMongApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.mong.port.persistence.AuthPersistencePort
}