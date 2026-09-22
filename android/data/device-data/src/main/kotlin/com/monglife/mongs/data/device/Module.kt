package com.monglife.mongs.data.device

import android.content.Context
import androidx.work.WorkManager
import com.monglife.mongs.application.device.port.web.DeviceWebPort
import com.monglife.mongs.data.device.persistence.adapter.DevicePersistenceAdapter
import com.monglife.mongs.data.device.web.adapter.DeviceWebAdapter
import com.monglife.mongs.data.device.web.client.DeviceWebClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdapterModule {

    @Binds
    @Singleton
    abstract fun bindDevicePersistencePortForAuthApplication(adapter: DevicePersistenceAdapter): com.monglife.mongs.application.auth.port.persistence.DevicePersistencePort

    @Binds
    @Singleton
    abstract fun bindDevicePersistencePortForBattleApplication(adapter: DevicePersistenceAdapter): com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort

    @Binds
    @Singleton
    abstract fun bindDevicePersistencePortForDeviceApplication(adapter: DevicePersistenceAdapter): com.monglife.mongs.application.device.port.persistence.DevicePersistencePort

    @Binds
    @Singleton
    abstract fun bindDevicePersistencePortForMemberApplication(adapter: DevicePersistenceAdapter): com.monglife.mongs.application.member.feedback.port.persistence.DevicePersistencePort

    @Binds
    @Singleton
    abstract fun bindDevicePersistencePortForMongApplication(adapter: DevicePersistenceAdapter): com.monglife.mongs.application.mong.port.persistence.DevicePersistencePort

    @Binds
    @Singleton
    abstract fun bindDeviceWebPort(adapter: DeviceWebAdapter): DeviceWebPort

}

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {

    @Provides
    @Singleton
    fun provideDeviceWebClient(@Named("monglife-mongs") retrofit: Retrofit): DeviceWebClient =
        retrofit.create(DeviceWebClient::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context) : WorkManager {
        return WorkManager.getInstance(context)
    }
}