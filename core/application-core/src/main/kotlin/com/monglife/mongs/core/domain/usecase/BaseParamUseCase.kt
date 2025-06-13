package com.monglife.mongs.core.domain.usecase

abstract class BaseParamUseCase<P, R> {

    abstract suspend fun execute(command: P): R

    suspend operator fun invoke(command: P): R {
        return try {
            // 메서드 실행
            this.execute(command = command)
        } catch (exception: Exception) {
            throw exception
        }
    }
}

