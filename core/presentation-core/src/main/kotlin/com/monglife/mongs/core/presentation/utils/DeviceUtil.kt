package com.monglife.mongs.core.presentation.utils

import android.os.SystemClock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DeviceUtil {

    private val DATE_FORMATER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

    /**
     * 기기 부팅 시간 조회
     */
    fun getBootedDt(): LocalDateTime {

        val uptimeMillis = System.currentTimeMillis() - SystemClock.elapsedRealtime()

        val deviceBootedDt = Instant.ofEpochMilli(uptimeMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()

        return LocalDateTime.parse(deviceBootedDt.format(DATE_FORMATER), DATE_FORMATER)
    }

    /**
     * 기기 고유 ID 조회
     */
    fun getDeviceId(): String {
        return ""
    }
}