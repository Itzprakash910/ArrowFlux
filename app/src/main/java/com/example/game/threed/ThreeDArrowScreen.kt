package com.example.game.threed

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import kotlin.math.*

private data class ProjectedFace(
    val face: BlockFace,
    val block: Block3D,
    val center2D: Offset,
    val depth: Float,
    val polygon2D: List<Offset>,
    val color: Color
)

private data class ProjectedArrow(
    val arrow: Arrow3D,
    val center2D: Offset,
    val tip2D: Offset,
    val depth: Float,
    val normal2D: Offset
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreeDArrowScreen(
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
    var currentLevel by remember(levelId) { mutableStateOf(ThreeDSolver.getLevel(levelId)) }
    var arrows by remember(levelId) { mutableStateOf(currentLevel.arrows) }
    var undoStack by remember(levelId) { mutableStateOf(listOf<List<Arrow3D>>()) }
    var mistakes by remember(levelId) { mutableStateOf(0) }
    var hintsUsed by remember(levelId) { mutableStateOf(0) }
    var hintedArrowId by remember(levelId) { mutableStateOf<String?>(null) }
    var shakingArrowId by remember(levelId) { mutableStateOf<String?>(null) }
    var isLevelCompleted by remember(levelId) { mutableStateOf(false) }
    var isGameOver by remember(levelId) { mutableStateOf(false) }
    var starsAwarded by remember(levelId) { mutableStateOf(3) }
    var escapingArrows by remember(levelId) { mutableStateOf(mapOf<String, Float>()) }

    // 3D Camera Controls
    var rotX by remember { mutableStateOf(28f) } // Pitch
    var rotY by remember { mutableStateOf(45f) } // Yaw
    var zoomScale by remember { mutableStateOf(1.0f) }

    fun handleArrowTap(arrow: Arrow3D) {
        if (isLevelCompleted || isGameOver || escapingArrows.containsKey(arrow.id)) return

        val canEscape = ThreeDSolver.canArrowEscape(arrow, arrows, currentLevel.blocks)
        if (canEscape) {
            soundManager.playMove()
            hapticManager.vibrateEscape()
            undoStack = undoStack + listOf(arrows)
            hintedArrowId = null

            coroutineScope.launch {
                val startTime = System.currentTimeMillis()
                val duration = 320L
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
                    val coinsEarned = 35 + (finalStars * 20)
                    prefsManager.completeLevel(GameMode.THREE_D, levelId, finalStars, coinsEarned)
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
        val free = arrows.firstOrNull { ThreeDSolver.canArrowEscape(it, arrows, currentLevel.blocks) }
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
                        Text(text = currentLevel.structureName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Drag to rotate cube · ${arrows.size} left", color = theme.glow.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("3d_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Reset Camera View Button
                    IconButton(
                        onClick = {
                            rotX = 28f
                            rotY = 45f
                            zoomScale = 1.0f
                        },
                        modifier = Modifier.testTag("reset_view_button")
                    ) {
                        Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset Camera View", tint = theme.primary)
                    }

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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.94f))
                        .border(1.2.dp, Color(0xFFB066FF).copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Zoom adjustment slider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Slider(
                            value = zoomScale,
                            onValueChange = { zoomScale = it },
                            valueRange = 0.6f..1.6f,
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFB066FF), activeTrackColor = Color(0xFFB066FF))
                        )
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                .size(42.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White, modifier = Modifier.size(20.dp))
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
                                .border(1.dp, if (undoStack.isNotEmpty()) Color(0xFFB066FF).copy(alpha = 0.6f) else Color.Transparent, RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = "Undo", tint = if (undoStack.isNotEmpty()) Color(0xFFB066FF) else Color.DarkGray, modifier = Modifier.size(18.dp))
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB066FF)),
                            shape = RoundedCornerShape(14.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Hint (${playerData.hints})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
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
            // Container for 3D Viewport with Drag and Tap Gestures
            var projectedArrowsList by remember { mutableStateOf<List<ProjectedArrow>>(emptyList()) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            rotY += dragAmount.x * 0.45f
                            rotX = (rotX - dragAmount.y * 0.45f).coerceIn(-75f, 75f)
                        }
                    }
                    .pointerInput(projectedArrowsList) {
                        detectTapGestures { tapOffset ->
                            // Find closest projected arrow within touch radius
                            val hitRadius = 42.dp.toPx()
                            val hitArrow = projectedArrowsList
                                .filter { (it.center2D - tapOffset).getDistance() <= hitRadius }
                                .maxByOrNull { it.depth } // Closest to camera

                            if (hitArrow != null) {
                                handleArrowTap(hitArrow.arrow)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val blockSize = (min(size.width, size.height) * 0.16f) * zoomScale

                    // Calculate 3D center of the structure to pivot around
                    val avgX = currentLevel.blocks.map { it.x }.average().toFloat()
                    val avgY = currentLevel.blocks.map { it.y }.average().toFloat()
                    val avgZ = currentLevel.blocks.map { it.z }.average().toFloat()

                    val radX = Math.toRadians(rotX.toDouble())
                    val radY = Math.toRadians(rotY.toDouble())
                    val cosX = cos(radX).toFloat()
                    val sinX = sin(radX).toFloat()
                    val cosY = cos(radY).toFloat()
                    val sinY = sin(radY).toFloat()

                    fun project(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
                        // Offset to center
                        val ox = x - avgX
                        val oy = y - avgY
                        val oz = z - avgZ

                        // Yaw (around Y axis)
                        val x1 = ox * cosY - oz * sinY
                        val z1 = ox * sinY + oz * cosY

                        // Pitch (around X axis)
                        val y2 = oy * cosX - z1 * sinX
                        val z2 = oy * sinX + z1 * cosX

                        // Perspective projection
                        val fov = 1000f
                        val cameraDist = 800f
                        val factor = fov / (fov + z2 * blockSize + cameraDist)

                        val screenX = centerX + x1 * blockSize * factor
                        val screenY = centerY - y2 * blockSize * factor

                        return Triple(screenX, screenY, z2)
                    }

                    // Collect and project all faces
                    val projectedFaces = mutableListOf<ProjectedFace>()
                    currentLevel.blocks.forEach { block ->
                        val bx = block.x.toFloat()
                        val by = block.y.toFloat()
                        val bz = block.z.toFloat()

                        // 8 vertices of cube
                        val v000 = project(bx - 0.5f, by - 0.5f, bz - 0.5f)
                        val v100 = project(bx + 0.5f, by - 0.5f, bz - 0.5f)
                        val v110 = project(bx + 0.5f, by + 0.5f, bz - 0.5f)
                        val v010 = project(bx - 0.5f, by + 0.5f, bz - 0.5f)

                        val v001 = project(bx - 0.5f, by - 0.5f, bz + 0.5f)
                        val v101 = project(bx + 0.5f, by - 0.5f, bz + 0.5f)
                        val v111 = project(bx + 0.5f, by + 0.5f, bz + 0.5f)
                        val v011 = project(bx - 0.5f, by + 0.5f, bz + 0.5f)

                        fun addFace(face: BlockFace, poly: List<Triple<Float, Float, Float>>, color: Color) {
                            val p1 = poly[0]
                            val p2 = poly[1]
                            val p3 = poly[2]

                            // Back-face culling via cross product in screen space
                            val cross = (p2.first - p1.first) * (p3.second - p1.second) - (p2.second - p1.second) * (p3.first - p1.first)
                            if (cross > 0) { // Visible towards viewer
                                val avgZDepth = poly.map { it.third }.average().toFloat()
                                val avg2D = Offset(poly.map { it.first }.average().toFloat(), poly.map { it.second }.average().toFloat())
                                projectedFaces.add(
                                    ProjectedFace(
                                        face = face,
                                        block = block,
                                        center2D = avg2D,
                                        depth = avgZDepth,
                                        polygon2D = poly.map { Offset(it.first, it.second) },
                                        color = color
                                    )
                                )
                            }
                        }

                        // Front Face (+Z)
                        addFace(BlockFace.FRONT, listOf(v001, v101, v111, v011), Color(0xFF1E293B))
                        // Back Face (-Z)
                        addFace(BlockFace.BACK, listOf(v100, v000, v010, v110), Color(0xFF0F172A))
                        // Top Face (+Y)
                        addFace(BlockFace.TOP, listOf(v011, v111, v110, v010), Color(0xFF334155))
                        // Bottom Face (-Y)
                        addFace(BlockFace.BOTTOM, listOf(v000, v100, v101, v001), Color(0xFF090D16))
                        // Right Face (+X)
                        addFace(BlockFace.RIGHT, listOf(v101, v100, v110, v111), Color(0xFF283548))
                        // Left Face (-X)
                        addFace(BlockFace.LEFT, listOf(v000, v001, v011, v010), Color(0xFF151D2A))
                    }

                    // Project arrows
                    val currentProjectedArrows = mutableListOf<ProjectedArrow>()
                    arrows.forEach { arrow ->
                        val normal = when (arrow.face) {
                            BlockFace.FRONT -> Triple(0f, 0f, 1f)
                            BlockFace.BACK -> Triple(0f, 0f, -1f)
                            BlockFace.TOP -> Triple(0f, 1f, 0f)
                            BlockFace.BOTTOM -> Triple(0f, -1f, 0f)
                            BlockFace.RIGHT -> Triple(1f, 0f, 0f)
                            BlockFace.LEFT -> Triple(-1f, 0f, 0f)
                        }

                        val escapeProg = escapingArrows[arrow.id] ?: 0f
                        val escapeDist = escapeProg * 6.0f

                        val ax = arrow.blockX + normal.first * (0.52f + escapeDist)
                        val ay = arrow.blockY + normal.second * (0.52f + escapeDist)
                        val az = arrow.blockZ + normal.third * (0.52f + escapeDist)

                        val pCenter = project(ax, ay, az)
                        val pTip = project(ax + normal.first * 0.35f, ay + normal.second * 0.35f, az + normal.third * 0.35f)

                        currentProjectedArrows.add(
                            ProjectedArrow(
                                arrow = arrow,
                                center2D = Offset(pCenter.first, pCenter.second),
                                tip2D = Offset(pTip.first, pTip.second),
                                depth = pCenter.third,
                                normal2D = Offset(pTip.first - pCenter.first, pTip.second - pCenter.second)
                            )
                        )
                    }

                    projectedArrowsList = currentProjectedArrows

                    // Render sorted back-to-front (depth: lower z2 means closer or farther depending on sign)
                    // In our projection, higher z1/z2 is deeper into screen, so sort descending (farthest first)
                    val sortedFaces = projectedFaces.sortedBy { it.depth }

                    sortedFaces.forEach { faceItem ->
                        val path = Path().apply {
                            moveTo(faceItem.polygon2D[0].x, faceItem.polygon2D[0].y)
                            for (i in 1 until faceItem.polygon2D.size) {
                                lineTo(faceItem.polygon2D[i].x, faceItem.polygon2D[i].y)
                            }
                            close()
                        }
                        // Draw face body
                        drawPath(path, faceItem.color)
                        // Draw edge neon outline
                        drawPath(path, theme.gridLine, style = Stroke(width = 1.5.dp.toPx()))
                    }

                    // Render arrows sorted back to front
                    val sortedArrows = currentProjectedArrows.sortedBy { it.depth }
                    sortedArrows.forEach { item ->
                        val isHinted = item.arrow.id == hintedArrowId
                        val isShaking = item.arrow.id == shakingArrowId
                        val escapeProg = escapingArrows[item.arrow.id] ?: 0f

                        val shakeOffset = if (isShaking) (sin(System.currentTimeMillis() * 0.05f) * 6.dp.toPx()) else 0f
                        val arrowPos = Offset(item.center2D.x + shakeOffset, item.center2D.y)

                        val radius = 16.dp.toPx() * zoomScale
                        val arrowColor = if (isHinted) theme.primary else Color(0xFF00F0FF)

                        // Glowing sphere backing on face
                        drawCircle(
                            color = arrowColor.copy(alpha = if (escapeProg > 0f) 0.7f else 0.25f),
                            radius = radius * 1.3f,
                            center = arrowPos
                        )
                        drawCircle(color = Color(0xFF0B1426), radius = radius, center = arrowPos)
                        drawCircle(color = arrowColor, radius = radius, center = arrowPos, style = Stroke(width = 2.dp.toPx()))

                        // Draw escaping neon laser line
                        drawLine(
                            color = arrowColor,
                            start = arrowPos,
                            end = item.tip2D,
                            strokeWidth = 3.5.dp.toPx(),
                            cap = StrokeCap.Round
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
