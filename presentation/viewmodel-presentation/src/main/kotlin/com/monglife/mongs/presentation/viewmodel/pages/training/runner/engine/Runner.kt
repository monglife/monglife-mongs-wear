package com.monglife.mongs.presentation.viewmodel.pages.training.runner.engine

import java.util.UUID
import kotlin.math.sqrt

data class Runner(
    val runnerId: String = UUID.randomUUID().toString(),
    var isProcess: Boolean,
    var timeMillis: Long,
    var score: Int,
    val runnerHurdles: ArrayList<RunnerHurdle> = ArrayList(),
    val runnerPlayer: RunnerPlayer,
    private val floorY: Float,
    private val startX: Float,
    private val endX: Float,
) {
    companion object {
        private const val COLLISION_PADDING = 10
    }

    /**
     * 게임 시작
     */
    fun start() {
        this.isProcess = true
    }

    /**
     * 게임 종료
     */
    fun stop() {
        this.isProcess = false
    }

    /**
     * 게임 시간 동기화
     */
    fun increaseTimeMillis(timeMillis: Long) {
        this.timeMillis += timeMillis
    }

    /**
     * 스코어 증가
     */
    fun increaseScore() {
        this.score += 1;
    }

    /**
     * 플레이어 & 장애물 이동
     */
    fun movePlayerAndHurdles() {
        // 플레이어 이동
        runnerPlayer.move()

        // 장애물 이동
        for (runnerHurdle in runnerHurdles) {
            runnerHurdle.move()
        }
    }

    /**
     * 장애물 생성
     */
    fun generateHurdle(speed: Float) {
        runnerHurdles.add(
            RunnerHurdle(
                height = 30,
                width = 40,
                initY = floorY,
                initX = endX,
                speed = speed,
            )
        )
    }

    /**
     * 장애물 정리
     */
    fun clearHurdle() {
        val it = runnerHurdles.iterator()

        while (it.hasNext()) {
            val runnerHurdle = it.next()
            if (runnerHurdle.px < startX) {
                it.remove()
            }
        }
    }

    /**
     * 충돌 검증
     */
    fun verifyCollision(): Boolean {
        for (runnerHurdle in runnerHurdles) {
            if (isCollision(runnerPlayer = runnerPlayer, runnerHurdle = runnerHurdle)) {
                return true
            }
        }
        return false
    }

    /**
     * 플레이어 아래 장애물 목록 조회
     */
    fun getUnderPlayerHurdles(): List<RunnerHurdle> {
        return runnerHurdles.filter { isUnder(runnerPlayer = runnerPlayer, runnerHurdle = it) }
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