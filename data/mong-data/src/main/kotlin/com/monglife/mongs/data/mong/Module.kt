package com.monglife.mongs.data.mong

import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.ActivityWebPort
import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.data.mong.persistence.adapter.ManagementPersistenceAdapter
import com.monglife.mongs.data.mong.web.adapter.ActivityWebAdapter
import com.monglife.mongs.data.mong.web.adapter.InteractionWebAdapter
import com.monglife.mongs.data.mong.web.adapter.ManagementWebAdapter
import com.monglife.mongs.data.mong.web.client.ActivityWebClient
import com.monglife.mongs.data.mong.web.client.InteractionWebClient
import com.monglife.mongs.data.mong.web.client.ManagementWebClient
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
    abstract fun bindManagementPersistencePort(adapter: ManagementPersistenceAdapter): ManagementPersistencePort

    @Binds
    @Singleton
    abstract fun bindActivityWebPort(adapter: ActivityWebAdapter): ActivityWebPort

    @Binds
    @Singleton
    abstract fun bindInteractionWebPort(adapter: InteractionWebAdapter): InteractionWebPort

    @Binds
    @Singleton
    abstract fun bindManagementWebPort(adapter: ManagementWebAdapter): ManagementWebPort
}

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    @Provides
    @Singleton
    fun provideActivityWebClient(@Named("monglife-mongs") retrofit: Retrofit): ActivityWebClient =
        retrofit.create(ActivityWebClient::class.java)

    @Provides
    @Singleton
    fun provideInteractionWebClient(@Named("monglife-mongs") retrofit: Retrofit): InteractionWebClient =
        retrofit.create(InteractionWebClient::class.java)

    @Provides
    @Singleton
    fun provideManagementWebClient(@Named("monglife-mongs") retrofit: Retrofit): ManagementWebClient =
        retrofit.create(ManagementWebClient::class.java)
}
