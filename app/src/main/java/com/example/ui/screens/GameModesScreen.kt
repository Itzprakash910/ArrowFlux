package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameMode
import com.example.model.GameTheme
import com.example.model.PlayerData

/**
 * Game Modes screen strictly matching Panel 3 in design reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameModesScreen(
    playerData: PlayerData,
    theme: GameTheme,
    onBack: () -> Unit,
    onSelectMode: (GameMode) -> Unit
) {
    val classicStars = playerData.completedClassic.values.sum()
    val mazeStars = playerData.completedMaze.values.sum()
    val threeDStars = playerData.completed3D.values.sum()

    val classicHighest = (playerData.completedClassic.keys.maxOrNull() ?: 0) + 1
    val mazeHighest = (playerData.completedMaze.keys.maxOrNull() ?: 0) + 1
    val threeDHighest = (playerData.completed3D.keys.maxOrNull() ?: 0) + 1

    Scaffold(
        containerColor = Color(0xFF070B16),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Game Modes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("modes_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070B16))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode 1: Classic Arrow
            item {
                ModeCard(
                    title = "CLASSIC ARROW",
                    subtitle = "Clear the arrows",
                    starsText = "★ $classicStars/300",
                    levelText = "Lv. $classicHighest",
                    accentColor = Color(0xFF00E5FF),
                    onClick = { onSelectMode(GameMode.CLASSIC) },
                    drawGraphic = {
                        drawClassicGraphic(Color(0xFF00E5FF))
                    }
                )
            }

            // Mode 2: Arrow Maze
            item {
                ModeCard(
                    title = "ARROW MAZE",
                    subtitle = "Find the free path",
                    starsText = "★ $mazeStars/300",
                    levelText = "Lv. $mazeHighest",
                    accentColor = Color(0xFF00FF87),
                    onClick = { onSelectMode(GameMode.MAZE) },
                    drawGraphic = {
                        drawMazeGraphic(Color(0xFF00FF87))
                    }
                )
            }

            // Mode 3: Arrow Escape 3D
            item {
                ModeCard(
                    title = "ARROW ESCAPE 3D",
                    subtitle = "Rotate. Think. Escape.",
                    starsText = "★ $threeDStars/150",
                    levelText = "Lv. $threeDHighest",
                    accentColor = Color(0xFFB066FF),
                    onClick = { onSelectMode(GameMode.THREE_D) },
                    drawGraphic = {
                        drawThreeDGraphic(Color(0xFFB066FF))
                    }
                )
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    subtitle: String,
    starsText: String,
    levelText: String,
    accentColor: Color,
    onClick: () -> Unit,
    drawGraphic: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                Brush.horizontalGradient(
                    listOf(accentColor.copy(alpha = 0.6f), Color.Transparent)
                ),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .testTag("mode_card_${title.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Title, Subtitle, and Star Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color.LightGray.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Star Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF161F33))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = starsText,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    // Level Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .border(1.dp, accentColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = levelText,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Graphic Box Preview (130dp height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF070B14))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawGraphic()
                }

                // Play Floating Pill Button
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PLAY",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawClassicGraphic(color: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val spacing = 48.dp.toPx()

    // 4 arrows facing different escape directions
    val directions = listOf(
        Pair(Offset(cx - spacing, cy), Offset(-1f, 0f)),
        Pair(Offset(cx + spacing, cy), Offset(1f, 0f)),
        Pair(Offset(cx, cy - spacing * 0.7f), Offset(0f, -1f)),
        Pair(Offset(cx, cy + spacing * 0.7f), Offset(0f, 1f))
    )

    directions.forEach { (pos, dir) ->
        // Glow circle
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = 22.dp.toPx(),
            center = pos
        )
        // Background node
        drawCircle(
            color = Color(0xFF131F36),
            radius = 18.dp.toPx(),
            center = pos
        )
        drawCircle(
            color = color,
            radius = 18.dp.toPx(),
            center = pos,
            style = Stroke(width = 2.dp.toPx())
        )
        // Arrow head
        val tip = Offset(pos.x + dir.x * 12.dp.toPx(), pos.y + dir.y * 12.dp.toPx())
        val base = Offset(pos.x - dir.x * 10.dp.toPx(), pos.y - dir.y * 10.dp.toPx())
        drawLine(
            color = Color.White,
            start = base,
            end = tip,
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMazeGraphic(color: Color) {
    val path = Path()
    val cx = size.width / 2f
    val cy = size.height / 2f

    // Winding corridor lines
    path.moveTo(cx - 80.dp.toPx(), cy)
    path.lineTo(cx - 30.dp.toPx(), cy)
    path.lineTo(cx - 30.dp.toPx(), cy - 35.dp.toPx())
    path.lineTo(cx + 30.dp.toPx(), cy - 35.dp.toPx())
    path.lineTo(cx + 30.dp.toPx(), cy + 25.dp.toPx())
    path.lineTo(cx + 80.dp.toPx(), cy + 25.dp.toPx())

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )

    // Glow path
    drawPath(
        path = path,
        color = color.copy(alpha = 0.25f),
        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
    )

    // Start 'S' and Exit 'E' markers
    drawCircle(color = Color(0xFF00FF87), radius = 10.dp.toPx(), center = Offset(cx - 80.dp.toPx(), cy))
    drawCircle(color = Color(0xFFFFD700), radius = 10.dp.toPx(), center = Offset(cx + 80.dp.toPx(), cy + 25.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawThreeDGraphic(color: Color) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val r = 36.dp.toPx()

    // Isometric Cube Faces
    val topFace = Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r * 1.1f, cy - r * 0.45f)
        lineTo(cx, cy + r * 0.1f)
        lineTo(cx - r * 1.1f, cy - r * 0.45f)
        close()
    }
    val leftFace = Path().apply {
        moveTo(cx - r * 1.1f, cy - r * 0.45f)
        lineTo(cx, cy + r * 0.1f)
        lineTo(cx, cy + r * 1.1f)
        lineTo(cx - r * 1.1f, cy + r * 0.55f)
        close()
    }
    val rightFace = Path().apply {
        moveTo(cx, cy + r * 0.1f)
        lineTo(cx + r * 1.1f, cy - r * 0.45f)
        lineTo(cx + r * 1.1f, cy + r * 0.55f)
        lineTo(cx, cy + r * 1.1f)
        close()
    }

    drawPath(path = topFace, color = Color(0xFF3B1E63))
    drawPath(path = topFace, color = color, style = Stroke(width = 2.dp.toPx()))

    drawPath(path = leftFace, color = Color(0xFF22113D))
    drawPath(path = leftFace, color = color.copy(alpha = 0.8f), style = Stroke(width = 2.dp.toPx()))

    drawPath(path = rightFace, color = Color(0xFF160A2A))
    drawPath(path = rightFace, color = color.copy(alpha = 0.8f), style = Stroke(width = 2.dp.toPx()))

    // Center glowing arrow on top
    drawLine(
        color = Color(0xFFFFD700),
        start = Offset(cx - 10.dp.toPx(), cy - r * 0.4f),
        end = Offset(cx + 10.dp.toPx(), cy - r * 0.4f),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
}
