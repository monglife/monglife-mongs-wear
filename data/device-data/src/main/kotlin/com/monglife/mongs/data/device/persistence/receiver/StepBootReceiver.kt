package com.monglife.mongs.data.device.persistence.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.monglife.mongs.data.device.persistence.coordinator.StepCollectionCoordinator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * 재부팅 / 앱 교체 후 Health Services 재등록
 *
 * Passive 등록은 재부팅을 넘어 유지되지 않는다(공식 문서 명시). 앱 교체 후 유지되는지는 문서에
 * 언급이 없어 보장으로 삼지 않고 [Intent.ACTION_MY_PACKAGE_REPLACED] 도 함께 받는다.
 * 재등록은 멱등이라 불필요하게 한 번 더 불려도 비용이 없다.
 */
class StepBootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface StepBootReceiverEntryPoint {
        fun stepCollectionCoordinator(): StepCollectionCoordinator
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // onReceive 는 10초 제한이 있고 Health Services 등록은 그보다 오래 걸릴 수 있다.
                // 여기서는 워커만 걸고 즉시 반환한다.
                EntryPointAccessors
                    .fromApplication(context.applicationContext, StepBootReceiverEntryPoint::class.java)
                    .stepCollectionCoordinator()
                    .requestSynchronize()
            }
        }
    }
}
