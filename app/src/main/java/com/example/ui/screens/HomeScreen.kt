package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameTheme
import com.example.model.PlayerData

/**
 * Home Screen strictly matching Panel 2 of the design reference.
 */
@Composable
fun HomeScreen(
    playerData: PlayerData,
    theme: GameTheme,
    onQuickPlay: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenLevels: () -> Unit,
    onOpenModes: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit
) {
    val totalAchievements = 8
    val totalSolved = playerData.completedClassic.size + playerData.completedMaze.size + playerData.completed3D.size
    val achievementsCount = listOf(
        totalSolved >= 1,
        totalSolved >= 10,
        totalSolved >= 50,
        (playerData.completedClassic.values + playerData.completedMaze.values + playerData.completed3D.values).count { it == 3 } >= 25,
        playerData.completed3D.isNotEmpty(),
        playerData.completedMaze.size >= 15,
        playerData.streak >= 7,
        playerData.stars >= 100
    ).count { it }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val logoGlowScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoGlow"
    )
    val playPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playPulse"
    )
    val backgroundGridPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridMotion"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070B16),
                        Color(0xFF0A1022),
                        Color(0xFF050711)
                    )
                )
            )
    ) {
        // Animated Cyber Grid Matrix Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 40.dp.toPx()
            val yOffset = backgroundGridPhase.dp.toPx() % step
            for (y in -step.toInt()..size.height.toInt() + step.toInt() step step.toInt()) {
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.04f),
                    start = Offset(0f, y.toFloat() + yOffset),
                    end = Offset(size.width, y.toFloat() + yOffset),
                    strokeWidth = 1f
                )
            }
            for (x in 0..size.width.toInt() step step.toInt()) {
                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = 0.04f),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), size.height),
                    strokeWidth = 1f
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Top Header Bar (Avatar Lv. 12 | Coins 1,250 | Hearts 3 + | Settings Cog)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Avatar with Level Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF10192D).copy(alpha = 0.85f))
                            .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable { onOpenProfile() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00E5FF).copy(alpha = 0.2f))
                                .border(1.5.dp, Color(0xFF00E5FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SportsEsports,
                                contentDescription = "Player Avatar",
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lv. ${playerData.level}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }

                    // Center: Coins Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF10192D).copy(alpha = 0.85f))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .clickable { onOpenShop() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${playerData.coins}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Right: Lives & Settings Cog
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF10192D).copy(alpha = 0.85f))
                                .border(1.dp, Color(0xFFFF2A6D).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable { onOpenShop() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Lives",
                                tint = Color(0xFFFF2A6D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${playerData.lives}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+",
                                color = Color(0xFFFF2A6D),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10192D))
                                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. Center Banner (Geometric Cyber Arrow Logo + "ARROWFLUX" + "Think. Rotate. Escape.")
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Geometric Cyber Arrow Logo in Canvas
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(logoGlowScale),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val r = 40.dp.toPx()

                            // Outer glowing diamond/hexagon
                            val diamond = Path().apply {
                                moveTo(cx, cy - r)
                                lineTo(cx + r, cy)
                                lineTo(cx, cy + r)
                                lineTo(cx - r, cy)
                                close()
                            }
                            drawPath(
                                path = diamond,
                                color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                                style = Stroke(width = 8.dp.toPx())
                            )
                            drawPath(
                                path = diamond,
                                color = Color(0xFF00E5FF),
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Inner intersecting neon arrows
                            val arrowPath = Path().apply {
                                moveTo(cx - 16.dp.toPx(), cy + 12.dp.toPx())
                                lineTo(cx, cy - 14.dp.toPx())
                                lineTo(cx + 16.dp.toPx(), cy + 12.dp.toPx())
                            }
                            drawPath(
                                path = arrowPath,
                                color = Color(0xFFFFD700),
                                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Center pulsing glow dot
                            drawCircle(
                                color = Color(0xFF00FF87),
                                radius = 4.dp.toPx(),
                                center = Offset(cx, cy)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ARROWFLUX",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Think. Rotate. Escape.",
                        color = Color(0xFF00E5FF),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // 3. Big Cyan Pill Button ("▶ PLAY") with pulsing glow
            item {
                Button(
                    onClick = onQuickPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
                    shape = RoundedCornerShape(26.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .scale(playPulseScale)
                        .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(26.dp))
                        .testTag("play_button")
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // 4. Vertical Menu List Cards strictly as shown in Panel 2
            // 1. Daily Challenge (Orange Icon)
            item {
                MenuCard(
                    title = "Daily Challenge",
                    subtitle = "New Challenge!",
                    icon = Icons.Default.Whatshot,
                    iconTint = Color(0xFFFF9500),
                    iconBg = Color(0xFF331E05),
                    onClick = onOpenDaily
                )
            }

            // 2. Levels (Green Icon)
            item {
                MenuCard(
                    title = "Levels",
                    subtitle = "100+ levels",
                    icon = Icons.Default.GridOn,
                    iconTint = Color(0xFF00FF87),
                    iconBg = Color(0xFF082B1B),
                    onClick = onOpenLevels
                )
            }

            // 3. Modes (Blue Icon)
            item {
                MenuCard(
                    title = "Modes",
                    subtitle = "3 Game Modes",
                    icon = Icons.Default.ViewInAr,
                    iconTint = Color(0xFF00B4D8),
                    iconBg = Color(0xFF082236),
                    onClick = onOpenModes
                )
            }

            // 4. Achievements (Gold Trophy Icon)
            item {
                MenuCard(
                    title = "Achievements",
                    subtitle = "$achievementsCount/$totalAchievements",
                    icon = Icons.Default.EmojiEvents,
                    iconTint = Color(0xFFFFD700),
                    iconBg = Color(0xFF332906),
                    onClick = onOpenAchievements
                )
            }

            // 5. Themes (Pink Swatches Icon)
            item {
                MenuCard(
                    title = "Themes",
                    subtitle = "${playerData.unlockedThemeIds.size} Unlocked",
                    icon = Icons.Default.Palette,
                    iconTint = Color(0xFFFF2A8D),
                    iconBg = Color(0xFF33091F),
                    onClick = onOpenThemes
                )
            }

            // 6. Shop (Purple Gift/Bag Icon)
            item {
                MenuCard(
                    title = "Shop",
                    subtitle = "Get Coins & More",
                    icon = Icons.Default.ShoppingBag,
                    iconTint = Color(0xFFB066FF),
                    iconBg = Color(0xFF270E45),
                    onClick = onOpenShop
                )
            }
        }
    }
}

@Composable
private fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "cardScale"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .border(1.2.dp, iconTint.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
            .clickable {
                isPressed = true
                onClick()
            }
            .testTag("menu_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconBg)
                        .border(1.dp, iconTint.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = Color.LightGray.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
