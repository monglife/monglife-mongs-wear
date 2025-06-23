package com.monglife.mongs.data.mong.persistence.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monglife.mongs.data.mong.persistence.dao.MongDao
import com.monglife.mongs.data.mong.persistence.entity.MongEntity
import com.monglife.mongs.data.mong.persistence.entity.MongOptionEntity

@Database(entities = [ MongEntity::class, MongOptionEntity::class ], version = 3)
@TypeConverters(RoomConverters::class)
abstract class MongRoomDB : RoomDatabase() {

    /**
     * 몽 Dao
     */
    abstract fun mongDao(): MongDao
}