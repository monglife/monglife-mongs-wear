package com.monglife.mongs.core.domain.usecase

abstract class BaseNoParamUseCase<R> {

    abstract suspend fun execute(): R

    suspend operator fun invoke(): R {
        return try {
            this.execute()
        } catch (exception: Exception) {
            throw exception
        }
    }
}

