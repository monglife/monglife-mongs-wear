package com.monglife.mongs.presentation.view.utils

import android.annotation.SuppressLint

internal object NumberUtil {

    @SuppressLint("DefaultLocale")
    fun formatAsCurrency(value: Int): String {
        return String.format("%,d", value)
    }

    @SuppressLint("DefaultLocale")
    fun formatAsCurrency(value: Long): String {
        return String.format("%,d", value)
    }

    @SuppressLint("DefaultLocale")
    fun formatAsCurrency(value: Double): String {
        return String.format("%,.2f", value)
    }
}