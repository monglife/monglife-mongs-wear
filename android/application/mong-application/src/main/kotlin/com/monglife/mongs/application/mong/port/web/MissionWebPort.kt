package com.monglife.mongs.application.mong.port.web

import com.monglife.mongs.application.mong.exception.InvalidClaimMissionRewardException
import com.monglife.mongs.application.mong.exception.NotFoundMissionException
import com.monglife.mongs.application.mong.port.web.response.ClaimMissionRewardResponse
import com.monglife.mongs.application.mong.port.web.response.GetMissionResponse

interface MissionWebPort {

    /**
     * 미션 목록 조회
     *
     * 한 번에 일간·주간·월간을 모두 돌려준다. 주기별 조회 파라미터는 없다.
     */
    @Throws(NotFoundMissionException::class)
    suspend fun getMissions(): List<GetMissionResponse>

    /**
     * 미션 보상 수령
     *
     * 보상이 몽 소유(경험치·페이 포인트·인벤토리)라 대상 몽이 필요하다.
     */
    @Throws(InvalidClaimMissionRewardException::class)
    suspend fun claimMissionReward(accountMissionId: Long, mongId: Long): ClaimMissionRewardResponse
}
