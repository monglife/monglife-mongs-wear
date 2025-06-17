package com.monglife.mongs.data.device.web.client

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object WebClientModule {


//    /**
//     * Auth API Client Provider
//     */
//    @Provides
//    @Singleton
//    fun provideAuthWebClient(@ApplicationContext context: Context, gson: Gson, httpLogInterceptor: HttpLogInterceptor): AuthWebClient {
//
//        val okHttpClient = OkHttpClient.Builder()
//            .connectTimeout(context.getString(R.string.api_connect_time_out).toLong(), TimeUnit.SECONDS)
//            .readTimeout(context.getString(R.string.api_read_time_out).toLong(), TimeUnit.SECONDS)
//            .writeTimeout(context.getString(R.string.api_write_time_out).toLong(), TimeUnit.SECONDS)
//            .addInterceptor(httpLogInterceptor)
//            .build()
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl(context.getString(R.string.api_url))
//            .addConverterFactory(GsonConverterFactory.create(gson))
//            .client(okHttpClient)
//            .build()
//
//        return retrofit.create(AuthWebClient::class.java)
//    }
}