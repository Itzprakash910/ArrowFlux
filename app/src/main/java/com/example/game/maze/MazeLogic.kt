package com.example.game.maze

import com.example.model.Arrow2D
import com.example.model.Direction
import com.example.model.MazeCell
import com.example.model.SpecialType
import kotlin.random.Random

data class MazeLevel(
    val id: Int,
    val width: Int,
    val height: Int,
    val cells: List<MazeCell>,
    val arrows: List<Arrow2D>,
    val name: String,
    val difficulty: Int
)

object MazeSolver {

    /**
     * Checks if an arrow inside a maze can escape.
     * In maze mode, an arrow travels in its direction until:
     * - It hits a cell wall (blocked)
     * - It hits another arrow (blocked)
     * - It reaches a cell with an exit in that direction or walks off the open border (escaped!)
     */
    fun canArrowEscape(arrow: Arrow2D, allArrows: List<Arrow2D>, cells: List<MazeCell>, width: Int, height: Int): Boolean {
        var cx = arrow.x
        var cy = arrow.y
        val dir = arrow.direction

        while (true) {
            val cell = cells.find { it.x == cx && it.y == cy } ?: return false

            // Check if there is a wall in current cell in moving direction
            val hasWall = when (dir) {
                Direction.UP -> cell.topWall
                Direction.DOWN -> cell.bottomWall
                Direction.LEFT -> cell.leftWall
                Direction.RIGHT -> cell.rightWall
                else -> false
            }

            // Check if current cell has an exit in this direction
            if (cell.isExit && (cell.exitDirection == null || cell.exitDirection == dir)) {
                return true
            }

            if (hasWall) {
                return false // Blocked by wall
            }

            val nextX = cx + dir.dx
            val nextY = cy + dir.dy

            // Escapes off boundary if no border wall
            if (nextX !in 0 until width || nextY !in 0 until height) {
                return true
            }

            // Blocked by another arrow in the way
            if (allArrows.any { it.id != arrow.id && it.x == nextX && it.y == nextY }) {
                return false
            }

            cx = nextX
            cy = nextY
        }
    }

    /**
     * Validates whether a maze level can be completely solved.
     */
    fun solve(arrows: List<Arrow2D>, cells: List<MazeCell>, width: Int, height: Int): List<String> {
        val remaining = arrows.toMutableList()
        val solution = mutableListOf<String>()

        while (remaining.isNotEmpty()) {
            val freeArrow = remaining.firstOrNull { canArrowEscape(it, remaining, cells, width, height) }
                ?: return emptyList()

            solution.add(freeArrow.id)
            remaining.remove(freeArrow)
        }
        return solution
    }

    /**
     * Generates a deterministic maze level (100 levels).
     */
    fun generateLevel(levelId: Int, seed: Long = levelId * 43981L + 1234L): MazeLevel {
        val rand = Random(seed)
        val size = when {
            levelId <= 25 -> 5
            levelId <= 60 -> 6
            else -> 7
        }
        val width = size
        val height = size
        val difficulty = ((levelId - 1) / 10) + 1

        var attempts = 0
        while (attempts < 80) {
            attempts++
            // Create cells with default borders
            val cellList = mutableListOf<MazeCell>()
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val isBorderTop = (y == 0)
                    val isBorderBottom = (y == height - 1)
                    val isBorderLeft = (x == 0)
                    val isBorderRight = (x == width - 1)

                    // Some interior walls based on seed
                    val hasTopWall = isBorderTop || (rand.nextFloat() < 0.28f && y > 0)
                    val hasLeftWall = isBorderLeft || (rand.nextFloat() < 0.28f && x > 0)

                    // Designate some exits at borders
                    val isExit = (isBorderTop && x % 2 == 1) || (isBorderBottom && x % 2 == 0) || (isBorderRight && y % 2 == 1)

                    cellList.add(
                        MazeCell(
                            x = x,
                            y = y,
                            topWall = hasTopWall,
                            bottomWall = isBorderBottom,
                            leftWall = hasLeftWall,
                            rightWall = isBorderRight,
                            isExit = isExit,
                            exitDirection = when {
                                isBorderTop && isExit -> Direction.UP
                                isBorderBottom && isExit -> Direction.DOWN
                                isBorderRight && isExit -> Direction.RIGHT
                                else -> null
                            }
                        )
                    )
                }
            }

            // Sync opposite walls
            val syncedCells = cellList.map { cell ->
                val neighborAbove = cellList.find { it.x == cell.x && it.y == cell.y - 1 }
                val neighborBelow = cellList.find { it.x == cell.x && it.y == cell.y + 1 }
                val neighborLeft = cellList.find { it.x == cell.x - 1 && it.y == cell.y }
                val neighborRight = cellList.find { it.x == cell.x + 1 && it.y == cell.y }

                cell.copy(
                    bottomWall = neighborBelow?.topWall ?: cell.bottomWall,
                    rightWall = neighborRight?.leftWall ?: cell.rightWall
                )
            }

            // Place arrows
            val arrowCount = (size * 2 + difficulty).coerceIn(6, size * size - 4)
            val availablePositions = mutableListOf<Pair<Int, Int>>()
            for (x in 0 until width) {
                for (y in 0 until height) {
                    availablePositions.add(x to y)
                }
            }
            availablePositions.shuffle(rand)

            val arrows = mutableListOf<Arrow2D>()
            var id = 1
            for (pos in availablePositions.take(arrowCount)) {
                val dir = listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT).random(rand)
                arrows.add(
                    Arrow2D(
                        id = "m_${levelId}_$id",
                        x = pos.first,
                        y = pos.second,
                        direction = dir,
                        colorIndex = (id % 4),
                        special = if (levelId > 30 && rand.nextFloat() < 0.12f) SpecialType.ROTATING else SpecialType.NONE
                    )
                )
                id++
            }

            val solution = solve(arrows, syncedCells, width, height)
            if (solution.isNotEmpty() && arrows.size >= 5) {
                return MazeLevel(
                    id = levelId,
                    width = width,
                    height = height,
                    cells = syncedCells,
                    arrows = arrows,
                    name = "Maze $levelId",
                    difficulty = difficulty
                )
            }
        }

        // Guaranteed fallback solvable maze
        val fallbackCells = mutableListOf<MazeCell>()
        for (y in 0 until 5) {
            for (x in 0 until 5) {
                fallbackCells.add(
                    MazeCell(
                        x = x,
                        y = y,
                        topWall = y == 0,
                        bottomWall = y == 4,
                        leftWall = x == 0,
                        rightWall = x == 4,
                        isExit = (x == 2 && y == 0) || (x == 4 && y == 2)
                    )
                )
            }
        }
        val fallbackArrows = listOf(
            Arrow2D("m_${levelId}_1", 2, 1, Direction.UP),
            Arrow2D("m_${levelId}_2", 3, 2, Direction.RIGHT),
            Arrow2D("m_${levelId}_3", 1, 2, Direction.RIGHT),
            Arrow2D("m_${levelId}_4", 2, 3, Direction.UP),
            Arrow2D("m_${levelId}_5", 0, 0, Direction.RIGHT)
        )

        return MazeLevel(
            id = levelId,
            width = 5,
            height = 5,
            cells = fallbackCells,
            arrows = fallbackArrows,
            name = "Maze $levelId",
            difficulty = 1
        )
    }

    private val levelCache = mutableMapOf<Int, MazeLevel>()

    fun getLevel(levelId: Int): MazeLevel {
        return levelCache.getOrPut(levelId) {
            generateLevel(levelId)
        }
    }
}
