package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameTheme
import com.example.model.PlayerData

/**
 * Profile Screen strictly matching Panel 5 in design reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    playerData: PlayerData,
    theme: GameTheme,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAchievements: () -> Unit
) {
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val totalCompleted = playerData.completedClassic.size + playerData.completedMaze.size + playerData.completed3D.size
    val perfectLevels = (playerData.completedClassic.values + playerData.completedMaze.values + playerData.completed3D.values).count { it == 3 }
    val xpInLevel = playerData.xp % 300
    val xpTarget = 300

    Scaffold(
        containerColor = Color(0xFF070B16),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("profile_settings_button")) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.LightGray)
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Avatar Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Golden Crown in Cyan Ring
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF082236))
                                .border(2.dp, Color(0xFF00E5FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = "Crown",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ArrowMaster",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Lv. ${playerData.level}",
                                color = Color(0xFF00E5FF),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // XP Progress Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "XP Progress",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "$xpInLevel/$xpTarget",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { xpInLevel.toFloat() / xpTarget.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFF00E5FF),
                                trackColor = Color(0xFF162138)
                            )
                        }
                    }
                }
            }

            // 2. 6-Stats Grid (3x2 Grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Row 1: Stars, Completed, Perfect
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Stars",
                            value = "${playerData.stars}",
                            icon = Icons.Default.Star,
                            iconColor = Color(0xFFFFD700)
                        )
                        ProfileStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Completed",
                            value = "$totalCompleted",
                            icon = Icons.Default.CheckCircle,
                            iconColor = Color(0xFF00FF87)
                        )
                        ProfileStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Perfect",
                            value = "$perfectLevels",
                            icon = Icons.Default.MilitaryTech,
                            iconColor = Color(0xFFFF7B00)
                        )
                    }

                    // Row 2: Streak, Coins, Hints
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Streak",
                            value = "${playerData.streak} Days",
                            icon = Icons.Default.Whatshot,
                            iconColor = Color(0xFFFF3366)
                        )
                        ProfileStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Coins",
                            value = "${playerData.coins}",
                            icon = Icons.Default.MonetizationOn,
                            iconColor = Color(0xFFFFD700)
                        )
                        ProfileStatCard(
                            modifier = Modifier.weight(1f),
                            label = "Hints",
                            value = "${playerData.hints}",
                            icon = Icons.Default.Lightbulb,
                            iconColor = Color(0xFF00E5FF)
                        )
                    }
                }
            }

            // 3. Menu Options List (Achievements, Settings, Help & Support, About)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ProfileMenuItem(
                            title = "Achievements",
                            subtitle = "View badges & trophies",
                            icon = Icons.Default.EmojiEvents,
                            iconColor = Color(0xFFFFD700),
                            onClick = onOpenAchievements
                        )
                        Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileMenuItem(
                            title = "Settings",
                            subtitle = "Audio, haptics & preferences",
                            icon = Icons.Default.Settings,
                            iconColor = Color(0xFF38BDF8),
                            onClick = onOpenSettings
                        )
                        Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileMenuItem(
                            title = "Help & Support",
                            subtitle = "How to play & rules guide",
                            icon = Icons.Default.HelpOutline,
                            iconColor = Color(0xFF00FF87),
                            onClick = { showHelpDialog = true }
                        )
                        Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileMenuItem(
                            title = "About",
                            subtitle = "Version, rules & credits",
                            icon = Icons.Default.Info,
                            iconColor = Color(0xFFB066FF),
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }
        }
    }

    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text("How to Play ArrowFlux", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Classic Arrow: Tap an arrow pointing toward a clear path to release it.", color = Color.LightGray, fontSize = 13.sp)
                    Text("2. Arrow Maze: Trace winding neon laser corridors. Tap the free arrow to guide it to the exit.", color = Color.LightGray, fontSize = 13.sp)
                    Text("3. Arrow Escape 3D: Drag anywhere on screen to freely rotate the 3D cube. Tap arrows on faces that have unobstructed escape trajectories.", color = Color.LightGray, fontSize = 13.sp)
                    Text("4. Power-ups: Use Undo to revert accidental moves and Hints to reveal a guaranteed free arrow.", color = Color.LightGray, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("Got It", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF0D1424)
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About ArrowFlux", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("ArrowFlux Mobile Puzzle Game", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Version 1.0.0", color = Color.Gray, fontSize = 12.sp)
                    Text("Offline First · Clean Logic · 250+ Levels", color = Color.LightGray, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF0D1424)
        )
    }
}

@Composable
private fun ProfileStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0D1424))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f))
                    .border(1.dp, iconColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}
