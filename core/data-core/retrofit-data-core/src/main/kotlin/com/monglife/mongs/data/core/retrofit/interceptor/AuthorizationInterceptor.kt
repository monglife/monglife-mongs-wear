package com.monglife.mongs.data.core.retrofit.interceptor

import androidx.datastore.preferences.core.edit
import com.monglife.mongs.data.core.retrofit.client.AuthWebClient
import com.monglife.mongs.data.core.retrofit.client.request.ReissueRequestDto
import com.monglife.mongs.data.core.retrofit.constant.HttpConst
import com.monglife.mongs.data.core.retrofit.exception.InvalidReissueException
import com.monglife.mongs.data.core.retrofit.datastore.SessionDataStore
import com.monglife.mongs.domain.auth.model.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    override fun intercept(chain: Chain): Response {
        // 요청 생성
        val request = runBlocking { getSession() }?.let { session: Session ->
            generateRequest(chain = chain, accessToken = session.accessToken)
        } ?: chain.request()

        // 응답 수신
        var response = chain.proceed(request)

        // 인증 실패 (토큰 만료) 시
        if (response.code() == HttpConst.FORBIDDEN_HTTP_STATUS_CODE) {
            runBlocking {
                // Session 이 존재하는 경우
                val session = getSession()
                response = if (session != null) {
                    reissue(session = session)?.let { accessToken: String ->
                        chain.proceed(generateRequest(chain = chain, accessToken = accessToken))
                    } ?: generateAuthorizationErrorResponse(chain = chain)
                } else {
                    generateAuthorizationErrorResponse(chain = chain)
                }
            }
        }

        return response
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

    /**
     * 토큰 재발행
     */
    @Volatile
    private var reissueJob: Deferred<String?>? = null
    private val reissueMutex = Mutex()
    private suspend fun reissue(session: Session): String? = reissueMutex.withLock {
        // 이미 진행 중인 reissueJob 이 있다면 재사용
        reissueJob?.let {
            return it.await()
        }
        // 새로운 재발급 시도
        reissueJob = CoroutineScope(Dispatchers.IO).async {
            try {

                authWebClient.reissue(reissueRequestDto = ReissueRequestDto(refreshToken = session.refreshToken))
                    .let { response ->
                        val body = response.takeIf { it.isSuccessful }?.body() ?: throw InvalidReissueException()
                        session.update(
                            accessToken = body.result.accessToken,
                            refreshToken = body.result.refreshToken,
                        )
                        saveSession(session).accessToken
                    }
            } catch (e: Exception) {
                deleteSession()
                null
            }
        }

        try {
            return reissueJob!!.await()
        } finally {
            reissueJob = null
        }
    }

    /**
     * 세션 조회
     */
    private suspend fun getSession(): Session? = sessionDataStore.getStore().data.map {

        val accountId = it[SessionDataStore.ACCOUNT_ID]
        val accessToken = it[SessionDataStore.ACCESS_TOKEN]
        val refreshToken = it[SessionDataStore.REFRESH_TOKEN]

        if (accountId != SessionDataStore.ACCOUNT_INIT_VALUE && accessToken != SessionDataStore.ACCESS_TOKEN_INIT_VALUE && refreshToken != SessionDataStore.REFRESH_TOKEN_INIT_VALUE) {
            Session(
                accountId = it[SessionDataStore.ACCOUNT_ID]!!,
                accessToken = it[SessionDataStore.ACCESS_TOKEN]!!,
                refreshToken = it[SessionDataStore.REFRESH_TOKEN]!!,
            )
        } else {
            null
        }
    }.first()

    /**
     * 세션 저장
     */
    private suspend fun saveSession(session: Session): Session = sessionDataStore.getStore().let { store ->

        store.edit { preferences ->
            preferences[SessionDataStore.ACCOUNT_ID] = session.accountId
            preferences[SessionDataStore.ACCESS_TOKEN] = session.accessToken
            preferences[SessionDataStore.REFRESH_TOKEN] = session.refreshToken
        }

        store.data.map {
            Session(
                accountId = it[SessionDataStore.ACCOUNT_ID]!!,
                accessToken = it[SessionDataStore.ACCESS_TOKEN]!!,
                refreshToken = it[SessionDataStore.REFRESH_TOKEN]!!,
            )
        }.first()
    }


    /**
     * 세션 삭제
     */
    private suspend fun deleteSession(): Session = sessionDataStore.getStore().let { store ->

        val session = store.data.map {
            Session(
                accountId = it[SessionDataStore.ACCOUNT_ID]!!,
                accessToken = it[SessionDataStore.ACCESS_TOKEN]!!,
                refreshToken = it[SessionDataStore.REFRESH_TOKEN]!!,
            )
        }

        store.edit { preferences ->
            preferences[SessionDataStore.ACCOUNT_ID] = SessionDataStore.ACCOUNT_INIT_VALUE
            preferences[SessionDataStore.ACCESS_TOKEN] = SessionDataStore.ACCESS_TOKEN_INIT_VALUE
            preferences[SessionDataStore.REFRESH_TOKEN] = SessionDataStore.REFRESH_TOKEN_INIT_VALUE
        }

        return session.first()
    }
}