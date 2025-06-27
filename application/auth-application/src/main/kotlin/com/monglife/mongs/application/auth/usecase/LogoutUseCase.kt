package com.monglife.mongs.application.auth.usecase

import com.monglife.mongs.application.auth.exception.InvalidLogoutException
import com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import com.monglife.mongs.domain.auth.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 로그 아웃 UseCase
 */
class LogoutUseCase @Inject constructor(
    private val authWebPort: AuthWebPort,
    private val authPersistencePort: AuthPersistencePort,
) : BaseNoParamUseCase<Unit>() {

    @Throws(InvalidLogoutException::class)
    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            authPersistencePort.getSession()?.let { session: Session ->
                // 로그 아웃 요청
                authWebPort.logout(refreshToken = session.refreshToken)
                // 세션 로컬 삭제
                authPersistencePort.deleteSession()
                // 현재 몽 설정 삭제
            } ?: throw InvalidLogoutException()
        }
    }
}