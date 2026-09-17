package com.example.game.classic

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
fun ClassicArrowScreen(
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
    var currentLevel by remember(levelId) { mutableStateOf(ClassicSolver.getLevel(levelId)) }
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
    var particles by remember(levelId) { mutableStateOf(listOf<ParticleEffect>()) }

    // Pulsing animation for hints
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val hintPulse by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintPulse"
    )

    fun handleArrowTap(arrow: Arrow2D) {
        if (isLevelCompleted || isGameOver || escapingArrows.containsKey(arrow.id)) return

        val isFree = ClassicSolver.isArrowFree(arrow, arrows, currentLevel.gridSize)
        if (isFree) {
            soundManager.playMove()
            hapticManager.vibrateEscape()
            undoStack = undoStack + listOf(arrows)
            hintedArrowId = null

            // Spawn neon burst particles around escaping arrow
            val newParticles = (0..12).map {
                val pAngle = Math.random() * 2 * Math.PI
                val pSpeed = (80f + Math.random().toFloat() * 140f)
                ParticleEffect(
                    x = arrow.x.toFloat(),
                    y = arrow.y.toFloat(),
                    vx = (cos(pAngle) * pSpeed).toFloat(),
                    vy = (sin(pAngle) * pSpeed).toFloat(),
                    color = if (Math.random() > 0.5) theme.primary else theme.accent,
                    maxLife = 350L,
                    startTime = System.currentTimeMillis()
                )
            }
            particles = particles + newParticles

            // Animate arrow outward with smooth acceleration
            coroutineScope.launch {
                val startTime = System.currentTimeMillis()
                val duration = 280L
                while (true) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                    val smoothProgress = progress * progress * (3f - 2f * progress) // smoothstep
                    escapingArrows = escapingArrows + (arrow.id to smoothProgress)
                    if (progress >= 1f) break
                    delay(16)
                }
                escapingArrows = escapingArrows - arrow.id
                arrows = arrows.filter { it.id != arrow.id }

                // Check victory condition
                if (arrows.isEmpty()) {
                    val finalStars = when {
                        mistakes == 0 && hintsUsed == 0 -> 3
                        mistakes <= 1 -> 2
                        else -> 1
                    }
                    starsAwarded = finalStars
                    val coinsEarned = 20 + (finalStars * 15)
                    prefsManager.completeLevel(GameMode.CLASSIC, levelId, finalStars, coinsEarned)
                    soundManager.playLevelComplete()
                    hapticManager.vibrateComplete()
                    isLevelCompleted = true
                }
            }
        } else {
            // Blocked!
            soundManager.playBlocked()
            hapticManager.vibrateBlocked()
            mistakes++
            shakingArrowId = arrow.id
            coroutineScope.launch {
                delay(300)
                if (shakingArrowId == arrow.id) shakingArrowId = null
            }

            // Consume life if strict
            val lifeLeft = prefsManager.consumeLife()
            if (!lifeLeft || playerData.lives <= 1) {
                isGameOver = true
            }
        }
    }

    fun applyHint() {
        val free = ClassicSolver.getFreeArrows(arrows, currentLevel.gridSize)
        if (free.isNotEmpty()) {
            val target = free.first()
            if (prefsManager.consumeHint()) {
                hintsUsed++
                soundManager.playHint()
                hintedArrowId = target.id
            }
        }
    }

    fun applyUndo() {
        if (undoStack.isNotEmpty()) {
            if (prefsManager.consumeUndo()) {
                soundManager.playUndo()
                val previous = undoStack.last()
                undoStack = undoStack.dropLast(1)
                arrows = previous
            }
        }
    }

    Scaffold(
        containerColor = theme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentLevel.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${arrows.size} arrows remaining",
                            color = theme.glow.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Lives / Hearts
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
                    // Coins
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
                        .border(1.2.dp, theme.primary.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Restart Button
                    IconButton(
                        onClick = {
                            arrows = currentLevel.arrows
                            undoStack = emptyList()
                            hintedArrowId = null
                            mistakes = 0
                            soundManager.playButton()
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .size(44.dp)
                            .testTag("restart_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White, modifier = Modifier.size(22.dp))
                    }

                    // Undo Button
                    Button(
                        onClick = { applyUndo() },
                        enabled = undoStack.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E293B),
                            disabledContainerColor = Color(0xFF141C2E)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .border(1.dp, if (undoStack.isNotEmpty()) theme.accent.copy(alpha = 0.6f) else Color.Transparent, RoundedCornerShape(14.dp))
                            .testTag("undo_button")
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo", tint = if (undoStack.isNotEmpty()) theme.accent else Color.DarkGray, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Undo (${playerData.undos})",
                            color = if (undoStack.isNotEmpty()) Color.White else Color.DarkGray,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }

                    // Hint Button
                    Button(
                        onClick = { applyHint() },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primary),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        modifier = Modifier.testTag("hint_button")
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
            val gridSize = currentLevel.gridSize

            // Interactive Game Grid Canvas
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .aspectRatio(1f)
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(theme.surface)
                    .pointerInput(arrows, escapingArrows) {
                        detectTapGestures { offset ->
                            val cellSize = size.width / gridSize
                            val tappedX = (offset.x / cellSize).toInt()
                            val tappedY = (offset.y / cellSize).toInt()

                            val tappedArrow = arrows.find { it.x == tappedX && it.y == tappedY }
                            if (tappedArrow != null) {
                                handleArrowTap(tappedArrow)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellSize = size.width / gridSize

                    // Draw subtle grid lines
                    for (i in 0..gridSize) {
                        drawLine(
                            color = theme.gridLine,
                            start = Offset(i * cellSize, 0f),
                            end = Offset(i * cellSize, size.height),
                            strokeWidth = 1.2f
                        )
                        drawLine(
                            color = theme.gridLine,
                            start = Offset(0f, i * cellSize),
                            end = Offset(size.width, i * cellSize),
                            strokeWidth = 1.2f
                        )
                    }

                    // Draw escape paths for hinted arrow
                    if (hintedArrowId != null) {
                        val hinted = arrows.find { it.id == hintedArrowId }
                        if (hinted != null) {
                            val cx = hinted.x * cellSize + cellSize / 2
                            val cy = hinted.y * cellSize + cellSize / 2
                            val endX = when (hinted.direction) {
                                Direction.LEFT -> 0f
                                Direction.RIGHT -> size.width
                                else -> cx
                            }
                            val endY = when (hinted.direction) {
                                Direction.UP -> 0f
                                Direction.DOWN -> size.height
                                else -> cy
                            }
                            drawLine(
                                color = theme.primary.copy(alpha = 0.5f),
                                start = Offset(cx, cy),
                                end = Offset(endX, endY),
                                strokeWidth = 4.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                            )
                        }
                    }

                    // Render arrows
                    arrows.forEach { arrow ->
                        val isHinted = arrow.id == hintedArrowId
                        val isShaking = arrow.id == shakingArrowId
                        val escapeProgress = escapingArrows[arrow.id] ?: 0f

                        val shakeOffset = if (isShaking) (sin(System.currentTimeMillis() * 0.05f) * 6.dp.toPx()) else 0f

                        var baseX = arrow.x * cellSize + cellSize / 2 + shakeOffset
                        var baseY = arrow.y * cellSize + cellSize / 2

                        // Animate outward when escaping
                        if (escapeProgress > 0f) {
                            val travelDistance = size.width * 1.2f * escapeProgress
                            baseX += arrow.direction.dx * travelDistance
                            baseY += arrow.direction.dy * travelDistance
                        }

                        drawArrowItem(
                            arrow = arrow,
                            center = Offset(baseX, baseY),
                            cellSize = cellSize,
                            primaryColor = if (isHinted) theme.primary else theme.glow,
                            accentColor = theme.accent,
                            scale = if (isHinted) hintPulse else 1.0f,
                            isEscaping = escapeProgress > 0f
                        )
                    }

                    // Render neon burst particles
                    val now = System.currentTimeMillis()
                    particles = particles.filter { now - it.startTime < it.maxLife }
                    particles.forEach { p ->
                        val pAge = (now - p.startTime).toFloat() / p.maxLife.toFloat()
                        val pAlpha = (1f - pAge).coerceIn(0f, 1f)
                        val px = p.x * cellSize + cellSize / 2 + (p.vx * pAge * 0.4f)
                        val py = p.y * cellSize + cellSize / 2 + (p.vy * pAge * 0.4f)
                        drawCircle(
                            color = p.color.copy(alpha = pAlpha),
                            radius = (4f * (1f - pAge * 0.5f)).dp.toPx(),
                            center = Offset(px, py)
                        )
                    }
                }
            }

            // Level Complete Dialog (Panel 9)
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

            // Game Over Dialog (Panel 10)
            if (isGameOver) {
                com.example.ui.components.GameOverDialog(
                    coinsAvailable = playerData.coins,
                    onWatchAdReward = {
                        prefsManager.refillLives()
                        isGameOver = false
                        arrows = currentLevel.arrows
                    },
                    onUseCoins = {
                        if (playerData.coins >= 50) {
                            prefsManager.save(playerData.copy(coins = playerData.coins - 50))
                            prefsManager.refillLives()
                            isGameOver = false
                            arrows = currentLevel.arrows
                        }
                    },
                    onRetry = {
                        prefsManager.refillLives()
                        isGameOver = false
                        arrows = currentLevel.arrows
                        undoStack = emptyList()
                        mistakes = 0
                    },
                    onHome = onBack
                )
            }
        }
    }
}

/**
 * Custom Canvas drawing for a futuristic glowing neon arrow.
 */
private fun DrawScope.drawArrowItem(
    arrow: Arrow2D,
    center: Offset,
    cellSize: Float,
    primaryColor: Color,
    accentColor: Color,
    scale: Float = 1.0f,
    isEscaping: Boolean = false
) {
    val radius = (cellSize * 0.38f) * scale
    val angle = Math.toRadians(arrow.direction.angleDegrees.toDouble())

    // Subtle glow bloom behind arrow
    drawCircle(
        color = primaryColor.copy(alpha = if (isEscaping) 0.5f else 0.15f),
        radius = radius * 1.3f,
        center = center
    )

    // Arrow background pill/capsule
    drawCircle(
        color = Color(0xFF161F38),
        radius = radius,
        center = center
    )
    drawCircle(
        color = primaryColor.copy(alpha = 0.4f),
        radius = radius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    // Draw directional Arrow Path
    val path = Path()
    val cosA = cos(angle).toFloat()
    val sinA = sin(angle).toFloat()
    val headLength = radius * 0.9f
    val wingLength = radius * 0.5f

    val tip = Offset(center.x + headLength * cosA, center.y + headLength * sinA)
    val base = Offset(center.x - headLength * 0.7f * cosA, center.y - headLength * 0.7f * sinA)

    // Arrow shaft line
    drawLine(
        color = primaryColor,
        start = base,
        end = tip,
        strokeWidth = 3.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Left and right chevron wing
    val wingAngle = Math.toRadians(35.0)
    val leftWingCos = cos(angle + Math.PI - wingAngle).toFloat()
    val leftWingSin = sin(angle + Math.PI - wingAngle).toFloat()
    val rightWingCos = cos(angle + Math.PI + wingAngle).toFloat()
    val rightWingSin = sin(angle + Math.PI + wingAngle).toFloat()

    val leftWing = Offset(tip.x + wingLength * leftWingCos, tip.y + wingLength * leftWingSin)
    val rightWing = Offset(tip.x + wingLength * rightWingCos, tip.y + wingLength * rightWingSin)

    path.moveTo(leftWing.x, leftWing.y)
    path.lineTo(tip.x, tip.y)
    path.lineTo(rightWing.x, rightWing.y)

    drawPath(
        path = path,
        color = primaryColor,
        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}

data class ParticleEffect(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val maxLife: Long,
    val startTime: Long
)
