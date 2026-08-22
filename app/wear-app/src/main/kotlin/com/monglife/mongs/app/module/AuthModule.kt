package com.monglife.mongs.app.module

import android.content.Context
import com.monglife.core.auth.client.GoogleAuthClient
import com.monglife.core.auth.client.GoogleLegacySignInClient
import com.monglife.core.data.web.client.AuthApiVariant
import com.mongs.wear.R
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * 이 앱이 쓸 구글 로그인 구현을 고른다.
 *
 * Wear OS 는 Credential Manager 의 Sign in with Google 을 GMS 가 거부한다.
 * 자세한 배경은 GoogleAuthClient KDoc 참고.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthClientModule {

    @Binds
    @Singleton
    abstract fun bindGoogleAuthClient(client: GoogleLegacySignInClient): GoogleAuthClient
}

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    /**
     * 이 앱이 쓸 인증 API 계약.
     *
     * 서버에 Credential 엔드포인트가 생겨도 wear 는 계속 기존 계약을 쓴다.
     * 이미 배포된 앱이고, legacy 도 requestIdToken 으로 토큰을 받아두므로 나중에 옮길 수 있다.
     */
    @Provides
    @Singleton
    fun provideAuthApiVariant(): AuthApiVariant = AuthApiVariant.LEGACY

    /**
     * 구글 로그인의 웹 클라이언트 ID
     *
     * google-services 플러그인이 configs 의 google-services.json 에서 생성하는 리소스다.
     * app 모듈에만 생성되므로 core:auth-core 가 직접 읽을 수 없어 여기서 주입한다.
     * 값의 출처가 json 하나뿐이라 콘솔에서 재다운로드하면 그대로 반영된다.
     */
    @Provides
    @Singleton
    @Named(GoogleAuthClient.GOOGLE_SERVER_CLIENT_ID)
    fun provideGoogleServerClientId(@ApplicationContext context: Context): String {
        return context.getString(R.string.default_web_client_id)
    }
}
