package com.monglife.mongs.data.member.feedback

import com.monglife.mongs.application.member.feedback.port.web.FeedbackWebPort
import com.monglife.mongs.data.member.feedback.web.adapter.FeedbackWebAdapter
import com.monglife.mongs.data.member.feedback.web.client.FeedbackWebClient
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
    abstract fun bindFeedbackWebPort(adapter: FeedbackWebAdapter): FeedbackWebPort
}

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    @Provides
    @Singleton
    fun provideFeedbackWebClient(
        @Named("monglife-mongs") retrofit: Retrofit
    ): FeedbackWebClient = retrofit.create(FeedbackWebClient::class.java)
}