package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidCreateQueuePlayerException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.web.QueueWebPort
import com.monglife.mongs.application.battle.vo.MatchVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 배틀 매칭 대기열 등록 UseCase
 */
class CreateMatchQueueUseCase @Inject constructor(
    private val queueWebPort: QueueWebPort,
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<CreateMatchQueueUseCase.Command, Flow<MatchVo?>>() {

    @Throws(InvalidCreateQueuePlayerException::class)
    override suspend fun execute(command: Command): Flow<MatchVo?> {
        return withContext(Dispatchers.IO) {
            // 이전 매치가 존재하는 경우 초기화
            runCatching {
                // 매치 삭제
                matchPersistencePort.deleteAllMatch()
            }
            // 새로운 매치 생성
            val newMatch = Match()
            // 매치 큐 등록
            queueWebPort.createQueuePlayer(mongId = command.mongId)
            // 매치 큐 진행중 변경
            newMatch.search()
            // 매치 로컬 저장
            matchPersistencePort.saveMatch(match = newMatch)
            // 매치 라이브 객체 반환
            val matchFlow = matchPersistencePort.getMatchFlow(queueId = newMatch.queueId)
            val matchPlayersFlow = matchPersistencePort.getMatchPlayersFlow(matchId = newMatch.matchId)
            combine(matchFlow, matchPlayersFlow) { match, matchPlayers ->
                match?.let { MatchVo.of(match = match, matchPlayers = matchPlayers) }
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}