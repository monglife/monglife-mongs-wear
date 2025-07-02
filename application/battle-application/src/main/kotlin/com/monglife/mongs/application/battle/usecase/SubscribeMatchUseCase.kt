package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidPublishMatchEnterException
import com.monglife.mongs.application.battle.exception.InvalidSubscribeMatchException
import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.exception.NotFoundMatchPlayerException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.subscribe.MatchSubscribePort
import com.monglife.mongs.application.battle.port.subscribe.event.MatchEndEvent
import com.monglife.mongs.application.battle.port.subscribe.event.MatchEvent
import com.monglife.mongs.application.battle.port.subscribe.event.MatchRoundOverEvent
import com.monglife.mongs.application.battle.port.subscribe.event.MatchStartEvent
import com.monglife.core.application.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import com.monglife.mongs.domain.battle.model.MatchPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 매치 구독 UseCase
 */
class SubscribeMatchUseCase @Inject constructor(
    private val matchSubscribePort: MatchSubscribePort,
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<SubscribeMatchUseCase.Command, Unit>() {

    @Throws(NotFoundMatchException::class, InvalidPublishMatchEnterException::class, NotFoundMatchPlayerException::class, InvalidSubscribeMatchException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 로컬 조회
            matchPersistencePort.getMatch(matchId = command.matchId).let {
                // 매치 구독
                matchSubscribePort.subscribeMatch(matchId = command.matchId).collect {
                    subscribeMatchEvent(event = it)
                }
            }
        }
    }

    /**
     * 매치 구독 이벤트 처리 메서드
     */
    @Throws(NotFoundMatchException::class, NotFoundMatchPlayerException::class, InvalidSubscribeMatchException::class)
    private suspend fun subscribeMatchEvent(event: MatchEvent) {
        when (event){
            // 매치 시작
            is MatchStartEvent -> {
                // 매치 플레이어 정보 업데이트
                event.matchPlayers.forEach {
                    matchPersistencePort.getMatchPlayer(playerId = it.playerId).let { matchPlayer: MatchPlayer ->
                        matchPlayer.update(
                            hp = it.hp,
                            roundCode = it.roundCode
                        )
                        matchPersistencePort.saveMatchPlayer(matchPlayer = matchPlayer)
                    }
                }
                // 매치 정보 업데이트
                matchPersistencePort.getMatch(matchId = event.matchId)
                    .let { match: Match ->
                        match.update(
                            round = event.round,
                            isLastRound = event.isLastRound,
                        )
                        // 매치 시작
                        match.start()
                        matchPersistencePort.saveMatch(match = match)
                    }
            }
            is MatchRoundOverEvent -> {
                // 매치 플레이어 정보 업데이트
                event.matchPlayers.forEach {
                    matchPersistencePort.getMatchPlayer(playerId = it.playerId).let { matchPlayer: MatchPlayer ->
                        matchPlayer.update(
                            hp = it.hp,
                            roundCode = it.roundCode
                        )
                        matchPersistencePort.saveMatchPlayer(matchPlayer = matchPlayer)
                    }
                }
                // 매치 정보 업데이트
                matchPersistencePort.getMatch(matchId = event.matchId)
                    .let { match: Match ->
                        match.update(
                            round = event.round,
                            isLastRound = event.isLastRound,
                        )
                        // 다음 라운드 진행
                        if (match.isLastRound) {
                            match.end()
                        }
                        // 매치 수정
                        matchPersistencePort.saveMatch(match = match)
                    }
            }
            is MatchEndEvent -> {
                matchPersistencePort.getMatch(matchId = event.matchId)
                    .let { match: Match ->
                        // 매치 종료
                        match.end()
                        // 매치 수정
                        matchPersistencePort.saveMatch(match = match)
                    }
            }

            else -> {}
        }
    }

    data class Command(
        val matchId: Long,
    )
}