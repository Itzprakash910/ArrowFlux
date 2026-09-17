package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Level Complete Dialog strictly matching Panel 9 of design reference.
 */
@Composable
fun LevelCompleteDialog(
    stars: Int,
    moves: Int,
    mistakes: Int,
    hintsUsed: Int,
    coinsEarned: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onLevelSelect: () -> Unit
) {
    var dialogVisible by remember { mutableStateOf(false) }
    var star1Visible by remember { mutableStateOf(false) }
    var star2Visible by remember { mutableStateOf(false) }
    var star3Visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dialogVisible = true
        kotlinx.coroutines.delay(120)
        star1Visible = true
        kotlinx.coroutines.delay(160)
        star2Visible = true
        kotlinx.coroutines.delay(160)
        star3Visible = true
    }

    val dialogScale by animateFloatAsState(
        targetValue = if (dialogVisible) 1.0f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "dialogScale"
    )

    val star1Scale by animateFloatAsState(
        targetValue = if (star1Visible && stars >= 1) 1.0f else 0.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "star1"
    )
    val star2Scale by animateFloatAsState(
        targetValue = if (star2Visible && stars >= 2) 1.0f else 0.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "star2"
    )
    val star3Scale by animateFloatAsState(
        targetValue = if (star3Visible && stars >= 3) 1.0f else 0.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "star3"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "star_glow")
    val starPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starScale"
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(dialogScale)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F1B38),
                            Color(0xFF080D1D),
                            Color(0xFF050812)
                        )
                    )
                )
                .border(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFFD700).copy(alpha = 0.7f),
                            Color(0xFF00FF87).copy(alpha = 0.4f),
                            Color(0xFF00E5FF).copy(alpha = 0.6f)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 3 Big Golden Glowing Stars
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Left Star
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star 1",
                        tint = if (stars >= 1) Color(0xFFFFD700) else Color(0xFF2A3756),
                        modifier = Modifier
                            .size(54.dp)
                            .padding(bottom = 6.dp)
                            .scale(star1Scale)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // Center Star (Larger & Elevated)
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star 2",
                        tint = if (stars >= 2) Color(0xFFFFD700) else Color(0xFF2A3756),
                        modifier = Modifier
                            .size(72.dp)
                            .scale(star2Scale * starPulse)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // Right Star
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Star 3",
                        tint = if (stars >= 3) Color(0xFFFFD700) else Color(0xFF2A3756),
                        modifier = Modifier
                            .size(54.dp)
                            .padding(bottom = 6.dp)
                            .scale(star3Scale)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Title
                Text(
                    text = "LEVEL COMPLETE!",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Card: Moves | Mistakes | Hints
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF131D36))
                        .border(1.dp, Color(0xFF24335C), RoundedCornerShape(18.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(label = "Moves", value = "$moves")
                        Divider(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp),
                            color = Color(0xFF24335C)
                        )
                        StatItem(label = "Mistakes", value = "$mistakes")
                        Divider(
                            modifier = Modifier
                                .height(28.dp)
                                .width(1.dp),
                            color = Color(0xFF24335C)
                        )
                        StatItem(label = "Hints", value = "$hintsUsed")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Coins Reward Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF26200A))
                        .border(1.2.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = "Coin",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+$coinsEarned Coins",
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Next Level Button (Vibrant Green Gradient)
                Button(
                    onClick = onNextLevel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF87)),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_level_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Next Level",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Replay Button (Blue Pill)
                Button(
                    onClick = onReplay,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E40AF)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("replay_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Replay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Level Select (Dark Button)
                TextButton(
                    onClick = onLevelSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("level_select_button")
                ) {
                    Text("Level Select", color = Color.LightGray, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}

/**
 * Game Over Dialog strictly matching Panel 10 of design reference.
 */
@Composable
fun GameOverDialog(
    coinsAvailable: Int,
    onWatchAdReward: () -> Unit,
    onUseCoins: () -> Unit,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF280C14),
                            Color(0xFF14070A),
                            Color(0xFF0A0406)
                        )
                    )
                )
                .border(1.5.dp, Color(0xFFFF2A6D).copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Red Glow Title
                Text(
                    text = "GAME OVER",
                    color = Color(0xFFFF2A6D),
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    letterSpacing = 2.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Dizzy face icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B121D))
                        .border(1.5.dp, Color(0xFFFF2A6D).copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "(x x)", color = Color(0xFFFF2A6D), fontSize = 22.sp, fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Out of Lives!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "You need more lives to continue solving.",
                    color = Color.LightGray.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Watch Reward +1 Life (Free Refill)
                Button(
                    onClick = onWatchAdReward,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB703)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("free_life_reward_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "▶ Free Refill +3 Lives",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Use Coins - 50 (Refill Lives)
                Button(
                    onClick = onUseCoins,
                    enabled = coinsAvailable >= 50,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD97706),
                        disabledContainerColor = Color(0xFF332014)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("use_coins_life_button")
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Use Coins · 50 Coins",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Retry Button
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    // Home Button
                    Button(
                        onClick = onHome,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Text("Home", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
