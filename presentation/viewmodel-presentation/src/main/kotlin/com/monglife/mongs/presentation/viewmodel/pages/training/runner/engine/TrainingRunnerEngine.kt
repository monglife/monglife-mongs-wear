package com.monglife.mongs.presentation.viewmodel.pages.training.runner.engine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.sqrt

class TrainingRunnerEngine {

    companion object {
        private const val GRAVITY = 9.8f
        private const val FRAME = 60L
        private const val COLLISION_PADDING = 10
        private const val HURDLE_GENERATE_DELAY_MILLIS = 1500

        private const val PLAYER_START_X = 24f
        private const val PLAYER_SPEED = 42f

        private const val HURDLE_START_X = 250f
        private const val HURDLE_SPEED = 3f
    }

    private val runnerMap = ConcurrentHashMap<String, MutableStateFlow<Runner>>()

    /**
     * 생성
     */
    fun generateRunner(
        runnerPlayerHeight: Int,
        runnerPlayerWidth: Int,
        runnerPlayerX: Float,
        floorY: Float,
    ): String {
        val runnerPlayer = RunnerPlayer(
            height = runnerPlayerHeight,
            width = runnerPlayerWidth,
            gravity = GRAVITY,
            startY = floorY,
            startX = runnerPlayerX,
            startSpeed = PLAYER_SPEED,
            startFrame = 0.2f,
        )
        val runner = Runner(
            isProcess = true,
            score = 0,
            runnerHurdles = emptyList(),
            runnerPlayer = runnerPlayer
        )

        val runnerStateFlow = MutableStateFlow(runner)

        runnerMap.put(runner.runnerId, runnerStateFlow)

        return runner.runnerId
    }

    fun start(): Flow<Runner> = flow {

    }

    fun end() {

    }

    /**
     * 충돌 여부 확인
     */
    private fun isCollision(runnerPlayer: RunnerPlayer, runnerHurdle: RunnerHurdle) : Boolean {

        val playerMinY = runnerPlayer.py - COLLISION_PADDING + runnerPlayer.height
        val playerMaxY = runnerPlayer.py + COLLISION_PADDING
        val playerMinX = runnerPlayer.px + COLLISION_PADDING
        val playerMaxX = runnerPlayer.px - COLLISION_PADDING + runnerPlayer.width
        val hurdleMinY = runnerHurdle.py - COLLISION_PADDING + runnerHurdle.height
        val hurdleMaxY = runnerHurdle.py + COLLISION_PADDING
        val hurdleMinX = runnerHurdle.px + COLLISION_PADDING
        val hurdleMaxX = runnerHurdle.px - COLLISION_PADDING + runnerHurdle.width


        val playerRect = listOf(
            Point(playerMinX, playerMinY),
            Point(playerMinX, playerMaxY),
            Point(playerMaxX, playerMaxY),
            Point(playerMaxX, playerMinY),
        )

        val hurdleRect = listOf(
            Point(hurdleMinX, hurdleMinY),
            Point(hurdleMinX, hurdleMaxY),
            Point(hurdleMaxX, hurdleMaxY),
            Point(hurdleMaxX, hurdleMinY),
        )

        val axes = mutableListOf<Point>()

        listOf(playerRect, hurdleRect).forEach { rect ->
            for (i in rect.indices) {
                val p1 = rect[i]
                val p2 = rect[(i + 1) % rect.size]
                val edge = Point(p2.x - p1.x, p2.y - p1.y)
                val normal = Point(-edge.y, edge.x)
                axes.add(normalize(normal))
            }
        }

        for (axis in axes) {
            val (minA, maxA) = projectPolygon(playerRect, axis)
            val (minB, maxB) = projectPolygon(hurdleRect, axis)
            if (!overlap(minA, maxA, minB, maxB)) {
                return false
            }
        }

        return true
    }

    /**
     * 장애물 아래 여부 확인
     */
    private fun isUnder(runnerPlayer: RunnerPlayer, runnerHurdle: RunnerHurdle) : Boolean {

        val playerMinX = runnerPlayer.px + COLLISION_PADDING
        val playerMaxX = runnerPlayer.px - COLLISION_PADDING + runnerPlayer.width

        val hurdleMinX = runnerHurdle.px + COLLISION_PADDING
        val hurdleMaxX = runnerHurdle.px - COLLISION_PADDING + runnerHurdle.width

        return (hurdleMinX in playerMinX..playerMaxX) || (hurdleMaxX in playerMinX..playerMaxX)
    }

    private data class Point(val x: Float, val y: Float)

    private fun projectPolygon(points: List<Point>, axis: Point): Pair<Float, Float> {
        val projections = points.map { point -> point.x * axis.x + point.y * axis.y }
        return projections.minOrNull()!! to projections.maxOrNull()!!
    }

    private fun overlap(minA: Float, maxA: Float, minB: Float, maxB: Float): Boolean {
        return maxA >= minB && maxB >= minA
    }

    private fun normalize(vector: Point): Point {
        val magnitude = sqrt(vector.x * vector.x + vector.y * vector.y)
        return Point(vector.x / magnitude, vector.y / magnitude)
    }
}