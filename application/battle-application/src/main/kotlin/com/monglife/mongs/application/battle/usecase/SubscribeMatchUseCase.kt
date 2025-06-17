package com.monglife.mongs.application.battle.usecase

import com.monglife.mongs.application.battle.exception.InvalidSubscribeMatchException
import com.monglife.mongs.application.battle.exception.NotFoundMatchException
import com.monglife.mongs.application.battle.port.persistence.MatchPersistencePort
import com.monglife.mongs.application.battle.port.subscribe.MatchSubscribePort
import com.monglife.mongs.application.battle.port.subscribe.event.FightMatchDto
import com.monglife.mongs.application.battle.port.subscribe.event.MatchEventCode
import com.monglife.mongs.core.domain.usecase.BaseParamUseCase
import com.monglife.mongs.domain.battle.model.Match
import com.monglife.mongs.domain.battle.model.MatchPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 매치 구독 UseCase
 */
class SubscribeMatchUseCase @Inject constructor(
    private val matchSubscribePort: MatchSubscribePort,
    private val matchPersistencePort: MatchPersistencePort,
) : BaseParamUseCase<SubscribeMatchUseCase.Command, Unit>() {

    @Throws(NotFoundMatchException::class, InvalidSubscribeMatchException::class)
    override suspend fun execute(command: Command) {
        withContext(Dispatchers.IO) {
            // 매치 구독
            matchSubscribePort.subscribeMatch(matchId = command.matchId).map { matchEvent ->
                when (matchEvent.code) {
                    MatchEventCode.MATCH_PLAYERS_ENTERED -> {
                        // TODO: 매치 입장시
                    }
                    MatchEventCode.MATCH -> {
                        val fightMatchDto = matchEvent as FightMatchDto
                        // 매치 정보 업데이트
                        matchPersistencePort.getMatch(matchId = fightMatchDto.matchId)
                            .let { match: Match ->
                                match.fight(
                                    round = fightMatchDto.round,
                                    isLastRound = fightMatchDto.isLastRound,
                                )
                                matchPersistencePort.saveMatch(match = match)
                            }
                        // 매치 플레이어 정보 업데이트
                        fightMatchDto.matchPlayers.forEach {
                            matchPersistencePort.getMatchPlayer(playerId = it.playerId).let { matchPlayer: MatchPlayer ->
                                matchPlayer.fight(
                                    hp = it.hp,
                                    roundCode = it.roundCode
                                )
                                matchPersistencePort.saveMatchPlayer(matchPlayer = matchPlayer)
                            }
                        }
                    }
                    MatchEventCode.MATCH_END -> {
                        // TODO: 매치 종료시
                    }
                }
            }.collect()
        }
    }

    data class Command(
        val matchId: Long,
    )
}