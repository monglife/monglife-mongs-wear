package com.monglife.mongs.data.device.web.adapter

import com.monglife.mongs.application.device.port.web.DeviceWebPort
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdapterModule {

    @Binds
    @Singleton
    abstract fun bindDeviceWebPort(adapter: DeviceWebAdapter): DeviceWebPort
}