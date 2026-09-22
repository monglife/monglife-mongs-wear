package com.monglife.mongs.application.mong.usecase.mission

import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.application.mong.exception.InvalidClaimMissionRewardException
import com.monglife.mongs.application.mong.port.web.MissionWebPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 미션 보상 수령 UseCase
 *
 * 몽의 페이 포인트·경험치는 여기서 고치지 않는다. 서버가 수령 직후 몽 상태를 MQTT 로 쏘고
 * ManagementPersistenceAdapter 가 그것을 Room 에 반영한다. 여기서 또 고치면 두 경로가 엇갈린다.
 */
class ClaimMissionRewardUseCase @Inject constructor(
    private val missionWebPort: MissionWebPort,
) : BaseParamUseCase<ClaimMissionRewardUseCase.Command, Unit>() {

    @Throws(InvalidClaimMissionRewardException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 미션 보상 수령 요청
            missionWebPort.claimMissionReward(
                accountMissionId = command.accountMissionId,
                mongId = command.mongId,
            )
        }
    }

    data class Command(
        val accountMissionId: Long,
        val mongId: Long,
    )
}
