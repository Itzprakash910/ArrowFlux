package com.example.game.maze

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.haptics.HapticManager
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrowMazeScreen(
    levelId: Int,
    theme: GameTheme,
    playerData: PlayerData,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    prefsManager: PreferencesManager,
    onBack: () -> Unit,
    onNextLevel: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentLevel by remember(levelId) { mutableStateOf(MazeSolver.getLevel(levelId)) }
    var arrows by remember(levelId) { mutableStateOf(currentLevel.arrows) }
    var undoStack by remember(levelId) { mutableStateOf(listOf<List<Arrow2D>>()) }
    var mistakes by remember(levelId) { mutableStateOf(0) }
    var hintsUsed by remember(levelId) { mutableStateOf(0) }
    var hintedArrowId by remember(levelId) { mutableStateOf<String?>(null) }
    var shakingArrowId by remember(levelId) { mutableStateOf<String?>(null) }
    var isLevelCompleted by remember(levelId) { mutableStateOf(false) }
    var isGameOver by remember(levelId) { mutableStateOf(false) }
    var starsAwarded by remember(levelId) { mutableStateOf(3) }
    var escapingArrows by remember(levelId) { mutableStateOf(mapOf<String, Float>()) }
    var mazeParticles by remember(levelId) { mutableStateOf(listOf<MazeParticle>()) }

    fun handleArrowTap(arrow: Arrow2D) {
        if (isLevelCompleted || isGameOver || escapingArrows.containsKey(arrow.id)) return

        val canEscape = MazeSolver.canArrowEscape(arrow, arrows, currentLevel.cells, currentLevel.width, currentLevel.height)
        if (canEscape) {
            soundManager.playMove()
            hapticManager.vibrateEscape()
            undoStack = undoStack + listOf(arrows)
            hintedArrowId = null

            // Spawn neon particles
            val newP = (0..10).map {
                val pAngle = Math.random() * 2 * Math.PI
                val pSpeed = 60f + Math.random().toFloat() * 120f
                MazeParticle(
                    x = arrow.x.toFloat(),
                    y = arrow.y.toFloat(),
                    vx = (cos(pAngle) * pSpeed).toFloat(),
                    vy = (sin(pAngle) * pSpeed).toFloat(),
                    color = Color(0xFF00FF87),
                    maxLife = 350L,
                    startTime = System.currentTimeMillis()
                )
            }
            mazeParticles = mazeParticles + newP

            coroutineScope.launch {
                val startTime = System.currentTimeMillis()
                val duration = 300L
                while (true) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                    val smoothProg = progress * progress * (3f - 2f * progress)
                    escapingArrows = escapingArrows + (arrow.id to smoothProg)
                    if (progress >= 1f) break
                    delay(16)
                }
                escapingArrows = escapingArrows - arrow.id
                arrows = arrows.filter { it.id != arrow.id }

                if (arrows.isEmpty()) {
                    val finalStars = when {
                        mistakes == 0 && hintsUsed == 0 -> 3
                        mistakes <= 1 -> 2
                        else -> 1
                    }
                    starsAwarded = finalStars
                    val coinsEarned = 25 + (finalStars * 15)
                    prefsManager.completeLevel(GameMode.MAZE, levelId, finalStars, coinsEarned)
                    soundManager.playLevelComplete()
                    hapticManager.vibrateComplete()
                    isLevelCompleted = true
                }
            }
        } else {
            soundManager.playBlocked()
            hapticManager.vibrateBlocked()
            mistakes++
            shakingArrowId = arrow.id
            coroutineScope.launch {
                delay(300)
                if (shakingArrowId == arrow.id) shakingArrowId = null
            }
            val lifeLeft = prefsManager.consumeLife()
            if (!lifeLeft || playerData.lives <= 1) {
                isGameOver = true
            }
        }
    }

    fun applyHint() {
        val free = arrows.firstOrNull {
            MazeSolver.canArrowEscape(it, arrows, currentLevel.cells, currentLevel.width, currentLevel.height)
        }
        if (free != null && prefsManager.consumeHint()) {
            hintsUsed++
            soundManager.playHint()
            hintedArrowId = free.id
        }
    }

    fun applyUndo() {
        if (undoStack.isNotEmpty() && prefsManager.consumeUndo()) {
            soundManager.playUndo()
            val previous = undoStack.last()
            undoStack = undoStack.dropLast(1)
            arrows = previous
        }
    }

    Scaffold(
        containerColor = theme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = currentLevel.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Navigate corridors to exit", color = theme.glow.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("maze_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.surface)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        repeat(3) { index ->
                            Icon(
                                imageVector = if (index < playerData.lives) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Life",
                                tint = if (index < playerData.lives) Color(0xFFFF2A6D) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.surface)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${playerData.coins}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = theme.background)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.94f))
                        .border(1.2.dp, Color(0xFF00FF87).copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            arrows = currentLevel.arrows
                            undoStack = emptyList()
                            hintedArrowId = null
                            soundManager.playButton()
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White, modifier = Modifier.size(22.dp))
                    }

                    Button(
                        onClick = { applyUndo() },
                        enabled = undoStack.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            disabledContainerColor = Color(0xFF141C2E)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .border(1.dp, if (undoStack.isNotEmpty()) Color(0xFF00FF87).copy(alpha = 0.6f) else Color.Transparent, RoundedCornerShape(14.dp))
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo", tint = if (undoStack.isNotEmpty()) Color(0xFF00FF87) else Color.DarkGray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Undo (${playerData.undos})",
                            color = if (undoStack.isNotEmpty()) Color.White else Color.DarkGray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = { applyHint() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87)),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hint (${playerData.hints})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            val width = currentLevel.width
            val height = currentLevel.height

            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .aspectRatio(1f)
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(theme.surface)
                    .pointerInput(arrows, escapingArrows) {
                        detectTapGestures { offset ->
                            val cellW = size.width / width
                            val cellH = size.height / height
                            val cx = (offset.x / cellW).toInt()
                            val cy = (offset.y / cellH).toInt()

                            val arrow = arrows.find { it.x == cx && it.y == cy }
                            if (arrow != null) {
                                handleArrowTap(arrow)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellW = size.width / width
                    val cellH = size.height / height

                    // Draw Maze Cells and Walls
                    currentLevel.cells.forEach { cell ->
                        val left = cell.x * cellW
                        val top = cell.y * cellH
                        val right = left + cellW
                        val bottom = top + cellH

                        // Exit glow
                        if (cell.isExit) {
                            drawRect(
                                color = Color(0xFF00FF87).copy(alpha = 0.18f),
                                topLeft = Offset(left, top),
                                size = androidx.compose.ui.geometry.Size(cellW, cellH)
                            )
                        }

                        val wallColor = theme.gridLine.copy(alpha = 0.9f)
                        val wallThickness = 3.5.dp.toPx()

                        if (cell.topWall) {
                            drawLine(wallColor, Offset(left, top), Offset(right, top), strokeWidth = wallThickness)
                        }
                        if (cell.bottomWall) {
                            drawLine(wallColor, Offset(left, bottom), Offset(right, bottom), strokeWidth = wallThickness)
                        }
                        if (cell.leftWall) {
                            drawLine(wallColor, Offset(left, top), Offset(left, bottom), strokeWidth = wallThickness)
                        }
                        if (cell.rightWall) {
                            drawLine(wallColor, Offset(right, top), Offset(right, bottom), strokeWidth = wallThickness)
                        }
                    }

                    // Render arrows
                    arrows.forEach { arrow ->
                        val isHinted = arrow.id == hintedArrowId
                        val isShaking = arrow.id == shakingArrowId
                        val escapeProgress = escapingArrows[arrow.id] ?: 0f

                        val shakeOffset = if (isShaking) (sin(System.currentTimeMillis() * 0.05f) * 6.dp.toPx()) else 0f
                        var baseX = arrow.x * cellW + cellW / 2 + shakeOffset
                        var baseY = arrow.y * cellH + cellH / 2

                        if (escapeProgress > 0f) {
                            val travel = size.width * 1.5f * escapeProgress
                            baseX += arrow.direction.dx * travel
                            baseY += arrow.direction.dy * travel
                        }

                        drawMazeArrow(
                            arrow = arrow,
                            center = Offset(baseX, baseY),
                            radius = cellW * 0.35f,
                            primaryColor = if (isHinted) theme.primary else theme.accent,
                            isEscaping = escapeProgress > 0f
                        )
                    }

                    // Render particles
                    val now = System.currentTimeMillis()
                    mazeParticles = mazeParticles.filter { now - it.startTime < it.maxLife }
                    mazeParticles.forEach { p ->
                        val pAge = (now - p.startTime).toFloat() / p.maxLife.toFloat()
                        val pAlpha = (1f - pAge).coerceIn(0f, 1f)
                        val px = p.x * cellW + cellW / 2 + (p.vx * pAge * 0.35f)
                        val py = p.y * cellH + cellH / 2 + (p.vy * pAge * 0.35f)
                        drawCircle(
                            color = p.color.copy(alpha = pAlpha),
                            radius = (3.5f * (1f - pAge * 0.5f)).dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }
            }

            if (isLevelCompleted) {
                com.example.ui.components.LevelCompleteDialog(
                    stars = starsAwarded,
                    moves = currentLevel.arrows.size - arrows.size + mistakes,
                    mistakes = mistakes,
                    hintsUsed = hintsUsed,
                    coinsEarned = 50,
                    onNextLevel = {
                        isLevelCompleted = false
                        onNextLevel(levelId + 1)
                    },
                    onReplay = {
                        isLevelCompleted = false
                        arrows = currentLevel.arrows
                        undoStack = emptyList()
                        hintedArrowId = null
                        mistakes = 0
                    },
                    onLevelSelect = onBack
                )
            }
        }
    }
}

private fun DrawScope.drawMazeArrow(
    arrow: Arrow2D,
    center: Offset,
    radius: Float,
    primaryColor: Color,
    isEscaping: Boolean
) {
    val angle = Math.toRadians(arrow.direction.angleDegrees.toDouble())

    drawCircle(
        color = primaryColor.copy(alpha = if (isEscaping) 0.5f else 0.15f),
        radius = radius * 1.25f,
        center = center
    )
    drawCircle(color = Color(0xFF131D33), radius = radius, center = center)
    drawCircle(color = primaryColor, radius = radius, center = center, style = Stroke(width = 2.dp.toPx()))

    val cosA = cos(angle).toFloat()
    val sinA = sin(angle).toFloat()
    val tip = Offset(center.x + radius * 0.75f * cosA, center.y + radius * 0.75f * sinA)
    val base = Offset(center.x - radius * 0.6f * cosA, center.y - radius * 0.6f * sinA)

    drawLine(primaryColor, base, tip, strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)

    val wingLen = radius * 0.45f
    val wingAngle = Math.toRadians(35.0)
    val leftWing = Offset(
        tip.x + wingLen * cos(angle + Math.PI - wingAngle).toFloat(),
        tip.y + wingLen * sin(angle + Math.PI - wingAngle).toFloat()
    )
    val rightWing = Offset(
        tip.x + wingLen * cos(angle + Math.PI + wingAngle).toFloat(),
        tip.y + wingLen * sin(angle + Math.PI + wingAngle).toFloat()
    )

    val path = Path().apply {
        moveTo(leftWing.x, leftWing.y)
        lineTo(tip.x, tip.y)
        lineTo(rightWing.x, rightWing.y)
    }
    drawPath(path, primaryColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
}

data class MazeParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val maxLife: Long,
    val startTime: Long
)
