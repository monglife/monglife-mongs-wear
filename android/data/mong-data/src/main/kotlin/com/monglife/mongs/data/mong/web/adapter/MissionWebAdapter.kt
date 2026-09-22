package com.monglife.mongs.data.mong.web.adapter

import com.monglife.mongs.application.mong.exception.InvalidClaimMissionRewardException
import com.monglife.mongs.application.mong.exception.NotFoundMissionException
import com.monglife.mongs.application.mong.port.web.MissionWebPort
import com.monglife.mongs.application.mong.port.web.response.ClaimMissionRewardResponse
import com.monglife.mongs.application.mong.port.web.response.GetMissionResponse
import com.monglife.mongs.application.mong.port.web.response.GetMissionRewardResponse
import com.monglife.mongs.data.mong.web.client.MissionWebClient
import com.monglife.mongs.data.mong.web.client.request.ClaimMissionRewardRequestDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionWebAdapter @Inject constructor(
    private val missionWebClient: MissionWebClient,
) : MissionWebPort {

    /**
     * 미션 목록 조회
     *
     * 다른 목록 조회와 달리 빈 목록으로 넘기지 않는다. 미션은 서버가 첫 조회 때 적재하므로
     * 정상 응답이면 반드시 비어 있지 않고, 비어 있다면 통신이 잘못된 것이다.
     */
    @Throws(NotFoundMissionException::class)
    override suspend fun getMissions(): List<GetMissionResponse> =
        missionWebClient.getMissions().let { response ->

            val body = response.takeIf { it.isSuccessful }?.body() ?: throw NotFoundMissionException()

            body.result.map { missionDto ->
                GetMissionResponse(
                    accountMissionId = missionDto.accountMissionId,
                    missionCode = missionDto.missionCode,
                    cycleCode = missionDto.cycleCode,
                    goalTypeCode = missionDto.goalTypeCode,
                    title = missionDto.title,
                    description = missionDto.description,
                    goalCount = missionDto.goalCount,
                    progressCount = missionDto.progressCount,
                    stateCode = missionDto.stateCode,
                    claimedAt = missionDto.claimedAt,
                    rewards = missionDto.rewards.map { rewardDto ->
                        GetMissionRewardResponse(
                            rewardTypeCode = rewardDto.rewardTypeCode,
                            rewardCode = rewardDto.rewardCode,
                            inventoryTypeCode = rewardDto.inventoryTypeCode,
                            amount = rewardDto.amount,
                        )
                    },
                )
            }
        }

    /**
     * 미션 리워드 수령
     */
    @Throws(InvalidClaimMissionRewardException::class)
    override suspend fun claimMissionReward(accountMissionId: Long, mongId: Long): ClaimMissionRewardResponse =
        missionWebClient.claimMissionReward(
            accountMissionId = accountMissionId,
            claimMissionRewardRequestDto = ClaimMissionRewardRequestDto(mongId = mongId),
        ).let { response ->

            val body = response.takeIf { it.isSuccessful }?.body() ?: throw InvalidClaimMissionRewardException()

            ClaimMissionRewardResponse(
                accountMissionId = body.result.accountMissionId,
                mongId = body.result.mongId,
                expRatio = body.result.expRatio,
                payPoint = body.result.payPoint,
            )
        }
}
