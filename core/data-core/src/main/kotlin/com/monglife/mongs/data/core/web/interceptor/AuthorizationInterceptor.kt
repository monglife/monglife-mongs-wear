package com.monglife.mongs.data.core.web.interceptor

import com.monglife.mongs.data.core.web.client.AuthWebClient
import com.monglife.mongs.data.core.web.client.request.ReissueRequestDto
import com.monglife.mongs.data.core.web.constant.HttpConst
import com.monglife.mongs.data.core.persistence.datastore.SessionDataStore
import com.monglife.mongs.data.core.persistence.entity.SessionEntity
import com.monglife.mongs.data.core.web.exception.InvalidReissueException
import com.monglife.mongs.domain.auth.model.Session
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import javax.inject.Inject

class AuthorizationInterceptor @Inject constructor(
    private val authWebClient: AuthWebClient,
    private val sessionDataStore: SessionDataStore,
) : Interceptor {

    /**
     * 인증/인가 Interceptor
     */
    private val interceptorMutex = Mutex()
    override fun intercept(chain: Chain): Response {
        val session = runBlocking { sessionDataStore.getSession()?.toDomain() }

        // 요청 생성
        val request = session?.let {
            generateRequest(chain = chain, accessToken = it.accessToken)
        } ?: chain.request()

        // 응답 수신
        val response = chain.proceed(request)

        // 인증 실패 (토큰 만료) 시
        return if (response.code() == HttpConst.FORBIDDEN_HTTP_STATUS_CODE) {
            response.close()
            // 토큰 재발행 뮤텍스
            runBlocking {
                interceptorMutex.withLock {
                    val currentSession = sessionDataStore.getSession()

                    if (session != null && currentSession != null) {
                        // 최초 요청 토큰과 동일 여부 검증
                        if (session.version < currentSession.version) {
                            // 이미 다른 코루틴에서 재발행한 경우 (version 이 맞지 않음)
                            chain.proceed(
                                generateRequest(
                                    chain = chain,
                                    accessToken = currentSession.accessToken
                                )
                            )
                        } else {
                            // 최초 요청 토큰과 동일한 버전인 경우 재발행 진행
                            reissue(session = session)
                                ?.let { accessToken: String ->
                                    chain.proceed(
                                        generateRequest(
                                            chain = chain,
                                            accessToken = accessToken
                                        )
                                    )
                                }
                                ?: generateAuthorizationErrorResponse(chain = chain)
                        }
                    } else {
                        generateAuthorizationErrorResponse(chain = chain)
                    }
                }
            }
        } else response
    }

    /**
     * 토큰 재발행
     */
    private suspend fun reissue(session: Session): String? = try {
        val response = authWebClient.reissue(
            ReissueRequestDto(session.accessToken, session.refreshToken)
        )
        val body = response.takeIf { it.isSuccessful }?.body()
            ?: throw InvalidReissueException()

        session.update(body.result.accessToken, body.result.refreshToken)
        session.increaseVersion()
        sessionDataStore.saveSession(
            sessionEntity = SessionEntity(
                accountId = session.accountId,
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                version = session.version,
            )
        )
        body.result.accessToken
    } catch (e: Exception) {
        sessionDataStore.deleteSession()
        null
    }

    /**
     * 인증 요청 헤더 생성
     */
    private fun generateRequest(chain: Chain, accessToken: String) = chain.request().newBuilder()
        .addHeader(HttpConst.AUTHORIZATION_HEADER, "Bearer $accessToken")
        .build()

    /**
     * 인증 불가 가상 응답 객체 생성
     */
    private fun generateAuthorizationErrorResponse(chain: Chain) = Response.Builder()
        .request(chain.request())
        .protocol(Protocol.HTTP_1_1)
        .code(HttpConst.AUTHORIZATION_HTTP_STATUS_CODE)
        .message(HttpConst.AUTHORIZATION_MESSAGE)
        .body(ResponseBody.create(MediaType.get("application/json"), ""))
        .build()
}