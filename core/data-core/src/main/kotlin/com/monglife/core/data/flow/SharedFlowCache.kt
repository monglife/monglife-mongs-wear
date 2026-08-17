package com.monglife.core.data.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import java.util.concurrent.ConcurrentHashMap

/**
 * 키별 SharedFlow 캐시
 *
 * `suspend fun getXxxFlow() = flow { ... }.shareIn(...)` 처럼 함수 본문에서 shareIn 을 호출하면
 * 호출할 때마다 새 SharedFlow 가 생성되어 공유 캐시 역할을 전혀 하지 못한다.
 * (호출자마다 업스트림이 따로 돌아 MQTT 구독/해제와 DB 조회가 중복된다.)
 *
 * 이 캐시는 같은 키에 대해 항상 같은 SharedFlow 인스턴스를 돌려준다.
 */
class SharedFlowCache<K : Any, V>(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val replay: Int = DEFAULT_REPLAY,
    private val stopTimeoutMillis: Long = DEFAULT_STOP_TIMEOUT_MILLIS,
) {

    companion object {
        private const val DEFAULT_REPLAY = 1

        /**
         * 화면 전환 중 구독이 잠깐 끊겨도 업스트림(MQTT 구독)을 유지하기 위한 유예 시간.
         * 0 이면 화면을 옮길 때마다 구독 해제 → 재구독 왕복이 발생한다.
         */
        private const val DEFAULT_STOP_TIMEOUT_MILLIS = 5000L
    }

    private val cache = ConcurrentHashMap<K, SharedFlow<V>>()

    /**
     * 키에 해당하는 SharedFlow 를 반환한다. 없으면 upstream 으로 만들어 캐시한다.
     * computeIfAbsent 로 원자적으로 처리해 경쟁 상황에서 버려지는 SharedFlow 가 생기지 않게 한다.
     */
    fun getOrCreate(key: K, upstream: () -> Flow<V>): SharedFlow<V> =
        cache.computeIfAbsent(key) {
            upstream().shareIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = stopTimeoutMillis),
                replay = replay,
            )
        }
}
