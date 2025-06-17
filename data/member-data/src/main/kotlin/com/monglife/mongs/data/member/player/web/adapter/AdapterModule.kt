package com.monglife.mongs.data.member.player.web.adapter

import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
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
    abstract fun bindPlayerWebPort(adapter: PlayerWebAdapter): PlayerWebPort
}