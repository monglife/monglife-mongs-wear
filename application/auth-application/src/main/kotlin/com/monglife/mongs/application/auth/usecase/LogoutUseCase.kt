package com.monglife.mongs.application.auth.usecase

import com.monglife.mongs.application.auth.exception.LogoutException
import com.monglife.mongs.application.auth.port.persistence.AuthPersistencePort
import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
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

    @Throws(LogoutException::class)
    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            // 로그 아웃 처리
            authWebPort.logout()
            // 세션 삭제
            authPersistencePort.deleteSession()
        }
    }
}