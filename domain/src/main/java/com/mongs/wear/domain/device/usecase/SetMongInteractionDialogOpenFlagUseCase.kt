package com.mongs.wear.domain.device.usecase

import com.mongs.wear.core.exception.global.DataException
import com.mongs.wear.core.exception.usecase.SetMongInteractionDialogOpenFlagException
import com.mongs.wear.core.usecase.BaseParamUseCase
import com.mongs.wear.domain.device.repository.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetMongInteractionDialogOpenFlagUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
) : BaseParamUseCase<SetMongInteractionDialogOpenFlagUseCase.Param, Unit>() {

    /**
     * 몽 상호작용 다이얼로그 오픈 여부 설정 UseCase
     */
    override suspend fun execute(param: Param) {
        withContext(Dispatchers.IO) {
            deviceRepository.setMongInteractionDialogOpenFlag(
                mongInteractionDialogOpenFlag = param.isOpen,
            )
        }
    }

    data class Param(
        val isOpen: Boolean,
    )

    override fun handleException(exception: DataException) {
        super.handleException(exception)

        when(exception) {
            else -> throw SetMongInteractionDialogOpenFlagException()
        }
    }
}