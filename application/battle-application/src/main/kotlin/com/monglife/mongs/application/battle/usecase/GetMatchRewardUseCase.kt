package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.NotFoundMatchRewardException
import com.monglife.mongs.application.battle.port.web.MatchWebPort
import com.monglife.mongs.application.battle.vo.MatchRewardVo
import com.monglife.core.application.usecase.BaseNoParamUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 매치 보상 페이 포인트 조회 UseCase
 */
class GetMatchRewardUseCase @Inject constructor(
    private val matchWebPort: MatchWebPort,
) : BaseNoParamUseCase<MatchRewardVo>() {

    @Throws(NotFoundMatchRewardException::class)
    override suspend fun execute(): MatchRewardVo {
        return withContext(Dispatchers.IO) {
            // 매치 보상 정보 조회 요청
            matchWebPort.getBattleReward().let { response ->
                // MatchRewardVo 반환
                MatchRewardVo(
                    rewardPayPoint = response.rewardPayPoint,
                    bettingPayPoint = response.battlePayPoint,
                )
            }
        }
    }
}