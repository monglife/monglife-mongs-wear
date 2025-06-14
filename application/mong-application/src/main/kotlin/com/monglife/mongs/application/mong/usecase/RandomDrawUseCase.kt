package com.monglife.mongs.application.mong.usecase

import com.monglife.mongs.application.mong.exception.InvalidBuyRandomDrawTicketException
import com.monglife.mongs.application.mong.exception.InvalidRandomDrawException
import com.monglife.mongs.application.mong.exception.NotFoundMongException
import com.monglife.mongs.application.mong.port.persistence.ManagementPersistencePort
import com.monglife.mongs.application.mong.port.web.InteractionWebPort
import com.monglife.mongs.application.mong.port.web.ManagementWebPort
import com.monglife.mongs.application.mong.vo.RandomDrawVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 랜덤 뽑기 UseCase
 */
class RandomDrawUseCase @Inject constructor(
    private val managementWebPort: ManagementWebPort,
    private val interactionWebPort: InteractionWebPort,
    private val managementPersistencePort: ManagementPersistencePort,
) : BaseParamUseCase<RandomDrawUseCase.Command, RandomDrawVo>() {

    @Throws(NotFoundMongException::class, InvalidRandomDrawException::class, InvalidBuyRandomDrawTicketException::class)
    override suspend fun execute(command: Command): RandomDrawVo {
        return withContext(Dispatchers.IO) {
            // 몽 조회 요청
            managementWebPort.getMong(mongId = command.mongId).let {
                val mong = it.toDomain()
                // 랜덤 뽑기 티켓이 없는 경우
                if (mong.randomDrawTicketCount <= 0) {
                    // 랜덤 뽑기 티켓 구매 요청
                    interactionWebPort.buyRandomDrawTicket(mongId = mong.mongId).let { response ->
                        // 랜덤 뽑기 티켓 구매
                        mong.buyRandomDrawTicket(
                            payPoint = response.payPoint,
                            randomDrawTicketCount = response.randomDrawTicketCount,
                        )
                        // 몽 로컬 등록
                        managementPersistencePort.saveMong(mong = mong)
                    }
                }
                // 랜덤 뽑기 요청
                interactionWebPort.randomDraw(mongId = mong.mongId).let { response ->
                    // RandomDrawVo 반환
                    RandomDrawVo.of(randomDraw = response.toDomain())
                }
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}