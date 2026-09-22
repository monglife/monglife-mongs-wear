package com.monglife.mongs.application.member.feedback.port.persistence

interface DevicePersistencePort {

    /**
     * 기기명 조회
     */
    suspend fun getDeviceName(): String
}