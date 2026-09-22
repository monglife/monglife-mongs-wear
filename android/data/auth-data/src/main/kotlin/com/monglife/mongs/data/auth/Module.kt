package com.monglife.mongs.data.auth

import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.mongs.application.auth.port.web.UserDeviceWebPort
import com.monglife.mongs.data.auth.persistence.adapter.AuthPersistenceAdapter
import com.monglife.mongs.data.auth.web.adapter.AuthWebAdapter
import com.monglife.mongs.data.auth.web.adapter.UserDeviceWebAdapter
import com.monglife.mongs.data.auth.web.client.UserDeviceWebClient
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
    abstract fun bindAuthPersistencePortForAuthApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort

    @Binds
    @Singleton
    abstract fun bindAuthPersistencePortForDeviceApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.device.port.persistence.AuthPersistencePort

    @Binds
    @Singleton
    abstract fun bindAuthPersistencePortForPlayerApplication(adapter: AuthPersistenceAdapter): com.monglife.mongs.application.member.player.port.persistence.AuthPersistencePort

    @Binds
    @Singleton
    abstract fun bindUserDeviceWebPort(adapter: UserDeviceWebAdapter): UserDeviceWebPort

    @Binds
    @Singleton
    abstract fun bindAuthWebPort(adapter: AuthWebAdapter): AuthWebPort
}

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    @Provides
    @Singleton
    fun provideUserDeviceWebClient(
        @Named("monglife-discovery") retrofit: Retrofit
    ): UserDeviceWebClient = retrofit.create(UserDeviceWebClient::class.java)
}