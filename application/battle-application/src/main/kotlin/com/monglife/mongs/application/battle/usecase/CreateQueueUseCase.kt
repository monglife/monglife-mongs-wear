package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidCreateQueuePlayerException
import com.monglife.mongs.application.battle.exception.InvalidDisSubscribeQueueException
import com.monglife.mongs.application.battle.exception.InvalidQueueMatchingException
import com.monglife.mongs.application.battle.exception.InvalidSubscribeQueueException
import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.port.persistence.DevicePersistencePort
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.subscribe.QueueSubscribePort
import com.monglife.mongs.application.battle.port.subscribe.event.MatchingEvent
import com.monglife.mongs.application.battle.port.subscribe.event.MatchingFailEvent
import com.monglife.mongs.application.battle.port.subscribe.event.QueueEvent
import com.monglife.mongs.application.battle.port.web.QueueWebPort
import com.monglife.mongs.application.battle.vo.MatchVo
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import com.monglife.mongs.domain.battle.model.MatchPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 매치 큐 대기열 등록 UseCase
 */
class CreateQueueUseCase @Inject constructor(
    private val queueWebPort: QueueWebPort,
    private val queueSubscribePort: QueueSubscribePort,
    private val devicePersistencePort: DevicePersistencePort,
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<CreateQueueUseCase.Command, Flow<MatchVo?>>() {

    @Throws(InvalidCreateQueuePlayerException::class, NotFoundMatchException::class, InvalidSubscribeQueueException::class, InvalidDisSubscribeQueueException::class, InvalidQueueMatchingException::class)
    override suspend fun execute(command: Command): Flow<MatchVo?> {
        return withContext(Dispatchers.IO) {
            // 이전 매치가 존재하는 경우 초기화
            runCatching { matchPersistencePort.deleteAllMatch() }
            // 새로운 매치 생성
            val createMatch = Match()
            // 매치 큐 등록
            queueWebPort.createQueuePlayer(mongId = command.mongId)
            // 매치 큐 진행중 변경
            createMatch.search()
            // 매치 로컬 저장
            matchPersistencePort.saveMatch(match = createMatch)

            // 매치 큐 구독
            devicePersistencePort.getDeviceId().let { deviceId ->
                queueSubscribePort.subscribeQueue(deviceId = deviceId).collect {
                    subscribeQueueEvent(
                        event = it,
                        createMatch = createMatch,
                        deviceId = deviceId
                    )
                }
            }

            // 매치 라이브 객체 생성
            combine(
                matchPersistencePort.getMatchFlow(queueId = createMatch.queueId),
                matchPersistencePort.getMatchPlayersFlow(matchId = createMatch.matchId)
            ) { match, matchPlayers ->
                match?.let {
                    MatchVo.of(match = match, matchPlayers = matchPlayers)
                }
            }
        }
    }

    /**
     * 매치 큐 이벤트 처리 메서드
     */
    @Throws(NotFoundMatchException::class, InvalidDisSubscribeQueueException::class, InvalidQueueMatchingException::class)
    private suspend fun subscribeQueueEvent(event: QueueEvent, createMatch: Match, deviceId: String) {
        when (event) {
            // 매치 큐 매칭 성공
            is MatchingEvent -> {
                // 매치 큐 구독 해제
                queueSubscribePort.disSubscribeQueue(deviceId = deviceId)
                // 매치 플레이어 정보 등록
                event.matchPlayers.forEach {
                    matchPersistencePort.saveMatchPlayer(
                        matchPlayer = MatchPlayer(
                            matchId = event.matchId,
                            playerId = it.playerId,
                            mongCode = it.mongCode,
                            mongName = it.mongName,
                            name = it.name,
                        )
                    )
                }
                // 매치 정보 업데이트
                matchPersistencePort.getMatch(queueId = createMatch.queueId)
                    .let { match: Match ->
                        // 매칭 상태로 변경
                        match.matching(matchId = event.matchId)
                        matchPersistencePort.saveMatch(match = match)
                    }
            }
            // 매치 큐 매칭 실패
            is MatchingFailEvent -> {
                // 매치 큐 구독 해제
                queueSubscribePort.disSubscribeQueue(deviceId = deviceId)
                // 모든 매치 삭제
                matchPersistencePort.deleteAllMatch()
                // 매치 큐 매칭 실패 예외
                throw InvalidQueueMatchingException()
            }
        }
    }

    data class Command(
        val mongId: Long,
    )
}