package com.monglife.core.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monglife.core.common.error.CrashReporter
import com.monglife.core.common.exception.ErrorException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

abstract class BaseViewModel : ViewModel() {

    companion object {
        // 예외 표출 딜레이
        const val NAVIGATE_DELAY = 500L

        /**
         * 오류 메시지 표출 이벤트 (Toast)
         *
         * SharedFlow 가 아니라 Channel 이다. SharedFlow 는 replay 가 0 이면 구독자가 없는 순간
         * emit 한 값을 그냥 버린다. 수집자는 LayoutView 한 곳인데, 앱 기동 중 그 화면이 붙기
         * 전에 난 오류는 그렇게 소리 없이 사라졌다.
         */
        private val _errorEvent = Channel<String>(Channel.BUFFERED)
        val errorEvent = _errorEvent.receiveAsFlow()

        suspend fun errorToast(message: String) {
            _errorEvent.send(message)
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

            /**
             * 여기서 삼킨 예외는 앱을 죽이지 않으므로 Crashlytics 가 자동으로 잡지 못한다.
             * 릴리스에서는 이 Log.e 를 볼 방법이 기기에 붙는 것뿐이라 명시적으로 올린다.
             *
             * ErrorException 은 뺀다. 서버가 코드로 알려 주는 정상적인 거절(잔액 부족,
             * 쿨다운 등)이라 버그가 아니고, 그대로 올리면 수집기가 그 소음에 묻힌다.
             */
            if (exception !is ErrorException) {
                CrashReporter.record(exception)
            }

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