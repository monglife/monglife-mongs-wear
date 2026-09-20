package com.monglife.mongs.app.activity

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HiltWorkerFactoryEntryPoint {
        fun workerFactory(): HiltWorkerFactory
    }

    // getter 다. 초기화식(`= Configuration.Builder()...`)으로 두면 이 값이 **생성자에서** 평가되고,
    // EntryPoints.get(this) 가 attachBaseContext 보다 먼저 Hilt 컴포넌트를 만든다.
    // 그 시점엔 applicationContext 가 아직 null 이라, Context 리소스를 읽는 바인딩이 그래프에
    // 하나라도 생기는 순간 Application 생성자에서 NPE 가 난다. 지금은 eager 바인딩이 없어
    // 우연히 살아 있을 뿐이다.
    //
    // WorkManager 는 첫 getInstance() 때 이 값을 한 번만 읽으므로 미뤄도 손해가 없다.
    // (프로세스 시작이 빨라지지는 않는다 — Hilt_MainApplication.onCreate() 가 어차피 컴포넌트를 만든다.)
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(EntryPoints.get(this, HiltWorkerFactoryEntryPoint::class.java).workerFactory())
            .build()
}