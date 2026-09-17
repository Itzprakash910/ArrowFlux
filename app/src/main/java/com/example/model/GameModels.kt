package com.example.model

import androidx.compose.ui.graphics.Color

enum class Direction(val dx: Int, val dy: Int, val dz: Int = 0) {
    UP(0, -1, 0),
    DOWN(0, 1, 0),
    LEFT(-1, 0, 0),
    RIGHT(1, 0, 0),
    FORWARD(0, 0, 1),
    BACKWARD(0, 0, -1);

    val opposite: Direction
        get() = when (this) {
            UP -> DOWN
            DOWN -> UP
            LEFT -> RIGHT
            RIGHT -> LEFT
            FORWARD -> BACKWARD
            BACKWARD -> FORWARD
        }

    val angleDegrees: Float
        get() = when (this) {
            UP -> 270f
            RIGHT -> 0f
            DOWN -> 90f
            LEFT -> 180f
            FORWARD -> 45f
            BACKWARD -> 225f
        }
}

enum class BlockFace {
    FRONT, BACK, TOP, BOTTOM, LEFT, RIGHT
}

enum class SpecialType {
    NONE,
    LOCKED,       // Needs key or switch to clear
    ROTATING,     // Tap to rotate 90 deg
    MOVING,       // Shifts position
    ICE,          // Arrow slides through
    PORTAL,       // Teleports to paired portal
    SWITCH,       // Activates gate
    GATE,         // Opens when switch toggled
    BREAKABLE     // Breaks after arrow passes
}

data class Arrow2D(
    val id: String,
    val x: Int,
    val y: Int,
    val direction: Direction,
    val colorIndex: Int = 0,
    val special: SpecialType = SpecialType.NONE,
    val isEscaping: Boolean = false,
    val escapeProgress: Float = 0f,
    val isShaking: Boolean = false,
    val isHinted: Boolean = false
)

data class MazeCell(
    val x: Int,
    val y: Int,
    val topWall: Boolean = true,
    val rightWall: Boolean = true,
    val bottomWall: Boolean = true,
    val leftWall: Boolean = true,
    val isExit: Boolean = false,
    val exitDirection: Direction? = null,
    val isIce: Boolean = false,
    val portalTarget: Pair<Int, Int>? = null
)

data class Arrow3D(
    val id: String,
    val blockX: Int,
    val blockY: Int,
    val blockZ: Int,
    val face: BlockFace,
    val direction: Direction, // Direction on the face or normal
    val special: SpecialType = SpecialType.NONE,
    val isEscaping: Boolean = false,
    val escapeOffset: Float = 0f,
    val isShaking: Boolean = false,
    val isHinted: Boolean = false
)

data class Block3D(
    val x: Int,
    val y: Int,
    val z: Int,
    val type: SpecialType = SpecialType.NONE,
    val isUnlocked: Boolean = true
)

enum class GameMode(val displayName: String, val subtitle: String) {
    CLASSIC("Classic Arrow", "Clear the arrows"),
    MAZE("Arrow Maze", "Find the free path"),
    THREE_D("Arrow Escape 3D", "Rotate. Think. Escape.")
}

enum class GameTheme(
    val id: String,
    val displayName: String,
    val background: Color,
    val surface: Color,
    val primary: Color,
    val accent: Color,
    val glow: Color,
    val gridLine: Color,
    val cost: Int = 0
) {
    NEON(
        id = "neon",
        displayName = "Neon Cyber",
        background = Color(0xFF070913),
        surface = Color(0xFF0F172A),
        primary = Color(0xFF00F0FF),
        accent = Color(0xFF8B5CF6),
        glow = Color(0xFF38BDF8),
        gridLine = Color(0xFF1E293B),
        cost = 0
    ),
    OCEAN(
        id = "ocean",
        displayName = "Deep Ocean",
        background = Color(0xFF031321),
        surface = Color(0xFF0A2540),
        primary = Color(0xFF00D2FF),
        accent = Color(0xFF00FF87),
        glow = Color(0xFF00B4D8),
        gridLine = Color(0xFF134E5E),
        cost = 150
    ),
    CYBER(
        id = "cyber",
        displayName = "Cyberpunk",
        background = Color(0xFF100720),
        surface = Color(0xFF1E1035),
        primary = Color(0xFFFF007F),
        accent = Color(0xFFFFE600),
        glow = Color(0xFFFF2A8D),
        gridLine = Color(0xFF311E43),
        cost = 250
    ),
    SUNSET(
        id = "sunset",
        displayName = "Solar Sunset",
        background = Color(0xFF180A0A),
        surface = Color(0xFF2A1215),
        primary = Color(0xFFFF6B4A),
        accent = Color(0xFFFFD166),
        glow = Color(0xFFFF8E53),
        gridLine = Color(0xFF4A2020),
        cost = 200
    ),
    LAVA(
        id = "lava",
        displayName = "Molten Lava",
        background = Color(0xFF1A0505),
        surface = Color(0xFF2E0C0C),
        primary = Color(0xFFFF3B30),
        accent = Color(0xFFFF9500),
        glow = Color(0xFFFF453A),
        gridLine = Color(0xFF4D1414),
        cost = 350
    ),
    FOREST(
        id = "forest",
        displayName = "Emerald Forest",
        background = Color(0xFF04140B),
        surface = Color(0xFF0A2616),
        primary = Color(0xFF34C759),
        accent = Color(0xFF30D158),
        glow = Color(0xFF32D74B),
        gridLine = Color(0xFF134526),
        cost = 250
    ),
    SPACE(
        id = "space",
        displayName = "Cosmic Void",
        background = Color(0xFF080514),
        surface = Color(0xFF140D2E),
        primary = Color(0xFFB066FF),
        accent = Color(0xFF4EE2EC),
        glow = Color(0xFFC084FC),
        gridLine = Color(0xFF2A1B54),
        cost = 300
    ),
    MINIMAL(
        id = "minimal",
        displayName = "Monochrome",
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        primary = Color(0xFFE2E8F0),
        accent = Color(0xFF94A3B8),
        glow = Color(0xFFCBD5E1),
        gridLine = Color(0xFF2D3748),
        cost = 100
    )
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val unlocked: Boolean = false,
    val rewardCoins: Int = 50
)
