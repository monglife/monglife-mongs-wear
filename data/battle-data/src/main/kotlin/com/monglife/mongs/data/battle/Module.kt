package com.monglife.mongs.data.battle

import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.persistence.MatchQueuePersistencePort
import com.monglife.mongs.application.battle.port.web.MatchWebPort
import com.monglife.mongs.application.battle.port.web.MatchQueueWebPort
import com.monglife.mongs.data.battle.persistence.adapter.MatchPersistenceAdapter
import com.monglife.mongs.data.battle.persistence.adapter.MatchQueuePersistenceAdapter
import com.monglife.mongs.data.battle.web.adapter.MatchWebAdapter
import com.monglife.mongs.data.battle.web.adapter.MatchQueueWebAdapter
import com.monglife.mongs.data.battle.web.client.MatchWebClient
import com.monglife.mongs.data.battle.web.client.MatchQueueWebClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class AdapterModule {

    /**
     * Bind MatchWebPort for BattleApplication
     */
    @Binds
    @Singleton
    abstract fun bindMatchWebPort(adapter: MatchWebAdapter): MatchWebPort

    /**
     * Bind QueueWebPort for BattleApplication
     */
    @Binds
    @Singleton
    abstract fun bindQueueWebPort(adapter: MatchQueueWebAdapter): MatchQueueWebPort

    /**
     * Bind MatchPersistencePort for BattleApplication
     */
    @Binds
    @Singleton
    abstract fun bindMatchPersistencePort(adapter: MatchPersistenceAdapter): MatchPersistencePort

    /**
     * Bind bindMatchQueuePersistencePort for BattleApplication
     */
    @Binds
    @Singleton
    abstract fun bindMatchQueuePersistencePort(adapter: MatchQueuePersistenceAdapter): MatchQueuePersistencePort
}

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    @Provides
    @Singleton
    fun provideQueueWebClient(@Named("monglife-mongs") retrofit: Retrofit): MatchQueueWebClient =
        retrofit.create(MatchQueueWebClient::class.java)

    @Provides
    @Singleton
    fun provideMatchWebClient(@Named("monglife-mongs") retrofit: Retrofit): MatchWebClient =
        retrofit.create(MatchWebClient::class.java)
}