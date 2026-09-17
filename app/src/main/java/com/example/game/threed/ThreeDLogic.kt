package com.example.game.threed

import com.example.model.Arrow3D
import com.example.model.Block3D
import com.example.model.BlockFace
import com.example.model.Direction
import com.example.model.SpecialType
import kotlin.random.Random

data class ThreeDLevel(
    val id: Int,
    val structureName: String,
    val blocks: List<Block3D>,
    val arrows: List<Arrow3D>,
    val difficulty: Int
)

object ThreeDSolver {

    /**
     * Checks if an arrow attached to a block face can escape outward.
     * Face normal determines escape vector.
     * For example, a FRONT face arrow points along +Z.
     * If there are other blocks directly in front (z > blockZ) at same (x, y), it's blocked.
     */
    fun canArrowEscape(arrow: Arrow3D, allArrows: List<Arrow3D>, blocks: List<Block3D>): Boolean {
        val normal = when (arrow.face) {
            BlockFace.FRONT -> Triple(0, 0, 1)
            BlockFace.BACK -> Triple(0, 0, -1)
            BlockFace.TOP -> Triple(0, 1, 0)
            BlockFace.BOTTOM -> Triple(0, -1, 0)
            BlockFace.RIGHT -> Triple(1, 0, 0)
            BlockFace.LEFT -> Triple(-1, 0, 0)
        }

        // Raycast outward from block position along normal
        var cx = arrow.blockX + normal.first
        var cy = arrow.blockY + normal.second
        var cz = arrow.blockZ + normal.third

        // Check up to 10 units away for blocking blocks
        for (i in 0 until 10) {
            if (blocks.any { it.x == cx && it.y == cy && it.z == cz }) {
                return false // Obstructed by another block
            }
            if (allArrows.any { it.id != arrow.id && it.blockX == cx && it.blockY == cy && it.blockZ == cz }) {
                return false // Obstructed by another arrow's origin
            }
            cx += normal.first
            cy += normal.second
            cz += normal.third
        }
        return true
    }

    fun solve(arrows: List<Arrow3D>, blocks: List<Block3D>): List<String> {
        val remaining = arrows.toMutableList()
        val solution = mutableListOf<String>()

        while (remaining.isNotEmpty()) {
            val freeArrow = remaining.firstOrNull { canArrowEscape(it, remaining, blocks) }
                ?: return emptyList()

            solution.add(freeArrow.id)
            remaining.remove(freeArrow)
        }
        return solution
    }

    /**
     * Generates a 3D level with specified architectural presets:
     * - Cube (2x2x2 or 3x3x3)
     * - Tower (1x3x1, 2x4x2)
     * - Staircase
     * - Cross
     * - L-Shape
     * - T-Shape
     * - Bridge
     * - Hollow Cube
     */
    fun generateLevel(levelId: Int, seed: Long = levelId * 51719L + 888L): ThreeDLevel {
        val rand = Random(seed)
        val structureType = (levelId - 1) % 10
        val blocks = mutableListOf<Block3D>()
        val structureName: String

        when (structureType) {
            0 -> { // 2x2x2 Cube
                structureName = "Compact Cube"
                for (x in 0..1) {
                    for (y in 0..1) {
                        for (z in 0..1) {
                            blocks.add(Block3D(x, y, z))
                        }
                    }
                }
            }
            1 -> { // Tower
                structureName = "Neon Tower"
                for (y in 0..3) {
                    blocks.add(Block3D(0, y, 0))
                    if (levelId > 15) blocks.add(Block3D(1, y, 0))
                }
            }
            2 -> { // Staircase
                structureName = "Staircase"
                for (x in 0..2) {
                    for (y in 0..x) {
                        blocks.add(Block3D(x, y, 0))
                        blocks.add(Block3D(x, y, 1))
                    }
                }
            }
            3 -> { // Cross (+)
                structureName = "Cross Core"
                blocks.add(Block3D(0, 0, 0))
                blocks.add(Block3D(1, 0, 0))
                blocks.add(Block3D(-1, 0, 0))
                blocks.add(Block3D(0, 1, 0))
                blocks.add(Block3D(0, -1, 0))
                blocks.add(Block3D(0, 0, 1))
                blocks.add(Block3D(0, 0, -1))
            }
            4 -> { // L-Shape
                structureName = "L-Matrix"
                for (y in 0..3) blocks.add(Block3D(0, y, 0))
                for (x in 1..2) blocks.add(Block3D(x, 0, 0))
            }
            5 -> { // T-Shape
                structureName = "T-Nexus"
                for (x in -1..1) blocks.add(Block3D(x, 2, 0))
                for (y in 0..1) blocks.add(Block3D(0, y, 0))
            }
            6 -> { // Bridge
                structureName = "Cyber Bridge"
                for (y in 0..1) blocks.add(Block3D(-1, y, 0))
                for (y in 0..1) blocks.add(Block3D(1, y, 0))
                blocks.add(Block3D(0, 1, 0))
            }
            7 -> { // Hollow Cube
                structureName = "Hollow Void"
                for (x in -1..1) {
                    for (y in -1..1) {
                        for (z in -1..1) {
                            if (x != 0 || y != 0 || z != 0) {
                                blocks.add(Block3D(x, y, z))
                            }
                        }
                    }
                }
            }
            8 -> { // Asymmetric Structure
                structureName = "Asymmetric Gate"
                blocks.add(Block3D(0, 0, 0))
                blocks.add(Block3D(1, 0, 0))
                blocks.add(Block3D(0, 1, 0))
                blocks.add(Block3D(0, 0, 1))
                blocks.add(Block3D(1, 1, 1))
            }
            else -> { // Multi-layer Structure
                structureName = "Hyper-Prism"
                for (x in 0..2) {
                    for (y in 0..1) {
                        for (z in 0..1) {
                            blocks.add(Block3D(x, y, z))
                        }
                    }
                }
            }
        }

        // Determine exterior faces of blocks
        val arrows = mutableListOf<Arrow3D>()
        var arrowId = 1
        for (block in blocks) {
            for (face in BlockFace.values()) {
                val normal = when (face) {
                    BlockFace.FRONT -> Triple(0, 0, 1)
                    BlockFace.BACK -> Triple(0, 0, -1)
                    BlockFace.TOP -> Triple(0, 1, 0)
                    BlockFace.BOTTOM -> Triple(0, -1, 0)
                    BlockFace.RIGHT -> Triple(1, 0, 0)
                    BlockFace.LEFT -> Triple(-1, 0, 0)
                }
                val neighborExists = blocks.any {
                    it.x == block.x + normal.first &&
                    it.y == block.y + normal.second &&
                    it.z == block.z + normal.third
                }

                // Place arrow on exterior face with some probability
                if (!neighborExists && rand.nextFloat() < 0.45f) {
                    arrows.add(
                        Arrow3D(
                            id = "3d_${levelId}_$arrowId",
                            blockX = block.x,
                            blockY = block.y,
                            blockZ = block.z,
                            face = face,
                            direction = when (face) {
                                BlockFace.FRONT, BlockFace.BACK -> Direction.UP
                                BlockFace.TOP, BlockFace.BOTTOM -> Direction.FORWARD
                                BlockFace.LEFT, BlockFace.RIGHT -> Direction.UP
                            }
                        )
                    )
                    arrowId++
                }
            }
        }

        // Guarantee at least 6 arrows
        if (arrows.size < 6) {
            for (b in blocks.take(6)) {
                arrows.add(
                    Arrow3D(
                        id = "3d_${levelId}_${arrows.size + 1}",
                        blockX = b.x,
                        blockY = b.y,
                        blockZ = b.z,
                        face = BlockFace.FRONT,
                        direction = Direction.UP
                    )
                )
            }
        }

        val difficulty = ((levelId - 1) / 5) + 1

        return ThreeDLevel(
            id = levelId,
            structureName = "$structureName $levelId",
            blocks = blocks,
            arrows = arrows.distinctBy { "${it.blockX}_${it.blockY}_${it.blockZ}_${it.face}" },
            difficulty = difficulty
        )
    }

    private val levelCache = mutableMapOf<Int, ThreeDLevel>()

    fun getLevel(levelId: Int): ThreeDLevel {
        return levelCache.getOrPut(levelId) {
            generateLevel(levelId)
        }
    }
}
