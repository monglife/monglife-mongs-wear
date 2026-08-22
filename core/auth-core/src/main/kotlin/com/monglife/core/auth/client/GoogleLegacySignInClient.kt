package com.monglife.core.auth.client

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.monglife.core.auth.exception.GoogleLoginCanceledException
import com.monglife.core.auth.exception.GoogleLoginException
import com.monglife.core.auth.vo.GoogleAccountVo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * [GoogleAuthClient] 의 legacy GoogleSignIn 구현 — wear 앱 전용
 *
 * Credential Manager 를 쓸 수 없는 이유는 [GoogleAuthClient] KDoc 참고.
 *
 * legacy 는 signInIntent 를 띄우고 ActivityResult 로 결과를 받는 구조라 원래는 호출부가
 * ActivityResultLauncher 를 들고 있어야 했다. 그 왕복을 ActivityResultRegistry 로 이 안에 가둬
 * suspend 한 번으로 끝나게 만든다. 덕분에 ViewModel 과 화면은 두 구현의 차이를 모른다.
 *
 * requestIdToken 으로 ID 토큰을 함께 받으므로 서버로 보내는 값은 Credential Manager 경로와 같다.
 * GoogleSignInAccount.id 가 곧 ID 토큰의 sub 라 [IdTokenPayload] 파싱은 필요 없다.
 */
@Singleton
class GoogleLegacySignInClient @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named(GoogleAuthClient.GOOGLE_SERVER_CLIENT_ID) private val serverClientId: String,
) : GoogleAuthClient {

    /**
     * ActivityResultRegistry 등록 키
     *
     * 매 시도마다 새 키를 써야 한다. 키를 재사용하면 register 시점에 이전 회차의 결과가
     * pendingResults 에서 즉시 배달되어, 아직 launch 도 하지 않은 continuation 이 먼저 깨어난다.
     */
    private val keySequence = AtomicInteger(0)

    private val googleSignInOptions: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // 서버가 검증할 수 있는 ID 토큰. 넘기는 값은 web client id 다.
            .requestIdToken(serverClientId)
            .requestEmail()
            .requestId()
            .build()
    }

    override suspend fun signIn(activity: Activity): GoogleAccountVo =
        withContext(Dispatchers.Main.immediate) {

            val client = GoogleSignIn.getClient(context, googleSignInOptions)

            /**
             * 계정 선택 UI 를 항상 띄우기 위해 먼저 로그아웃한다.
             *
             * await 가 핵심이다. 이전 구현은 Task 를 버리고 곧바로 signInIntent 를 띄워
             * 로그아웃이 끝나기 전에 선택 UI 가 뜨는 경합이 있었다.
             * 호출 순서를 ViewModel 에 맡기지 않고 여기서 보장한다.
             */
            runCatching { client.signOut().await() }

            val registryOwner = activity as? ActivityResultRegistryOwner
                ?: throw GoogleLoginException(
                    result = mapOf("reason" to "not ActivityResultRegistryOwner")
                )

            suspendCancellableCoroutine { continuation ->
                val key = "google-sign-in:${keySequence.incrementAndGet()}"

                lateinit var launcher: ActivityResultLauncher<Intent>

                launcher = registryOwner.activityResultRegistry.register(
                    key,
                    StartActivityForResult(),
                ) { activityResult ->
                    /**
                     * 해제를 resume 보다 먼저 한다.
                     * launch 가 아직 진행 중일 때 해제하면 콜백만 사라지고 요청 코드 매핑이 남아
                     * 결과가 pendingResults 로 새며 continuation 이 영원히 깨어나지 못한다.
                     */
                    launcher.unregister()

                    if (continuation.isActive) {
                        runCatching { toGoogleAccountVo(activityResult) }
                            .onSuccess { continuation.resume(it) }
                            .onFailure { continuation.resumeWithException(it) }
                    }
                }

                continuation.invokeOnCancellation { launcher.unregister() }

                launcher.launch(client.signInIntent)
            }
        }

    override suspend fun clearState() {
        withContext(Dispatchers.Main.immediate) {
            runCatching {
                // revokeAccess 가 아니다. 동의까지 철회하면 매번 동의 화면이 다시 뜬다.
                GoogleSignIn.getClient(context, googleSignInOptions).signOut().await()
            }
        }
    }

    /**
     * ActivityResult 를 도메인 값 또는 진단 가능한 예외로 바꾼다.
     *
     * 어떤 경로로도 조용히 빠져나가지 않는다. 이전 구현은 resultCode 가 RESULT_OK 가 아니면
     * 아무 것도 하지 않고 돌아가서, 실패 원인이 로그에도 화면에도 남지 않았다.
     */
    private fun toGoogleAccountVo(activityResult: ActivityResult): GoogleAccountVo {

        // 선택 UI 밖으로 나간 경우. data 조차 없어 statusCode 를 읽을 수 없다.
        if (activityResult.resultCode == Activity.RESULT_CANCELED && activityResult.data == null) {
            throw GoogleLoginCanceledException()
        }

        val account = try {
            /**
             * getResult(ApiException::class) 여야 한다.
             * 인자 없는 getResult 는 실패를 RuntimeExecutionException 으로 감싸 statusCode 를 잃는다.
             */
            GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
                .getResult(ApiException::class.java)
        } catch (exception: ApiException) {
            throw exception.toLoginException(resultCode = activityResult.resultCode)
        }

        return account.toGoogleAccountVo()
    }

    private fun ApiException.toLoginException(resultCode: Int): Throwable {

        if (statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED ||
            statusCode == GoogleSignInStatusCodes.CANCELED
        ) {
            return GoogleLoginCanceledException()
        }

        /**
         * statusCode 를 문자열과 함께 싣는다.
         * BaseViewModel 이 result 맵을 그대로 로그로 찍어주므로 10(DEVELOPER_ERROR, SHA-1/clientId
         * 오설정) 인지 12500 인지 logcat 한 줄로 갈린다.
         */
        return GoogleLoginException(
            result = mapOf(
                "resultCode" to resultCode,
                "statusCode" to statusCode,
                "statusCodeString" to GoogleSignInStatusCodes.getStatusCodeString(statusCode),
            )
        )
    }

    private fun GoogleSignInAccount.toGoogleAccountVo(): GoogleAccountVo {

        // id 는 ID 토큰의 sub 와 같은 값이라 서버의 socialAccountId 계약이 그대로 유지된다.
        val googleAccountId = id.takeUnless { it.isNullOrEmpty() }
            ?: throw GoogleLoginException(result = mapOf("reason" to "account id empty"))

        val accountEmail = email.takeUnless { it.isNullOrEmpty() }
            ?: throw GoogleLoginException(result = mapOf("reason" to "account email empty"))

        val token = idToken.takeUnless { it.isNullOrEmpty() }
            ?: throw GoogleLoginException(result = mapOf("reason" to "idToken null"))

        return GoogleAccountVo(
            googleAccountId = googleAccountId,
            email = accountEmail,
            displayName = displayName?.takeUnless { it.isEmpty() },
            idToken = token,
        )
    }
}
