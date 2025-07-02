package com.monglife.core.presentation

import android.content.Context
import com.monglife.core.presentation.utils.PermissionUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    /**
     * 권한 유틸 Provider
     */
    @Provides
    @Singleton
    fun providePermissionUtil(@ApplicationContext context: Context): PermissionUtil {
        return PermissionUtil(context)
    }
}