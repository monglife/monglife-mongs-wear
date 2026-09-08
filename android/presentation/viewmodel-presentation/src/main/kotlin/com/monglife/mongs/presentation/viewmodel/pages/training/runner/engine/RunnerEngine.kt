package com.monglife.mongs.presentation.viewmodel.pages.training.runner.engine

import com.monglife.mongs.presentation.viewmodel.pages.training.runner.vo.RunnerVo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunnerEngine @Inject constructor() {

    companion object {
        private const val GRAVITY = 9.8f
        private const val FRAME = 60L
        private const val PLAYER_SPEED = 42f
        private const val HURDLE_SPEED = 3f
        private const val HURDLE_GEN_DELAY_MILLIS = 1500
    }

    private val processMap = ConcurrentHashMap<String, Runner>()

    /**
     * 생성
     */
    fun generate(
        runnerPlayerHeight: Int,
        runnerPlayerWidth: Int,
        runnerPlayerX: Float,
        floorY: Float,
        startX: Float,
        endX: Float,
    ): RunnerVo {
        val runnerPlayer = RunnerPlayer(
            height = runnerPlayerHeight,
            width = runnerPlayerWidth,
            gravity = GRAVITY,
            initY = floorY,
            initX = runnerPlayerX,
            initSpeed = PLAYER_SPEED,
            initFrame = 0.2f,
        )
        val runner = Runner(
            isProcess = false,
            isStart = false,
            timeMillis = 0L,
            score = 0,
            runnerPlayer = runnerPlayer,
            floorY = floorY,
            startX = startX,
            endX = endX
        )

        processMap[runner.runnerId] = runner

        return RunnerVo.of(runner)
    }

    /**
     * 시작
     */
    fun start(runnerId: String): Flow<RunnerVo> = flow {
        processMap[runnerId]?.let {
            it.start()

            var totalHurdleCount = 0L

            while(it.isProcess) {

                val cycleMillis = 1000L / FRAME
                val cycleStart = LocalDateTime.now()

                it.increaseTimeMillis(cycleMillis)

                // 물체 이동
                it.movePlayerAndHurdles()

                // 충돌 체크
                if (it.verifyCollision()) {
                    stop(runnerId = it.runnerId)
                }

                // 스코어 체크
                it.getUnderPlayerHurdles().forEach { runnerHurdle ->
                    if (!runnerHurdle.isContainedScore) {
                        runnerHurdle.checkContainedScore()
                        it.increaseScore()
                    }
                }

                // 지나간 장애물 정리
                it.clearHurdle()

                // 장애물 생성
                if (it.timeMillis / HURDLE_GEN_DELAY_MILLIS > totalHurdleCount) {
                    val speed = HURDLE_SPEED + (totalHurdleCount / 5 * 0.125f)
                    it.generateHurdle(speed = speed)
                    totalHurdleCount = it.timeMillis / HURDLE_GEN_DELAY_MILLIS
                }

                delay(0L.coerceAtLeast(cycleMillis - Duration.between(cycleStart, LocalDateTime.now()).toMillis()))

                emit(RunnerVo.of(it))
            }

            // 게임 끝 -> 점프 중인 경우 지면에 도착할 때까지 프레임 진행
            while (it.runnerPlayer.isJump) {

                val cycleMillis = 1000L / FRAME
                val cycleStart = LocalDateTime.now()

                // 물체 이동
                it.movePlayerAndHurdles()

                delay(0L.coerceAtLeast(cycleMillis - Duration.between(cycleStart, LocalDateTime.now()).toMillis()))

                emit(RunnerVo.of(it))
            }
        }
    }

    /**
     * 종료
     */
    fun stop(runnerId: String) {
        processMap[runnerId]?.let {
            it.stop()
            processMap.remove(it.runnerId)
        }
    }

    /**
     * 점프
     */
    fun jump(runnerId: String) {
        processMap[runnerId]?.runnerPlayer?.jump()
    }
}