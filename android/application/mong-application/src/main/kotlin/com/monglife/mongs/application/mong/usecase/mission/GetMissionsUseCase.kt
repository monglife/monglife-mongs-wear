package com.monglife.mongs.application.mong.usecase.mission

import com.monglife.core.application.usecase.BaseNoParamUseCase
import com.monglife.mongs.application.mong.port.web.MissionWebPort
import com.monglife.mongs.application.mong.vo.MissionVo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 미션 목록 조회 UseCase
 *
 * 서버가 일간·주간·월간을 한 번에 돌려준다. 주기별로 나누는 것은 화면의 몫이다.
 */
class GetMissionsUseCase @Inject constructor(
    private val missionWebPort: MissionWebPort,
) : BaseNoParamUseCase<List<MissionVo>>() {

    override suspend fun execute(): List<MissionVo> {
        return withContext(Dispatchers.IO) {
            // 미션 목록 조회 요청
            missionWebPort.getMissions().map { MissionVo.of(mission = it.toDomain()) }
        }
    }
}
