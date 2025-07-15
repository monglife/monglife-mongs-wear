package com.monglife.mongs.application.auth.usecase

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.auth.exception.InvalidJoinException
import com.monglife.mongs.application.auth.port.web.AuthWebPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 회원 가입 UseCase
 */
class JoinUseCase @Inject constructor(
    private val authWebPort: AuthWebPort,
) : BaseParamUseCase<JoinUseCase.Command, Unit>() {

    @Throws(InvalidJoinException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 회원 가입 요청
            authWebPort.join(
                email = command.email,
                name = command.name,
                socialAccountId = command.socialAccountId,
            )
        }
    }

    data class Command(
        val socialAccountId: String,
        val email: String,
        val name: String,
    )
}
