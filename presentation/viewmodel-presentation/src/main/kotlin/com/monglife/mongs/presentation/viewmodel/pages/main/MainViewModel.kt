package com.monglife.mongs.presentation.viewmodel.pages.main

import android.content.Context
import com.monglife.mongs.application.auth.usecase.LogoutUseCase
import com.monglife.mongs.core.presentation.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logoutUseCase: LogoutUseCase,
): BaseViewModel() {

    fun logout() {
        viewModelScopeWithHandler.launch(Dispatchers.IO) {
            logoutUseCase()
        }
    }

    override fun initialize() {
        // UI 초기화

    }

    override suspend fun exceptionHandler(exception: Throwable) {}
}