package com.monglife.mongs.data.member.feedback.web.adapter

import com.monglife.mongs.application.member.feedback.port.web.FeedbackWebPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdapterModule {

    @Binds
    @Singleton
    abstract fun bindFeedbackWebPort(adapter: FeedbackWebAdapter): FeedbackWebPort
}