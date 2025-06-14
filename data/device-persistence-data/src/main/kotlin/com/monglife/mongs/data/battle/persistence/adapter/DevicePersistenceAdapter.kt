package com.monglife.mongs.data.battle.persistence.adapter

import com.monglife.mongs.application.device.port.persistence.DevicePersistencePort
import com.monglife.mongs.domain.device.model.DeviceOption
import com.monglife.mongs.domain.device.model.Step
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DevicePersistenceAdapter @Inject constructor(

) : DevicePersistencePort {

    override suspend fun getStep(): Step {
        TODO("Not yet implemented")
    }

    override suspend fun getStepFlow(): Flow<Step> {
        TODO("Not yet implemented")
    }

    override suspend fun saveStep(step: Step): Step {
        TODO("Not yet implemented")
    }

    override suspend fun getDeviceOption(): DeviceOption {
        TODO("Not yet implemented")
    }

    override suspend fun getDeviceOptionFlow(): Flow<DeviceOption> {
        TODO("Not yet implemented")
    }

    override suspend fun saveDeviceOption(deviceOption: DeviceOption): DeviceOption {
        TODO("Not yet implemented")
    }
}