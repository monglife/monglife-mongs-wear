package com.monglife.mongs.data.battle.publish.adapter

import com.monglife.mongs.application.battle.port.publish.MatchPublishPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdapterModule {

    /**
     * Bind MatchPublishPort for BattleApplication
     */
    @Binds
    @Singleton
    abstract fun bindMatchPublishPort(adapter: MatchPublishAdapter): MatchPublishPort
}