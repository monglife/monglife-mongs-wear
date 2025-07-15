package com.monglife.mongs.presentation.view.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal object TimeUtil {

    /**
     * LocalDateTime to String
     */
    fun localDateTimeToString(localDateTime: LocalDateTime): String =
        localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
}