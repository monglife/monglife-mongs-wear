package com.monglife.mongs.data.battle.subscribe.adapter

import com.monglife.mongs.application.battle.port.subscribe.MatchSubscribePort
import com.monglife.mongs.application.battle.port.subscribe.QueueSubscribePort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdapterModule {

    /**
     * Bind MatchSubscribePort for BattleApplication
     */
    @Binds
    @Singleton
    abstract fun bindMatchSubscribePort(adapter: MatchSubscribeAdapter): MatchSubscribePort

    /**
     * Bind QueueSubscribePort for BattleApplication
     */
    @Binds
    @Singleton
    abstract fun bindQueueSubscribePort(adapter: QueueSubscribeAdapter): QueueSubscribePort
}