package com.monglife.mongs.app.service

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.monglife.mongs.app.activity.MainActivity
import com.monglife.mongs.app.module.NotificationModule
import com.monglife.mongs.application.auth.usecase.SyncUserDeviceUseCase
import com.monglife.mongs.application.device.usecase.GetNotificationOptionUseCase
import com.mongs.wear.presentation.view.wear.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationService : FirebaseMessagingService() {

    @Inject lateinit var notificationManager: NotificationManager
    @Inject lateinit var syncUserDeviceUseCase: SyncUserDeviceUseCase
    @Inject lateinit var getNotificationOptionUseCase: GetNotificationOptionUseCase

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            syncUserDeviceUseCase(
                command = SyncUserDeviceUseCase.Command(
                    fcmToken = token,
                )
            )
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        CoroutineScope(Dispatchers.IO).launch {
            if (remoteMessage.data.isNotEmpty()) {
                if (getNotificationOptionUseCase()) {
                    sendNotification(
                        title = remoteMessage.data["title"].toString(),
                        body = remoteMessage.data["body"].toString(),
                    )
                }
            }
        }
    }

    @SuppressLint("WearRecents")
    private fun sendNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, NotificationModule.CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.icon_logo_not_open)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setOnlyAlertOnce(false)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        notificationManager.notify(0, notificationBuilder.build())
    }
}