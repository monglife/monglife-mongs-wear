package com.monglife.mongs.data.member.collection.web.manager

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationSensorManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * GPS 조회
     */
    @SuppressLint("MissingPermission")
    suspend fun getLocation(): LocationVo? {
        // 이전 위치 정보 삭제
        mFusedLocationClient.flushLocations()

        // 조회 및 반환
        return mFusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).await().let { location: Location? ->
            location?.let {
                LocationVo(
                    latitude = location.latitude,
                    longitude = location.longitude,
                )
            }
        }
    }

    /**
     * 위치 정보 Vo
     */
    data class LocationVo(
        val latitude: Double,
        val longitude: Double,
    )
}