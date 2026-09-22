package com.monglife.mongs.data.member.player

import com.monglife.mongs.application.member.player.port.persistence.PlayerPersistencePort
import com.monglife.mongs.application.member.player.port.web.PlayerWebPort
import com.monglife.mongs.data.member.player.persistence.adapter.PlayerPersistenceAdapter
import com.monglife.mongs.data.member.player.web.adapter.PlayerWebAdapter
import com.monglife.mongs.data.member.player.web.client.PlayerWebClient
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

    @Binds
    @Singleton
    abstract fun bindPlayerPersistencePort(adapter: PlayerPersistenceAdapter): PlayerPersistencePort

    @Binds
    @Singleton
    abstract fun bindPlayerWebPort(adapter: PlayerWebAdapter): PlayerWebPort
}

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    @Provides
    @Singleton
    fun providePlayerWebClient(@Named("monglife-mongs") retrofit: Retrofit): PlayerWebClient =
        retrofit.create(PlayerWebClient::class.java)
}