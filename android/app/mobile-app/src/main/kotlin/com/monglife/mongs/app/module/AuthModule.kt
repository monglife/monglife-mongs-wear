package com.monglife.mongs.app.module

import android.content.Context
import com.monglife.core.auth.client.GoogleAuthClient
import com.monglife.core.auth.client.GoogleCredentialClient
import com.monglife.core.data.web.client.AuthApiVariant
import com.mongs.mobile.R
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
 * 폰은 제약이 없어 신형 API 를 쓴다.
 * 자세한 배경은 GoogleAuthClient KDoc 참고.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthClientModule {

    @Binds
    @Singleton
    abstract fun bindGoogleAuthClient(client: GoogleCredentialClient): GoogleAuthClient
}

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    /**
     * 이 앱이 쓸 인증 API 계약.
     *
     * 서버의 Credential 엔드포인트는 아직 없다. 생기기 전까지 이 앱의 서버 로그인은 실패한다.
     */
    @Provides
    @Singleton
    fun provideAuthApiVariant(): AuthApiVariant = AuthApiVariant.CREDENTIAL

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
