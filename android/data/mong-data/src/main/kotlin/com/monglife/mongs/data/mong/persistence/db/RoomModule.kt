package com.monglife.mongs.data.mong.persistence.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    private const val ROOM_NAME = "MONGS-MONG-DATABASE"

    @Provides
    @Singleton
    fun provideMongRoomDB(@ApplicationContext context: Context): MongRoomDB =
        Room.databaseBuilder(context.applicationContext, MongRoomDB::class.java, ROOM_NAME)
            .fallbackToDestructiveMigration(true)
            .build()
}