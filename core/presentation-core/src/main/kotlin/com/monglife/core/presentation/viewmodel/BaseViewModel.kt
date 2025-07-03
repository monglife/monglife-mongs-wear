package com.monglife.core.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monglife.core.common.exception.ErrorException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    companion object {
        /**
         * 오류 메시지 표출 이벤트 (Toast)
         */
        private val _errorEvent = MutableSharedFlow<String>()
        val errorEvent = _errorEvent.asSharedFlow()
        suspend fun errorToast(message: String) {
            _successEvent.emit(message)
        }

        /**
         * 성공 메시지 표출 이벤트 (Toast)
         */
        private val _successEvent = MutableSharedFlow<String>()
        val successEvent = _successEvent.asSharedFlow()
        suspend fun successToast(message: String) {
            _successEvent.emit(message)
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
     * View Model 에서의 Cold Flow 구독 등록
     */
    protected suspend fun <T> observeForever(flow: Flow<T>, state: MutableStateFlow<T>) {

        state.value = flow.first()

        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            flow.collect {
                state.emit(it)
            }
        }
    }
}