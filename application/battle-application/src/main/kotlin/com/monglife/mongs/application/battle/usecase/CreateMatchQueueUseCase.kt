package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidCreateQueuePlayerException
import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.web.QueueWebPort
import com.monglife.mongs.application.battle.vo.MatchVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배틀 매칭 대기열 등록 UseCase
 */
class CreateMatchQueueUseCase @Inject constructor(
    private val queueWebPort: QueueWebPort,
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<CreateMatchQueueUseCase.Command, Flow<MatchVo?>>() {

    @Throws(NotFoundMatchException::class, InvalidCreateQueuePlayerException::class)
    override suspend fun execute(command: Command): Flow<MatchVo?> {
        return withContext(Dispatchers.IO) {
            // 이전 매치가 존재하는 경우 초기화
            runCatching {
                matchPersistencePort.getMatch(deviceId = command.deviceId).let { match: Match ->
                    // 매치 삭제
                    matchPersistencePort.deleteMatch(matchId = match.matchId)
                }
            }
            // 새로운 매치 생성
            val match = Match(deviceId = command.deviceId)
            // 매치 큐 등록
            queueWebPort.createQueuePlayer(mongId = command.mongId)
            // 매치 큐 진행중 변경
            match.search()
            // 매치 로컬 저장
            matchPersistencePort.saveMatch(match = match)
            // 매치 라이브 객체 반환
            matchPersistencePort.getMatchFlow(matchId = match.matchId).map {
                MatchVo.of(match = it)
            }
        }
    }

    data class Command(
        val mongId: Long,
        val deviceId: String,
    )
}