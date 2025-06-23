package com.monglife.mongs.core.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monglife.mongs.core.domain.exception.ErrorException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.drop
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
            if (exception is ErrorException) {
                Log.e(exception.javaClass.simpleName, "[Exception] ${exception.javaClass.name} ${exception.message} ===> ${exception.result}")

                // 오류 메시지 표출 이벤트 발생
                if (exception.code.isMessageShow()) {
                    errorToast(exception.message)
                }
            } else {
                Log.e(exception.javaClass.simpleName, "[Exception] ${exception.javaClass.name} ${exception.message ?: ""}\n${exception.stackTraceToString()}")
            }

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

    protected fun <T> observeForever(flow: StateFlow<T>, state: MediatorLiveData<T>) {
        viewModelScopeWithHandler.launch {
            flow.drop(1).collect {
                state.value = it
            }
        }
    }
}