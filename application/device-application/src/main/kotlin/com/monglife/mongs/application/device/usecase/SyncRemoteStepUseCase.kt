package com.monglife.mongs.application.device.usecase

import com.monglife.mongs.core.domain.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncRemoteStepUseCase @Inject constructor(

) : BaseNoParamUseCase<Unit>() {

    override suspend fun execute() {
        withContext(Dispatchers.IO) {
            
        }
    }
}