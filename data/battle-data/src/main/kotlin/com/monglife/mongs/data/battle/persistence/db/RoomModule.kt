package com.monglife.mongs.data.battle.persistence.db

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

    private const val ROOM_NAME = "MONGS-BATTLE-DATABASE"

    @Provides
    @Singleton
    fun provideBattleRoomDB(@ApplicationContext context: Context): BattleRoomDB =
        Room.databaseBuilder(context.applicationContext, BattleRoomDB::class.java, ROOM_NAME)
            .fallbackToDestructiveMigration()
            .build()
}