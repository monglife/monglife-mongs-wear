package com.mongs.wear.domain.auth.usecase

import androidx.lifecycle.LiveData
import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.JoinUseCaseException
import com.mongs.wear.core.usecase.BaseNoParamUseCase
import com.mongs.wear.domain.auth.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetIsLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) : BaseNoParamUseCase<LiveData<Boolean>>() {

    override suspend fun execute(): LiveData<Boolean> {
        return withContext(Dispatchers.IO) {
            authRepository.isLoginLive()
        }
    }

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            else -> throw JoinUseCaseException()
        }
    }
}