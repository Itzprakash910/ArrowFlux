package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Daily Challenge screen strictly matching Panel 11 in design reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeScreen(
    playerData: PlayerData,
    theme: GameTheme,
    onBack: () -> Unit,
    onStartDaily: (GameMode, Int) -> Unit
) {
    val dateDisplay = SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date())
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val isCompletedToday = playerData.lastDailyDate == todayDateStr
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    val dailyLevel = (dayOfYear % 30) + 1

    Scaffold(
        containerColor = Color(0xFF070B16),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Daily Challenge",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("daily_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070B16))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Date & Difficulty Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateDisplay,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF261D0A))
                            .border(1.dp, Color(0xFFFF9E00), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Medium",
                            color = Color(0xFFFF9E00),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Glowing Neon Maze Preview Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.5.dp,
                            Brush.verticalGradient(
                                listOf(Color(0xFF00FF87).copy(alpha = 0.5f), Color(0xFF00E5FF).copy(alpha = 0.3f))
                            ),
                            RoundedCornerShape(22.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Canvas Illustration of Neon Labyrinth
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF070B14)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val cx = size.width / 2f
                                val cy = size.height / 2f

                                // Maze grid backdrop lines
                                for (i in -2..2) {
                                    drawLine(
                                        color = Color(0x15FFFFFF),
                                        start = Offset(cx + i * 35.dp.toPx(), cy - 60.dp.toPx()),
                                        end = Offset(cx + i * 35.dp.toPx(), cy + 60.dp.toPx()),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                    drawLine(
                                        color = Color(0x15FFFFFF),
                                        start = Offset(cx - 70.dp.toPx(), cy + i * 25.dp.toPx()),
                                        end = Offset(cx + 70.dp.toPx(), cy + i * 25.dp.toPx()),
                                        strokeWidth = 1.dp.toPx()
                                    )
                                }

                                // Vibrant green/cyan laser maze trajectory
                                val mazePath = Path().apply {
                                    moveTo(cx - 60.dp.toPx(), cy - 30.dp.toPx())
                                    lineTo(cx - 20.dp.toPx(), cy - 30.dp.toPx())
                                    lineTo(cx - 20.dp.toPx(), cy + 30.dp.toPx())
                                    lineTo(cx + 25.dp.toPx(), cy + 30.dp.toPx())
                                    lineTo(cx + 25.dp.toPx(), cy - 10.dp.toPx())
                                    lineTo(cx + 65.dp.toPx(), cy - 10.dp.toPx())
                                }

                                drawPath(
                                    path = mazePath,
                                    color = Color(0xFF00FF87).copy(alpha = 0.3f),
                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawPath(
                                    path = mazePath,
                                    color = Color(0xFF00FF87),
                                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Start & End Markers
                                drawCircle(color = Color(0xFF00FF87), radius = 8.dp.toPx(), center = Offset(cx - 60.dp.toPx(), cy - 30.dp.toPx()))
                                drawCircle(color = Color(0xFFFFD700), radius = 8.dp.toPx(), center = Offset(cx + 65.dp.toPx(), cy - 10.dp.toPx()))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Reward Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF241C06))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reward: +100 Coins & +1 Star",
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7 Day Streak Section
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Whatshot,
                                    contentDescription = "Streak",
                                    tint = Color(0xFFFF9E00),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "7 Day Streak",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "${playerData.streak} days active",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 7 Day Indicator Dots
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val activeDayCount = (playerData.streak % 7).let { if (it == 0 && playerData.streak > 0) 7 else it }
                            for (day in 1..7) {
                                val isDone = day <= activeDayCount
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (isDone) Color(0xFFFF9E00) else Color(0xFF161F33))
                                            .border(
                                                1.dp,
                                                if (isDone) Color(0xFFFFD700) else Color(0x33FFFFFF),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "$day",
                                                color = Color.Gray,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "D$day",
                                        color = if (isDone) Color.White else Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Big Green "Play" Button
            Button(
                onClick = { onStartDaily(GameMode.MAZE, dailyLevel) },
                enabled = !isCompletedToday,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FF87),
                    disabledContainerColor = Color(0xFF162A20)
                ),
                shape = RoundedCornerShape(22.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("play_daily_button")
            ) {
                Icon(
                    imageVector = if (isCompletedToday) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (isCompletedToday) Color.Gray else Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCompletedToday) "COMPLETED TODAY" else "PLAY",
                    color = if (isCompletedToday) Color.Gray else Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
