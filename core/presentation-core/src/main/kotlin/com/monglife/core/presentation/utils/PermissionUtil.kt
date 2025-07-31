package com.monglife.core.presentation.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class PermissionUtil (
    private val context: Context
) {
    /**
     * 알림 권한 체크
     */
    fun verifyNotificationPermission() : ArrayList<String> {
        val permissions = ArrayList<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        return permissions
    }

    /**
     * 활동 권한 체크
     */
    fun verifyActivityPermission() : ArrayList<String> {
        val permissions = ArrayList<String>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        return permissions
    }

    /**
     * 위치 권한 체크
     */
    fun verifyLocationPermission() : ArrayList<String> {
        val permissions = ArrayList<String>()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        return permissions
    }
}