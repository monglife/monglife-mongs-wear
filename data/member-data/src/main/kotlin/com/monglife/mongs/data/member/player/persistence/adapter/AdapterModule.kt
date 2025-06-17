package com.monglife.mongs.data.member.player.persistence.adapter

import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
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
    abstract fun bindPlayerPersistencePort(adapter: PlayerPersistenceAdapter): PlayerPersistencePort
}