package com.monglife.mongs.data.battle.persistence.adapter

import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdapterModule {

    /**
     * Bind MatchPersistencePort for BattleApplication
     */
    @Binds
    @Singleton
    abstract fun bindMatchPersistencePort(adapter: MatchPersistenceAdapter): MatchPersistencePort
}