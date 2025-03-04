package com.mongs.wear.domain.device.usecase

import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.GetMongInteractionDialogOpenFlagException
import com.mongs.wear.core.usecase.BaseNoParamUseCase
import com.mongs.wear.domain.device.repository.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetMongInteractionDialogOpenFlagUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
) : BaseNoParamUseCase<Boolean>() {

    /**
     * 몽 상호작용 다이얼로그 오픈 여부 조회 UseCase
     */
    override suspend fun execute(): Boolean {
        return withContext(Dispatchers.IO) {
            deviceRepository.getMongInteractionDialogOpenFlag()
        }
    }

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            else -> throw GetMongInteractionDialogOpenFlagException()
        }
    }
}