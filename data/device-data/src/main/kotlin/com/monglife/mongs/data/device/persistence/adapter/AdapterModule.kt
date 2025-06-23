package com.monglife.mongs.data.device.persistence.adapter

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdapterModule {

    /**
     * Bind DevicePersistencePort for AuthApplication
     */
    @Binds
    @Singleton
    abstract fun bindDevicePersistencePortForAuthApplication(adapter: DevicePersistenceAdapter): com.monglife.mongs.application.auth.port.persistence.DevicePersistencePort

    /**
     * Bind DevicePersistencePort for BattleApplication
     */
    @Binds
    @Singleton
    abstract fun bindDevicePersistencePortForBattleApplication(adapter: DevicePersistenceAdapter): com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort

    /**
     * Bind DevicePersistencePort for DeviceApplication
     */
    @Binds
    @Singleton
    abstract fun bindDevicePersistencePortForDeviceApplication(adapter: DevicePersistenceAdapter): com.monglife.mongs.application.device.port.persistence.DevicePersistencePort

    /**
     * Bind DevicePersistencePort for MemberApplication
     */
    @Binds
    @Singleton
    abstract fun bindDevicePersistencePortForMemberApplication(adapter: DevicePersistenceAdapter): com.monglife.mongs.application.member.feedback.port.persistence.DevicePersistencePort
}