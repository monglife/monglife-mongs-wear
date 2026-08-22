package com.monglife.core.auth.client

import android.app.Activity
import com.monglife.core.auth.vo.GoogleAccountVo

/**
 * 구글 로그인 클라이언트
 *
 * 구현이 앱마다 다르다. app 모듈의 Hilt 바인딩이 어느 것을 쓸지 정한다.
 *
 *  - mobile : [GoogleCredentialClient]      (Credential Manager)
 *  - wear   : [GoogleLegacySignInClient]    (legacy GoogleSignIn)
 *
 * Wear 가 legacy 로 남는 이유는 코드만 봐서는 보이지 않는다.
 * Credential Manager 의 Sign in with Google 은 Wear OS 5.1+ 일부 기종만 지원하고,
 * 그 아래에서는 GMS 가 요청을 받자마자 거부한다.
 *
 *   Auth.Api.Credentials: [GetGoogleIdOperation] Operation failed.
 *   bfse: Google Identity Services do not support this
 *         Android Credential Manager API on Wear OS.
 *
 * 이 앱의 minSdk 30 은 Wear OS 3 이라 상당수 기기가 지원 범위 밖이다.
 * Wear OS 5.1+ 가 보급되면 wear 도 바인딩만 바꿔 넘어갈 수 있다.
 *
 * 두 구현 모두 서버에 보낼 idToken 을 채워 준다.
 * 로그인 요청이 어느 경로로 왔든 서버가 검증할 수 있는 형태는 같다.
 */
interface GoogleAuthClient {

    /**
     * 구글 로그인 흐름을 시작하고 결과를 돌려준다.
     *
     * 계정 선택 UI 를 띄우므로 Activity 가 필요하다.
     * 보관하지 않고 호출 동안만 쓴다.
     */
    suspend fun signIn(activity: Activity): GoogleAccountVo

    /**
     * 자격증명 선택 상태를 초기화한다. 다음 로그인에서 계정 선택 UI 가 다시 뜨게 한다.
     *
     * 실패해도 던지지 않는다. 이 호출이 막히면 로그인 화면 자체가 못 뜬다.
     */
    suspend fun clearState()

    companion object {
        // app 모듈이 google-services.json 에서 생성된 default_web_client_id 를 이 이름으로 제공한다.
        const val GOOGLE_SERVER_CLIENT_ID = "google-server-client-id"
    }
}
