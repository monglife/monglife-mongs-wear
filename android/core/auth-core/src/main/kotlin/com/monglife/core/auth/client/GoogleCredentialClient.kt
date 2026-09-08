package com.monglife.core.auth.client

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.monglife.core.auth.exception.GoogleLoginCanceledException
import com.monglife.core.auth.exception.GoogleLoginException
import com.monglife.core.auth.exception.NoGoogleAccountException
import com.monglife.core.auth.vo.GoogleAccountVo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * [GoogleAuthClient] 의 Credential Manager 구현 — mobile 앱 전용
 *
 * Wear 에서는 GMS 가 이 API 를 거부한다. 이유는 [GoogleAuthClient] KDoc 참고.
 *
 * 계정 선택 UI 를 띄우므로 Activity 가 필요한데 application 모듈은 순수 Kotlin 이라
 * 포트 시그니처에 Activity 를 넣을 수 없다. 그래서 GoogleBillingClient 와 같은 형태로
 * core 모듈에 두고 ViewModel 이 직접 호출한다.
 * Activity 는 메서드 파라미터로만 받고 필드에 담지 않는다.
 */
@Singleton
class GoogleCredentialClient @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named(GoogleAuthClient.GOOGLE_SERVER_CLIENT_ID) private val serverClientId: String,
) : GoogleAuthClient {

    private val credentialManager = CredentialManager.create(context)

    /**
     * 구글 로그인 흐름 시작
     *
     * GetSignInWithGoogleOption 은 "Sign in with Google" 버튼 클릭 전용 흐름이라
     * 항상 계정 선택 UI 를 띄운다. 자동 선택/복귀 사용자 분기가 없어 기존 UX 와 그대로 대응된다.
     */
    override suspend fun signIn(activity: Activity): GoogleAccountVo {

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(serverClientId).build())
            .build()

        val response = try {
            credentialManager.getCredential(context = activity, request = request)
        } catch (exception: GetCredentialCancellationException) {
            throw GoogleLoginCanceledException()
        } catch (exception: NoCredentialException) {
            throw NoGoogleAccountException()
        } catch (exception: GetCredentialException) {
            /**
             * legacy 의 DEVELOPER_ERROR(10) 에 해당하는 설정 오류도 여기로 들어온다.
             * Credential Manager 는 코드를 주지 않아 원인이 type/message 문자열에만 남는다.
             */
            throw GoogleLoginException(
                result = mapOf(
                    "type" to exception.type,
                    "message" to (exception.message ?: ""),
                )
            )
        }

        val credential = response.credential

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw GoogleLoginException(
                result = mapOf("reason" to "unexpected credential", "type" to credential.type)
            )
        }

        val idToken = runCatching { GoogleIdTokenCredential.createFrom(credential.data).idToken }
            .getOrElse { throw GoogleLoginException(result = mapOf("reason" to "credential parse failed")) }

        /**
         * GoogleIdTokenCredential.id 는 이메일이라 서버의 socialAccountId 로 쓸 수 없다.
         * 기존 사용자와 같은 식별자를 유지하려면 ID 토큰의 sub 를 써야 한다.
         *
         * legacy 쪽은 GoogleSignInAccount.id 가 곧 sub 라 이 파싱이 필요 없다.
         * 두 구현이 여기서만 비대칭인 이유다.
         */
        val payload = IdTokenPayload.of(idToken)
            ?: throw GoogleLoginException(result = mapOf("reason" to "idToken parse failed"))

        return GoogleAccountVo(
            googleAccountId = payload.sub,
            email = payload.email,
            displayName = payload.name,
            idToken = idToken,
        )
    }

    /**
     * 자격증명 선택 상태 초기화
     *
     * legacy 의 signOut() 자리를 대신한다. 다음 로그인에서 계정 선택 UI 가 다시 뜨게 한다.
     */
    override suspend fun clearState() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
