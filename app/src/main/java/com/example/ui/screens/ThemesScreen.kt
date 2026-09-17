package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.model.GameTheme
import com.example.model.PlayerData
import com.example.model.PreferencesManager

/**
 * Themes screen strictly matching Panel 13 in design reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemesScreen(
    playerData: PlayerData,
    theme: GameTheme,
    prefsManager: PreferencesManager,
    soundManager: SoundManager,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = Color(0xFF070B16),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Themes",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("themes_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10192D))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "${playerData.coins}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070B16))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(GameTheme.values()) { t ->
                    val isUnlocked = playerData.unlockedThemeIds.contains(t.id)
                    val isEquipped = playerData.activeThemeId == t.id

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1628)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isEquipped) 2.dp else 1.dp,
                                color = if (isEquipped) t.primary else Color(0x22FFFFFF),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                if (isUnlocked) {
                                    prefsManager.setTheme(t.id)
                                    soundManager.playTap()
                                }
                            }
                            .testTag("theme_card_${t.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Palette Color Showcase
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(t.background)
                                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(t.primary))
                                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(t.accent))
                                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(t.glow))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = t.displayName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = if (isEquipped) "Active" else if (isUnlocked) "Unlocked" else "${t.cost} Coins",
                                color = if (isEquipped) t.primary else Color.LightGray,
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (isUnlocked) {
                                        prefsManager.setTheme(t.id)
                                        soundManager.playTap()
                                    } else {
                                        if (prefsManager.purchaseTheme(t.id, t.cost)) {
                                            soundManager.playCoin()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        isEquipped -> Color(0xFF1E293B)
                                        isUnlocked -> t.primary.copy(alpha = 0.85f)
                                        else -> Color(0xFFD97706)
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                            ) {
                                if (isEquipped) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = t.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Equipped", color = t.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else if (isUnlocked) {
                                    Text("Equip", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Unlock", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Banner: Unlock All Themes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF111A30))
                    .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable {
                        // Unlock all themes for 600 coins
                        if (playerData.coins >= 600) {
                            val allIds = GameTheme.values().map { it.id }.toSet()
                            prefsManager.save(playerData.copy(coins = playerData.coins - 600, unlockedThemeIds = allIds))
                            soundManager.playSuccess()
                        }
                    }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Unlock All 8 Themes",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Special Collector's Pack",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2E2406))
                            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("600", color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
