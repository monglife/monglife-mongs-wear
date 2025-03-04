package com.mongs.wear.data.global.interceptor

import com.mongs.wear.data.auth.api.AuthApi
import com.mongs.wear.data.auth.dataStore.TokenDataStore
import com.mongs.wear.data.auth.dto.request.ReissueRequestDto
import com.mongs.wear.core.exception.data.ReissueException
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody

class AuthorizationInterceptor (
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore,
) : Interceptor {

    companion object {
        private const val AUTHORIZATION_HEADER = "Authorization"

        private const val AUTHORIZATION_RESPONSE_CODE = 403
        private const val AUTHORIZATION_RESPONSE_MESSAGE = "권한 인가 실패"

        private const val CONNECTION_FORBIDDEN_CODE = 401
    }

    /**
     * 엑세스 토큰 인터 셉터
     */
    override fun intercept(chain: Chain): Response {

        val accessToken = runBlocking { tokenDataStore.getAccessToken() }

        val response = chain.proceed(this.generateRequest(chain, accessToken))

        if (response.code() == CONNECTION_FORBIDDEN_CODE) {
            return runBlocking {
                try {
                    val newAccessToken = reissue()

                    return@runBlocking chain.proceed(generateRequest(chain, newAccessToken))

                } catch (e: ReissueException) {
                    // 리프래시 토큰까지 만료된 경우 로그아웃 처리
                    tokenDataStore.setAccessToken(accessToken = "")
                    tokenDataStore.setRefreshToken(refreshToken = "")

                    return@runBlocking Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(AUTHORIZATION_RESPONSE_CODE)
                        .message(AUTHORIZATION_RESPONSE_MESSAGE)
                        .body(ResponseBody.create(MediaType.get("application/json"), ""))
                        .build()
                }
            }
        } else {
            return response
        }
    }

    /**
     * Http 요청 생성
     */
    private fun generateRequest(chain: Chain, accessToken: String) : Request = chain.request().newBuilder()
        .addHeader(AUTHORIZATION_HEADER, "Bearer $accessToken")
        .build()

    /**
     * 토큰 재발행
     */
    private suspend fun reissue() : String {

        val refreshToken = tokenDataStore.getRefreshToken()

        val response = authApi.reissue(ReissueRequestDto(refreshToken = refreshToken))

        if (response.isSuccessful) {
            response.body()?.let { body ->
                tokenDataStore.setAccessToken(accessToken = body.result.accessToken)
                tokenDataStore.setRefreshToken(refreshToken = body.result.refreshToken)

                return body.result.accessToken
            }
        }

        throw ReissueException()
    }
}