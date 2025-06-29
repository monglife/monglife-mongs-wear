package com.monglife.mongs.data.core.web.module

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.monglife.mongs.data.core.web.adapter.GsonLocalDateTimeFormatAdapter
import com.monglife.mongs.data.core.web.adapter.GsonLocalTimeAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GsonModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, GsonLocalDateTimeFormatAdapter())
        .registerTypeAdapter(LocalTime::class.java, GsonLocalTimeAdapter())
        .create()
}