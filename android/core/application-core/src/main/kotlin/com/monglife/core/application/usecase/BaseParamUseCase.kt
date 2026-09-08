package com.monglife.core.application.usecase

abstract class BaseParamUseCase<P, R> {

    abstract suspend fun execute(command: P): R

    suspend operator fun invoke(command: P): R {
        return try {
            this.execute(command = command)
        } catch (exception: Exception) {
            throw exception
        }
    }
}

