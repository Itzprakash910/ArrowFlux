package com.example.game.classic

import com.example.model.Arrow2D
import com.example.model.Direction
import com.example.model.SpecialType
import kotlin.random.Random

data class ClassicLevel(
    val id: Int,
    val gridSize: Int,
    val arrows: List<Arrow2D>,
    val name: String,
    val difficulty: Int // 1 to 10
)

object ClassicSolver {

    /**
     * Checks if arrow's straight escape path towards the edge is clear of any other arrow.
     */
    fun isArrowFree(arrow: Arrow2D, allArrows: List<Arrow2D>, gridSize: Int): Boolean {
        var cx = arrow.x + arrow.direction.dx
        var cy = arrow.y + arrow.direction.dy

        while (cx in 0 until gridSize && cy in 0 until gridSize) {
            if (allArrows.any { it.id != arrow.id && it.x == cx && it.y == cy }) {
                return false // Blocked by another arrow
            }
            cx += arrow.direction.dx
            cy += arrow.direction.dy
        }
        return true
    }

    /**
     * Finds the obstacle blocking the given arrow, if any.
     */
    fun findBlockingArrow(arrow: Arrow2D, allArrows: List<Arrow2D>, gridSize: Int): Arrow2D? {
        var cx = arrow.x + arrow.direction.dx
        var cy = arrow.y + arrow.direction.dy

        while (cx in 0 until gridSize && cy in 0 until gridSize) {
            val blocker = allArrows.find { it.id != arrow.id && it.x == cx && it.y == cy }
            if (blocker != null) return blocker
            cx += arrow.direction.dx
            cy += arrow.direction.dy
        }
        return null
    }

    /**
     * Finds all currently free arrows.
     */
    fun getFreeArrows(arrows: List<Arrow2D>, gridSize: Int): List<Arrow2D> {
        return arrows.filter { isArrowFree(it, arrows, gridSize) }
    }

    /**
     * Validates whether the board is completely solvable to empty state.
     * Returns the solution order of arrow IDs if solvable, or emptyList() if impossible.
     */
    fun solve(arrows: List<Arrow2D>, gridSize: Int): List<String> {
        val remaining = arrows.toMutableList()
        val solution = mutableListOf<String>()

        while (remaining.isNotEmpty()) {
            val freeArrow = remaining.firstOrNull { isArrowFree(it, remaining, gridSize) }
                ?: return emptyList() // Deadlock! Unsolvable.

            solution.add(freeArrow.id)
            remaining.remove(freeArrow)
        }
        return solution
    }

    /**
     * Generates a guaranteed 100% solvable level using reverse-unwinding (backward construction).
     * In reverse construction: start with empty board, insert arrows where they could freely enter
     * from outside the board, thus in forward play they will be the last to leave or vice versa.
     */
    fun generateLevel(levelId: Int, seed: Long = levelId.toLong() * 31337L + 777L): ClassicLevel {
        val rand = Random(seed)

        // Grid size scales with level: 1..20: 5x5, 21..50: 6x6, 51..80: 7x7, 81..100: 8x8
        val gridSize = when {
            levelId <= 20 -> 5
            levelId <= 50 -> 6
            levelId <= 80 -> 7
            else -> 8
        }

        val difficulty = ((levelId - 1) / 10) + 1
        val targetArrowCount = (gridSize * gridSize * 0.45).toInt() + (difficulty * 2).coerceAtMost(gridSize * 2)

        var attempts = 0
        while (attempts < 100) {
            attempts++
            val placedArrows = mutableListOf<Arrow2D>()
            val occupied = mutableSetOf<Pair<Int, Int>>()

            // Reverse construction:
            // We iteratively add an arrow that CAN reach the boundary freely from its position
            // without hitting previously placed arrows in that step.
            val steps = targetArrowCount.coerceIn(8, gridSize * gridSize - 4)
            var idCounter = 1

            for (step in 0 until steps) {
                val candidatePositions = mutableListOf<Pair<Int, Int>>()
                for (x in 0 until gridSize) {
                    for (y in 0 until gridSize) {
                        if (!occupied.contains(x to y)) {
                            candidatePositions.add(x to y)
                        }
                    }
                }
                candidatePositions.shuffle(rand)

                var placed = false
                val directions = Direction.values().filter { it != Direction.FORWARD && it != Direction.BACKWARD }.shuffled(rand)

                for (pos in candidatePositions) {
                    for (dir in directions) {
                        // Check if from pos in dir, the path to boundary is free of already placed arrows
                        var cx = pos.first + dir.dx
                        var cy = pos.second + dir.dy
                        var pathBlocked = false
                        while (cx in 0 until gridSize && cy in 0 until gridSize) {
                            if (occupied.contains(cx to cy)) {
                                pathBlocked = true
                                break
                            }
                            cx += dir.dx
                            cy += dir.dy
                        }

                        if (!pathBlocked) {
                            val arrow = Arrow2D(
                                id = "c_${levelId}_$idCounter",
                                x = pos.first,
                                y = pos.second,
                                direction = dir,
                                colorIndex = rand.nextInt(4),
                                special = if (levelId > 25 && rand.nextFloat() < 0.15f) SpecialType.ROTATING else SpecialType.NONE
                            )
                            placedArrows.add(arrow)
                            occupied.add(pos)
                            idCounter++
                            placed = true
                            break
                        }
                    }
                    if (placed) break
                }
            }

            // Shuffle placement order, then verify solvability forward
            val solution = solve(placedArrows, gridSize)
            if (solution.isNotEmpty() && placedArrows.size >= 6) {
                return ClassicLevel(
                    id = levelId,
                    gridSize = gridSize,
                    arrows = placedArrows,
                    name = "Level $levelId",
                    difficulty = difficulty
                )
            }
        }

        // Fallback guaranteed template if random generation took > 100 attempts
        val fallbackArrows = mutableListOf<Arrow2D>()
        fallbackArrows.add(Arrow2D("c_${levelId}_1", 0, 0, Direction.UP))
        fallbackArrows.add(Arrow2D("c_${levelId}_2", 1, 0, Direction.UP))
        fallbackArrows.add(Arrow2D("c_${levelId}_3", 0, 1, Direction.LEFT))
        fallbackArrows.add(Arrow2D("c_${levelId}_4", 1, 1, Direction.RIGHT))
        fallbackArrows.add(Arrow2D("c_${levelId}_5", 2, 2, Direction.DOWN))
        fallbackArrows.add(Arrow2D("c_${levelId}_6", 3, 3, Direction.RIGHT))

        return ClassicLevel(
            id = levelId,
            gridSize = 5,
            arrows = fallbackArrows,
            name = "Level $levelId",
            difficulty = 1
        )
    }

    /**
     * Pre-generates all 100 validated levels with deterministic caching.
     */
    private val levelCache = mutableMapOf<Int, ClassicLevel>()

    fun getLevel(levelId: Int): ClassicLevel {
        return levelCache.getOrPut(levelId) {
            generateLevel(levelId)
        }
    }
}
