package com.monglife.mongs.application.device.port.persistence

import com.monglife.mongs.application.device.exception.NotFoundDeviceOptionException
import com.monglife.mongs.domain.device.model.DeviceOption
import com.monglife.mongs.domain.device.model.Step
import kotlinx.coroutines.flow.Flow

interface DevicePersistencePort {

    /**
     * 걸음 수 조회
     */
    suspend fun getStep(): Step

    /**
     * 걸음 수 라이브 객체 조회
     */
    suspend fun getStepFlow(): Flow<Step>

    /**
     * 걸음 수 로컬 동기화
     */
    suspend fun saveStep(step: Step): Step

    /**
     * 기기 옵션 조회
     */
    @Throws(NotFoundDeviceOptionException::class)
    suspend fun getDeviceOption(): DeviceOption

    /**
     * 기기 옵션 라이브 객체 조회
     */
    @Throws(NotFoundDeviceOptionException::class)
    suspend fun getDeviceOptionFlow(): Flow<DeviceOption>

    /**
     * 기기 옵션 수정
     */
    suspend fun saveDeviceOption(deviceOption: DeviceOption): DeviceOption
}