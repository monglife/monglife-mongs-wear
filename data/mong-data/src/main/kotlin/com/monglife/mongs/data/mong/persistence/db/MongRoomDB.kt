package com.monglife.mongs.data.mong.persistence.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monglife.mongs.data.mong.persistence.dao.MongDao
import com.monglife.mongs.data.mong.persistence.entity.MongEntity

@Database(entities = [ MongEntity::class ], version = 1)
@TypeConverters(RoomConverters::class)
abstract class MongRoomDB : RoomDatabase() {

    /**
     * 몽 DAO
     */
    abstract fun mongDao(): MongDao
}