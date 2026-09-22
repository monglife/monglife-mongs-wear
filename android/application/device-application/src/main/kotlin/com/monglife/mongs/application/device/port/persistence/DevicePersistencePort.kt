package com.monglife.mongs.application.device.port.persistence

import com.monglife.mongs.domain.device.model.DeviceOption
import com.monglife.mongs.domain.device.model.Step
import kotlinx.coroutines.flow.Flow

interface DevicePersistencePort {

    /**
     * 걸음 수 수집 시작
     */
    suspend fun startStepCollection()

    /**
     * 걸음 수 조회
     */
    suspend fun getStep(): Step

    /**
     * 걸음 수 Flow 조회
     */
    suspend fun getStepFlow(): Flow<Step>

    /**
     * 걸음 수 차감 (환전)
     */
    suspend fun consumeWalkingCount(walkingCount: Int): Step

    /**
     * 기기 옵션 조회
     */
    suspend fun getDeviceOption(): DeviceOption

    /**
     * 기기 옵션 Flow 조회
     */
    suspend fun getDeviceOptionFlow(): Flow<DeviceOption>

    /**
     * 기기 옵션 저장
     */
    suspend fun saveDeviceOption(deviceOption: DeviceOption): DeviceOption
}
