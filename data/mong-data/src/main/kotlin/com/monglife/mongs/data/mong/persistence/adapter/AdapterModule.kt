package com.monglife.mongs.data.mong.persistence.adapter

import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
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
    abstract fun bindManagementPersistencePort(adapter: ManagementPersistenceAdapter): ManagementPersistencePort
}