package com.monglife.mongs.data.member.notice

import com.monglife.mongs.application.member.notice.port.web.NoticeWebPort
import com.monglife.mongs.data.member.notice.web.adapter.NoticeWebAdapter
import com.monglife.mongs.data.member.notice.web.client.NoticeWebClient
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
    abstract fun bindNoticeWebPort(adapter: NoticeWebAdapter): NoticeWebPort
}

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    @Provides
    @Singleton
    fun provideNoticeWebClient(
        @Named("monglife-mongs") retrofit: Retrofit
    ): NoticeWebClient = retrofit.create(NoticeWebClient::class.java)
}