package com.monglife.mongs.data.battle.persistence.db

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.LocalTime

class RoomConverters {

    @TypeConverter
    fun strToLocalDateTime(str: String): LocalDateTime {
        return str.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun localDateTimeToStr(date: LocalDateTime): String {
        return date.toString()
    }

    @TypeConverter
    fun strToLocalTime(str: String): LocalTime {
        return str.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun localTimeToStr(time: LocalTime): String {
        return time.toString()
    }
}