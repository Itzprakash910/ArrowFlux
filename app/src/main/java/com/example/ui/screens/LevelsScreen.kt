package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.GameTheme
import com.example.model.PlayerData

/**
 * Level Select screen strictly matching Panel 4 of design reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelsScreen(
    initialMode: GameMode,
    playerData: PlayerData,
    theme: GameTheme,
    onBack: () -> Unit,
    onStartLevel: (GameMode, Int) -> Unit
) {
    var selectedMode by remember(initialMode) { mutableStateOf(initialMode) }

    val totalLevels = when (selectedMode) {
        GameMode.CLASSIC -> 100
        GameMode.MAZE -> 100
        GameMode.THREE_D -> 50
    }

    val completedMap = when (selectedMode) {
        GameMode.CLASSIC -> playerData.completedClassic
        GameMode.MAZE -> playerData.completedMaze
        GameMode.THREE_D -> playerData.completed3D
    }

    val modeTitle = when (selectedMode) {
        GameMode.CLASSIC -> "Classic Arrow"
        GameMode.MAZE -> "Arrow Maze"
        GameMode.THREE_D -> "Arrow Escape 3D"
    }

    val modeColor = when (selectedMode) {
        GameMode.CLASSIC -> Color(0xFF00E5FF)
        GameMode.MAZE -> Color(0xFF00FF87)
        GameMode.THREE_D -> Color(0xFFB066FF)
    }

    val starsEarned = completedMap.values.sum()

    Scaffold(
        containerColor = Color(0xFF070B16),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = modeTitle,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("levels_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Star Badge: ★ 45/100
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF10192D))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Stars",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "$starsEarned / ${totalLevels * 3}",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070B16))
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Mode Switcher Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0D1424))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GameMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        val tabColor = when (mode) {
                            GameMode.CLASSIC -> Color(0xFF00E5FF)
                            GameMode.MAZE -> Color(0xFF00FF87)
                            GameMode.THREE_D -> Color(0xFFB066FF)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) tabColor.copy(alpha = 0.2f) else Color.Transparent)
                                .then(if (isSelected) Modifier.border(1.2.dp, tabColor, RoundedCornerShape(12.dp)) else Modifier)
                                .clickable { selectedMode = mode }
                                .testTag("tab_${mode.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (mode) {
                                    GameMode.CLASSIC -> "Classic"
                                    GameMode.MAZE -> "Maze"
                                    GameMode.THREE_D -> "3D Escape"
                                },
                                color = if (isSelected) Color.White else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Grid of Level Nodes (5 columns)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(totalLevels) { index ->
                        val levelNumber = index + 1
                        val isCompleted = completedMap.containsKey(levelNumber)
                        val stars = completedMap[levelNumber] ?: 0
                        val isUnlocked = levelNumber == 1 || completedMap.containsKey(levelNumber - 1)
                        val isCurrent = isUnlocked && !isCompleted

                        LevelCardNode(
                            level = levelNumber,
                            isUnlocked = isUnlocked,
                            isCompleted = isCompleted,
                            isCurrent = isCurrent,
                            stars = stars,
                            modeColor = modeColor,
                            onClick = {
                                if (isUnlocked) {
                                    onStartLevel(selectedMode, levelNumber)
                                }
                            }
                        )
                    }
                }

                // Bottom Scenic Silhouette Art (from Panel 4)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val path = Path().apply {
                            moveTo(0f, h)
                            lineTo(0f, h * 0.4f)
                            lineTo(w * 0.2f, h * 0.2f)
                            lineTo(w * 0.35f, h * 0.5f)
                            lineTo(w * 0.55f, h * 0.15f)
                            lineTo(w * 0.75f, h * 0.45f)
                            lineTo(w * 0.9f, h * 0.25f)
                            lineTo(w, h * 0.5f)
                            lineTo(w, h)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                listOf(modeColor.copy(alpha = 0.2f), Color(0xFF070B16))
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelCardNode(
    level: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    isCurrent: Boolean,
    stars: Int,
    modeColor: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "currentLevelPulse")
    val pulseScale by if (isCurrent) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press"
    )

    val borderColor = when {
        isCurrent -> modeColor
        isCompleted -> Color(0xFFFFD700).copy(alpha = 0.6f)
        isUnlocked -> Color(0x33FFFFFF)
        else -> Color(0x15FFFFFF)
    }

    val bgColor = when {
        isCurrent -> modeColor.copy(alpha = 0.22f)
        isCompleted -> Color(0xFF0F1B30)
        isUnlocked -> Color(0xFF0D1424)
        else -> Color(0xFF080D18)
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .scale(if (isCurrent) pulseScale * pressScale else pressScale)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isCurrent) 2.2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = isUnlocked) {
                isPressed = true
                onClick()
            }
            .testTag("level_node_$level"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isUnlocked) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = "$level",
                    color = if (isCompleted) Color.White else if (isCurrent) modeColor else Color.LightGray,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )

                if (stars > 0) {
                    Row(modifier = Modifier.padding(top = 2.dp)) {
                        repeat(3) { i ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i < stars) Color(0xFFFFD700) else Color(0xFF334155),
                                modifier = Modifier.size(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
