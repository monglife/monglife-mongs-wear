package com.monglife.mongs.application.auth.usecase

import com.monglife.mongs.application.auth.exception.JoinException
import com.monglife.mongs.application.auth.port.web.AuthWebPort
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 회원 가입 UseCase
 */
class JoinUseCase @Inject constructor(
    private val authWebPort: AuthWebPort,
) : BaseParamUseCase<JoinUseCase.Command, Unit>() {

    @Throws(JoinException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 회원 가입
            authWebPort.join(
                email = command.email,
                name = command.name,
                googleAccountId = command.googleAccountId,
            )
        }
    }

    data class Command(
        val googleAccountId: String,
        val email: String,
        val name: String,
    )
}
