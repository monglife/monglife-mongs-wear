package com.monglife.mongs.presentation.viewmodel.layout

import android.app.Activity
import com.monglife.core.auth.client.GoogleAuthClient
import com.monglife.core.auth.exception.GoogleLoginCanceledException
import com.monglife.core.auth.vo.GoogleAccountVo
import com.monglife.core.presentation.utils.PermissionUtil
import com.monglife.core.presentation.viewmodel.BaseViewModel
import com.monglife.mongs.application.auth.exception.InvalidJoinException
import com.monglife.mongs.application.auth.exception.NeedJoinException
import com.monglife.mongs.application.auth.usecase.JoinUseCase
import com.monglife.mongs.application.auth.usecase.LoginUseCase
import com.monglife.mongs.application.device.usecase.DeleteBackgroundMapCodeUseCase
import com.monglife.mongs.application.mong.usecase.management.DeleteCurrentMongIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val googleAuthClient: GoogleAuthClient,
    private val loginUseCase: LoginUseCase,
    private val joinUseCase: JoinUseCase,
    private val deleteCurrentMongIdUseCase: DeleteCurrentMongIdUseCase,
    private val deleteBackgroundMapCodeUseCase: DeleteBackgroundMapCodeUseCase,
    private val permissionUtil: PermissionUtil,
): BaseViewModel() {

    /**
     * UI 상태 정의
     */
    sealed class UiState(
        val loadingBar: Boolean = false,
        val signInButton: Boolean = false,
    ) {
        data object Idle : UiState(signInButton = true)
        data object Loading : UiState(loadingBar = true)
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
     * 직전 구글 로그인 결과
     *
     * 회원 가입 분기에서 계정 정보가 다시 필요한데, 그때 구글에 재조회하지 않는다.
     * Credential Manager 에는 getLastSignedInAccount 에 해당하는 API 가 아예 없고,
     * legacy 에서도 로그인 직전 signOut 을 하므로 재조회하면 null 이 돌아온다.
     * 조용히 아무 것도 안 하고 끝나던 이전 버그의 원인이 그것이었다.
     */
    @Volatile
    private var pendingGoogleAccount: GoogleAccountVo? = null

    /**
     * 예외 복구 재진입 가드
     *
     * exceptionHandler 가 부르는 복구 메서드가 다시 실패하면 같은 핸들러로 돌아와
     * 무한히 반복된다. 각각 한 번만 시도하게 막는다.
     */
    private val recovering = AtomicBoolean(false)
    private val joining = AtomicBoolean(false)

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
     * 구글 로그인 & 서버 로그인
     *
     * legacy 는 signInIntent 실행과 결과 수신이 ActivityResult 로 갈라져 있었다.
     * Credential Manager 는 한 번의 suspend 호출로 끝나 그 왕복이 사라진다.
     */
    fun googleLogin(activity: Activity) {
        // 중복 클릭 방지
        if (_uiState.value is UiState.Loading) return

        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            val googleAccountVo = googleAuthClient.signIn(activity = activity)

            pendingGoogleAccount = googleAccountVo

            withContext(Dispatchers.IO) {
                loginUseCase(
                    LoginUseCase.Command(
                        googleAccountId = googleAccountVo.googleAccountId,
                        email = googleAccountVo.email,
                        idToken = googleAccountVo.idToken,
                    )
                )
            }

            /**
             * 실패 경로에서는 여기까지 오지 않는다. 상태 복구는 exceptionHandler 가 전담한다.
             * 여기서 finally 로 Idle 을 넣으면 회원 가입이 필요한 경우
             * 로딩바 -> 버튼 -> 로딩바 로 깜빡인다.
             */
            _uiState.value = UiState.Idle
        }
    }

    /**
     * 서버 회원 가입 & 서버 로그인
     */
    private fun joinAndLogin() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            _uiState.value = UiState.Loading

            val googleAccountVo = pendingGoogleAccount ?: throw InvalidJoinException()

            val name = googleAccountVo.displayName?.takeIf { it.isNotEmpty() }
                ?: googleAccountVo.email

            withContext(Dispatchers.IO) {
                joinUseCase(
                    JoinUseCase.Command(
                        socialAccountId = googleAccountVo.googleAccountId,
                        email = googleAccountVo.email,
                        name = name,
                        idToken = googleAccountVo.idToken,
                    )
                )

                loginUseCase(
                    LoginUseCase.Command(
                        googleAccountId = googleAccountVo.googleAccountId,
                        email = googleAccountVo.email,
                        idToken = googleAccountVo.idToken,
                    )
                )
            }

            _uiState.value = UiState.Idle
        }
    }

    /**
     * 화면 초기화 메서드
     */
    override fun initialize() {
        viewModelScopeWithHandler.launch(Dispatchers.Main) {
            /**
             * 자격증명 선택 상태 초기화.
             * legacy 구현에서는 signOut().await() 로 실현된다 - 어느 구현인지는 알 필요가 없다.
             * GMS 가 비정상인 기기에서 이 한 줄 때문에 로그인 화면 자체가 못 뜨면 안 된다.
             */
            runCatching { googleAuthClient.clearState() }

            withContext(Dispatchers.IO) {
                deleteBackgroundMapCodeUseCase()
                deleteCurrentMongIdUseCase()
            }

            pendingGoogleAccount = null
            recovering.set(false)
            joining.set(false)

            _uiState.value = UiState.Idle
        }
    }

    override suspend fun exceptionHandler(exception: Throwable) {
        when (exception) {
            // 사용자 취소. 복구할 것이 없다.
            is GoogleLoginCanceledException -> {
                _uiState.value = UiState.Idle
            }
            // 회원 가입 필요 예외
            is NeedJoinException -> {
                if (joining.compareAndSet(false, true)) {
                    joinAndLogin()
                } else {
                    // 가입 후에도 다시 가입을 요구받는 상태. 반복해도 결과가 같다.
                    errorToast(InvalidJoinException().message)
                    _uiState.value = UiState.Idle
                }
            }
            // 그 외의 경우
            else -> {
                if (recovering.compareAndSet(false, true)) {
                    initialize()
                } else {
                    _uiState.value = UiState.Idle
                }
            }
        }
    }
}
