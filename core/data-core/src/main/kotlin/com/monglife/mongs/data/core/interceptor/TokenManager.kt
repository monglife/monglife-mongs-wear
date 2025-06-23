//package com.monglife.mongs.data.core.interceptor
//
//import com.monglife.mongs.data.core.client.AuthWebClient
//import com.monglife.mongs.data.core.client.request.ReissueRequestDto
//import com.monglife.mongs.data.core.datastore.SessionDataStore
//import com.monglife.mongs.data.core.exception.InvalidReissueException
//import com.monglife.mongs.domain.auth.model.Session
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.CoroutineStart
//import kotlinx.coroutines.Deferred
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.async
//import kotlinx.coroutines.sync.Mutex
//import kotlinx.coroutines.sync.withLock
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class TokenManager @Inject constructor(
//    private val authWebClient: AuthWebClient,
//    private val sessionDataStore: SessionDataStore,
//) {
//    /**
//     * 토큰 재발행
//     */
//    private val reissueMutex = Mutex()
//    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
//
//    @Volatile
//    private var reissueJob: Deferred<String?>? = null
//    suspend fun reissue(session: Session?): String? {
//        return reissueMutex.withLock {
//            reissueJob?.let { return it.await() }
//            session?.let {
//                val newReissueJob = scope.async(start = CoroutineStart.LAZY) {
//                    try {
//                        val response = authWebClient.reissue(
//                            ReissueRequestDto(session.accessToken, session.refreshToken)
//                        )
//                        val body = response.takeIf { it.isSuccessful }?.body()
//                            ?: throw InvalidReissueException()
//
//                        session.update(body.result.accessToken, body.result.refreshToken)
//                        session.increaseVersion()
//                        sessionDataStore.saveSession(session)
//                        body.result.accessToken
//                    } catch (e: Exception) {
//                        sessionDataStore.deleteSession()
//                        null
//                    }
//                }
//
//                reissueJob = newReissueJob
//                newReissueJob.start()
//
//                try {
//                    newReissueJob.await()
//                } finally {
//                    reissueJob = null
//                }
//            }
//        }
//    }
//}