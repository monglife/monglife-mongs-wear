package com.mongs.wear.core.exception.data

import com.mongs.wear.core.errors.DataErrorCode
import com.mongs.wear.core.exception.global.DataException

class GetTrainingException(result: Map<String, Any> = emptyMap()) : DataException(
    code = DataErrorCode.DATA_ACTIVITY_GET_TRAINING,
    result = result,
)

class TrainingRunnerException(result: Map<String, Any> = emptyMap()) : DataException(
    code = DataErrorCode.DATA_ACTIVITY_TRAINING_RUNNER,
    result = result,
)

class TrainingBasketballException(result: Map<String, Any> = emptyMap()) : DataException(
    code = DataErrorCode.DATA_ACTIVITY_TRAINING_BASKETBALL,
    result = result,
)