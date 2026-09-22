package com.monglife.mongs.application.battle.port.persistence

interface DevicePersistencePort {

    /**
     * 기기 ID 조회
     */
    suspend fun getDeviceId(): String
}