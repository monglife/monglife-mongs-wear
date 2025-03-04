package com.mongs.wear.core.exception.usecase

import com.mongs.wear.core.errors.UseCaseErrorCode
import com.mongs.wear.core.errors.ErrorCode
import com.mongs.wear.core.exception.global.UseCaseException

class GetTrainingUseCaseException(
    override val code: ErrorCode = UseCaseErrorCode.USE_CASE_GET_TRAINING_FAILED,
    override val message: String = code.getMessage()
) : UseCaseException(code = code, message = message)

class TrainingEndUseCaseException(
    override val code: ErrorCode = UseCaseErrorCode.USE_CASE_TRAINING_END_FAILED,
    override val message: String = code.getMessage()
) : UseCaseException(code = code, message = message)