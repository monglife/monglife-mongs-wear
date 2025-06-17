package com.monglife.mongs.data.battle.web.adapter

import com.monglife.mongs.application.battle.port.web.MatchWebPort
import com.monglife.mongs.application.battle.port.web.QueueWebPort
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
    abstract fun bindMatchWebPort(adapter: MatchWebAdapter): MatchWebPort

    @Binds
    @Singleton
    abstract fun bindQueueWebPort(adapter: QueueWebAdapter): QueueWebPort
}