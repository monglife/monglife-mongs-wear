package com.monglife.core.data.web

import com.monglife.core.data.web.client.AuthWebClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    @Provides
    @Singleton
    fun provideAuthWebClient(
        @Named("monglife-discovery") retrofit: Retrofit
    ): AuthWebClient = retrofit.create(AuthWebClient::class.java)
}