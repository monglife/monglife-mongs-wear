package com.monglife.mongs.data.member.player.subscribe.adapter

import com.monglife.mongs.application.member.player.port.subscribe.PlayerSubscribePort
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
    abstract fun bindPlayerSubscribePort(adapter: PlayerSubscribeAdapter): PlayerSubscribePort
}