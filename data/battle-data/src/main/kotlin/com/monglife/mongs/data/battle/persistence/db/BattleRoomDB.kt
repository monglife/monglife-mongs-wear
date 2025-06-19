package com.monglife.mongs.data.battle.persistence.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monglife.mongs.data.battle.persistence.dao.MatchDao
import com.monglife.mongs.data.battle.persistence.dao.MatchPlayerDao
import com.monglife.mongs.data.battle.persistence.entity.MatchEntity
import com.monglife.mongs.data.battle.persistence.entity.MatchPlayerEntity

@Database(entities = [ MatchEntity::class, MatchPlayerEntity::class ], version = 1)
@TypeConverters(RoomConverters::class)
abstract class BattleRoomDB : RoomDatabase() {

    /**
     * 매치 DAO
     */
    abstract fun matchDao(): MatchDao

    /**
     * 매치 플레이어 DAO
     */
    abstract fun matchPlayerDao(): MatchPlayerDao
}