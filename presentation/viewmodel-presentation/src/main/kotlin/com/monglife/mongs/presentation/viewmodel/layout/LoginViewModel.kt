package com.monglife.mongs.presentation.viewmodel.layout

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.monglife.core.presentation.utils.PermissionUtil
import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.auth.exception.InvalidJoinException
import com.monglife.mongs.application.auth.exception.InvalidLoginException
import com.monglife.mongs.application.auth.exception.NeedJoinException
import com.monglife.mongs.application.auth.usecase.JoinUseCase
import com.monglife.mongs.application.auth.usecase.LoginUseCase
import com.monglife.mongs.application.device.usecase.DeleteBackgroundMapCodeUseCase
import com.monglife.mongs.application.mong.usecase.management.DeleteCurrentMongIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionUtil: PermissionUtil,
    private val loginUseCase: LoginUseCase,
    private val joinUseCase: JoinUseCase,
    private val deleteCurrentMongIdUseCase: DeleteCurrentMongIdUseCase,
    private val deleteBackgroundMapCodeUseCase: DeleteBackgroundMapCodeUseCase,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean,
        val signInButton: Boolean,
    ) {
        data object Idle : UiState(loadingBar = false, signInButton = true)
        data object Loading : UiState(loadingBar = true, signInButton = false)
    }

    /**
     * UI 이벤트 정의
     */
    sealed class UiEvent {
        data object Idle: UiEvent()
        data class RequestPermission(val permissions: List<String>): UiEvent()
    }

    /**
     * UI 상태 변수
     */
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /**
     * UI 이벤트 변수
     */
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    /**
     * 권한 부여 설정 인덴트 오픈
     */
    fun verifyPermissionIntentOpen() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            // 권한 확인
            val permissions = buildList {
                addAll(permissionUtil.verifyNotificationPermission())
                addAll(permissionUtil.verifyActivityPermission())
                addAll(permissionUtil.verifyLocationPermission())
            }

            if (permissions.isNotEmpty()) {
                _uiEvent.emit(UiEvent.RequestPermission(permissions))
            }
        }
    }

    /**
     * 권한 부여 설정 인덴트 닫기
     */
    fun verifyPermissionIntentClose() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 구글 로그인
     */
    fun googleLogin(googleLoginLauncher: ActivityResultLauncher<Intent>) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            val googleSignIn = GoogleSignIn.getLastSignedInAccount(context)
            val googleSignInOption = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(context, googleSignInOption)

            if (googleSignIn != null) {
                googleSignInClient.signOut()
            }

            googleLoginLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    /**
     * 서버 로그인
     */
    fun login(googleSignInResult: ActivityResult) {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            if (googleSignInResult.resultCode == Activity.RESULT_OK) {
                GoogleSignIn.getSignedInAccountFromIntent(googleSignInResult.data).result?.let { account ->

                    val googleAccountId = account.id.takeIf { !it.isNullOrEmpty() } ?: throw InvalidLoginException()
                    val email = account.email.takeIf { !it.isNullOrEmpty() } ?: throw InvalidLoginException()

                    withContext(Dispatchers.IO) {
                        loginUseCase(
                            LoginUseCase.Command(
                                googleAccountId = googleAccountId,
                                email = email,
                            )
                        )
                    }
                }
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 서버 회원 가입 & 서버 로그인
     */
    private fun joinAndLogin() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            GoogleSignIn.getLastSignedInAccount(context)?.let { account ->

                val name = account.displayName.takeIf { !it.isNullOrEmpty() } ?: throw InvalidJoinException()
                val googleAccountId = account.id.takeIf { !it.isNullOrEmpty() } ?: throw InvalidJoinException()
                val email = account.email.takeIf { !it.isNullOrEmpty() } ?: throw InvalidJoinException()

                withContext(Dispatchers.IO) {
                    joinUseCase(
                        JoinUseCase.Command(
                            socialAccountId = googleAccountId,
                            email = email,
                            name = name,
                        )
                    )

                    loginUseCase(
                        LoginUseCase.Command(
                            googleAccountId = googleAccountId,
                            email = email,
                        )
                    )
                }
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 화면 초기화 메서드
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            // 구글 로그아웃
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(context, gso).signOut().await()

            withContext(Dispatchers.IO) {
                deleteBackgroundMapCodeUseCase()
                deleteCurrentMongIdUseCase()
            }

            _uiState.value = UiState.Idle
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        when (exception) {
            // 회원 가입 필요 예외
            is NeedJoinException -> {
                joinAndLogin()
            }
            // 그 외의 경우
            else -> {
                initialize()
            }
        }
    }
}