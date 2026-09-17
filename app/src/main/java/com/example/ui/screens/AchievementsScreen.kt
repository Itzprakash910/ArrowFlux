package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

data class AchievementUiItem(
    val id: String,
    val title: String,
    val description: String,
    val current: Int,
    val max: Int,
    val icon: ImageVector,
    val iconColor: Color
)

/**
 * Achievements screen strictly matching Panel 12 in design reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    playerData: PlayerData,
    theme: GameTheme,
    onBack: () -> Unit
) {
    val totalClassic = playerData.completedClassic.size
    val totalMaze = playerData.completedMaze.size
    val total3D = playerData.completed3D.size
    val totalSolved = totalClassic + totalMaze + total3D
    val totalStars = playerData.stars
    val perfectClears = (playerData.completedClassic.values + playerData.completedMaze.values + playerData.completed3D.values).count { it == 3 }

    val achievements = listOf(
        AchievementUiItem(
            id = "FIRST_ESCAPE",
            title = "First Escape",
            description = "Complete your very first arrow puzzle",
            current = if (totalSolved >= 1) 1 else 0,
            max = 1,
            icon = Icons.Default.PlayArrow,
            iconColor = Color(0xFF00FF87)
        ),
        AchievementUiItem(
            id = "ARROW_BEGINNER",
            title = "Arrow Beginner",
            description = "Clear 10 levels across any game mode",
            current = totalSolved.coerceAtMost(10),
            max = 10,
            icon = Icons.Default.CheckCircle,
            iconColor = Color(0xFF00E5FF)
        ),
        AchievementUiItem(
            id = "ARROW_MASTER",
            title = "Arrow Master",
            description = "Master and complete 50 puzzle levels",
            current = totalSolved.coerceAtMost(50),
            max = 50,
            icon = Icons.Default.EmojiEvents,
            iconColor = Color(0xFFFFD700)
        ),
        AchievementUiItem(
            id = "PERFECT_THINKER",
            title = "Perfect Thinker",
            description = "Earn 3 stars on 25 levels without mistakes",
            current = perfectClears.coerceAtMost(25),
            max = 25,
            icon = Icons.Default.MilitaryTech,
            iconColor = Color(0xFFFF7B00)
        ),
        AchievementUiItem(
            id = "3D_EXPLORER",
            title = "3D Explorer",
            description = "Rotate and escape your first 3D structure",
            current = if (total3D >= 1) 1 else 0,
            max = 1,
            icon = Icons.Default.ViewInAr,
            iconColor = Color(0xFFB066FF)
        ),
        AchievementUiItem(
            id = "MAZE_NAVIGATOR",
            title = "Maze Navigator",
            description = "Successfully solve 15 Arrow Maze labyrinths",
            current = totalMaze.coerceAtMost(15),
            max = 15,
            icon = Icons.Default.Timeline,
            iconColor = Color(0xFF00FF87)
        ),
        AchievementUiItem(
            id = "DAILY_WARRIOR",
            title = "Daily Warrior",
            description = "Maintain a 7-day daily puzzle streak",
            current = playerData.streak.coerceAtMost(7),
            max = 7,
            icon = Icons.Default.Whatshot,
            iconColor = Color(0xFFFF3366)
        ),
        AchievementUiItem(
            id = "STAR_COLLECTOR",
            title = "Constellation Master",
            description = "Collect 100 golden stars throughout the cosmos",
            current = totalStars.coerceAtMost(100),
            max = 100,
            icon = Icons.Default.Star,
            iconColor = Color(0xFFFFD700)
        )
    )

    val unlockedCount = achievements.count { it.current >= it.max }

    Scaffold(
        containerColor = Color(0xFF070B16),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Achievements",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("achievements_back_button")) {
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Progress Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A30)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF332A0A))
                                .border(1.5.dp, Color(0xFFFFD700), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Trophies Unlocked",
                                color = Color.LightGray,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$unlockedCount of ${achievements.size} Completed",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { unlockedCount.toFloat() / achievements.size.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFFFD700),
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }

            // Achievements List
            items(achievements) { item ->
                val isCompleted = item.current >= item.max

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isCompleted) item.iconColor.copy(alpha = 0.5f) else Color(0x22FFFFFF),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icon Badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isCompleted) item.iconColor.copy(alpha = 0.2f) else Color(0xFF161F33))
                                .border(
                                    1.2.dp,
                                    if (isCompleted) item.iconColor else Color(0x44FFFFFF),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isCompleted) item.iconColor else Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${item.current}/${item.max}",
                                    color = if (isCompleted) item.iconColor else Color.Gray,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.description,
                                color = Color.LightGray.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { (item.current.toFloat() / item.max.toFloat()).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (isCompleted) item.iconColor else Color(0xFF3B82F6),
                                trackColor = Color(0xFF161F33)
                            )
                        }
                    }
                }
            }
        }
    }
}
