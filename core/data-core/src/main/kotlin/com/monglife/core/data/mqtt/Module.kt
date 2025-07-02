package com.monglife.core.data.mqtt

import android.content.Context
import com.mongs.wear.data.core.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.MqttClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MqttClientModule {

    @Provides
    @Singleton
    fun provideMqttClient(@ApplicationContext context: Context): MqttAndroidClient =
        MqttAndroidClient(
            context = context,
            serverURI = context.getString(R.string.mongs_mqtt_url),
            clientId = MqttClient.generateClientId()
        )
}
