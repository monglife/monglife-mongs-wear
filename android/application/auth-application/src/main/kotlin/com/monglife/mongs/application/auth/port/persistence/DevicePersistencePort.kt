package com.monglife.mongs.application.auth.port.persistence

interface DevicePersistencePort {

    /**
     * 기기 ID 조회
     */
    suspend fun getDeviceId(): String

    /**
     * 기기명 조회
     */
    suspend fun getDeviceName(): String

    /**
     * 앱 패키지명 조회
     */
    suspend fun getAppPackageName(): String

    /**
     * 앱 버전 조회
     */
    suspend fun getBuildVersion(): String

    /**
     * FCM 토큰 조회
     */
    suspend fun getFcmToken(): String
}