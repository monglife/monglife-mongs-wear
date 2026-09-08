package com.monglife.core.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monglife.core.common.exception.ErrorException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

abstract class BaseViewModel : ViewModel() {

    companion object {
        // 예외 표출 딜레이
        const val NAVIGATE_DELAY = 500L

        // 오류 메시지 표출 이벤트 (Toast)
        private val _errorEvent = MutableSharedFlow<String>()
        val errorEvent = _errorEvent.asSharedFlow()

        suspend fun errorToast(message: String) {
            _errorEvent.emit(message)
        }
    }

    /**
     * 화면 초기화 메서드
     */
    abstract fun initialize()

    /**
     * ViewModel Exception Handler For Override
     * 필요한 경우 자식 클래스에서 Exception Handler 를 재정의하여 사용
     */
    open suspend fun exceptionHandler(exception: Throwable) {}

    /**
     * ViewModel Exception Handler
     */
    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        CoroutineScope(Dispatchers.IO).launch {
            val out = StringBuilder()
                .append("${exception::class.qualifiedName}\n")
                .append("  - message       => ${exception.message ?: ""}\n")

            if (exception is ErrorException) {
                out.append("  - result        => ${exception.result}\n")

                // 오류 메시지 표출 이벤트 발생
                if (exception.code.isMessageShow()) {
                    errorToast(exception.message)
                }
            }

            out.append("  - exception     => ${exception.stackTraceToString()}")

            Log.e(this@BaseViewModel::class.simpleName ?: "Anonymous", "EXCEPTION >> $out")

            delay(NAVIGATE_DELAY)

            // 자식 클래스 exception handler 실행
            exceptionHandler(exception = exception)
        }
    }

    /**
     * 공통 예외 처리를 위한 coroutine view model scope
     */
    protected val viewModelScopeWithHandler = CoroutineScope(
        viewModelScope.coroutineContext + exceptionHandler
    )

    /**
     * ObserveForever Coroutine Job Map
     */
    private val observeForeverJobMap = ConcurrentHashMap<String, Job>()

    /**
     * View Model 에서의 Flow 구독 등록
     */
    protected suspend fun <T> observeForever(flow: Flow<T>, state: MutableStateFlow<T>) =
        observeForever(flow = flow) { state.emit(it) }

    /**
     * View Model 에서의 Flow 구독 등록 (여러 파생 상태를 한 번의 구독으로 갱신)
     *
     * 파생 상태 개수만큼 observeForever 를 거는 대신 이 오버로드를 쓰면
     * cold flow 를 한 번만 구독한다.
     */
    protected suspend fun <T> observeForever(flow: Flow<T>, onEach: suspend (T) -> Unit) =
        UUID.randomUUID().toString().also { key ->
            /**
             * 첫 값 수신까지 대기하되 구독은 한 번만 한다.
             * 이전에는 collect 와 별개로 flow.first() 를 한 번 더 호출해
             * cold flow 를 두 번 구독했고, 그 탓에 MQTT 구독/해제와 DB 조회가 중복 발생했다.
             */
            val firstEmitted = CompletableDeferred<Unit>()

            observeForeverJobMap[key] = viewModelScopeWithHandler.launch(Dispatchers.IO) {
                try {
                    flow.collect {
                        onEach(it)
                        firstEmitted.complete(Unit)
                    }
                } finally {
                    // 값 없이 종료·취소되어도 호출자가 영구 대기하지 않도록 해제
                    firstEmitted.complete(Unit)
                }
            }

            firstEmitted.await()
        }

    /**
     * View Model 에서의 Flow 구독 해제
     */
    protected fun observeStop(key: String) {
        observeForeverJobMap[key]?.let {
            it.cancel()
            observeForeverJobMap.remove(key)
        }
    }
}