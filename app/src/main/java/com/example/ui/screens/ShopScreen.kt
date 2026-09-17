package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.services.IAPService

enum class ShopTab {
    COINS, HINTS, UNDO, THEMES
}

/**
 * Shop screen strictly matching Panel 14 in design reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    playerData: PlayerData,
    theme: GameTheme,
    prefsManager: PreferencesManager,
    soundManager: SoundManager,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(ShopTab.COINS) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color(0xFF070B16),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Shop",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("shop_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF10192D))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.MonetizationOn,
                            contentDescription = "Coins",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${playerData.coins}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
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
            // Segmented Tabs: Coins | Hints | Undo | Themes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0D1424))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ShopTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color.Transparent)
                            .then(if (isSelected) Modifier.border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(10.dp)) else Modifier)
                            .clickable { selectedTab = tab }
                            .testTag("tab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (tab) {
                                ShopTab.COINS -> "Coins"
                                ShopTab.HINTS -> "Hints"
                                ShopTab.UNDO -> "Undo"
                                ShopTab.THEMES -> "Themes"
                            },
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Premium Banner: No Ads + Extra Rewards
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F36)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.2.dp,
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFFFD700).copy(alpha = 0.7f), Color(0xFF00E5FF).copy(alpha = 0.5f))
                                ),
                                RoundedCornerShape(18.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF332906)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "VIP Arrow Pass",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Unlimited Lives + 2x Coin Rewards",
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    prefsManager.save(playerData.copy(lives = 99))
                                    soundManager.playSuccess()
                                    snackbarMessage = "VIP Activated: Unlimited Lives!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Get", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                when (selectedTab) {
                    ShopTab.COINS -> {
                        item {
                            Text("COIN PACKS", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        items(IAPService.products) { product ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF261E08)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.MonetizationOn,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD700),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(product.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(product.description, color = Color.Gray, fontSize = 11.sp)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            IAPService.purchase(product.sku) {
                                                prefsManager.addCoins(it.coinsReward)
                                                soundManager.playCoin()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16223B)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(product.priceFormatted, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    ShopTab.HINTS -> {
                        item {
                            Text("HINT BUNDLES", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        item {
                            PowerUpShopCard(
                                title = "3 Hints Pack",
                                description = "Reveals the next free arrow to escape",
                                icon = Icons.Default.Lightbulb,
                                iconColor = Color(0xFF00E5FF),
                                costCoins = 80,
                                userCoins = playerData.coins,
                                onBuy = {
                                    if (playerData.coins >= 80) {
                                        prefsManager.save(playerData.copy(coins = playerData.coins - 80, hints = playerData.hints + 3))
                                        soundManager.playCoin()
                                    }
                                }
                            )
                        }
                        item {
                            PowerUpShopCard(
                                title = "10 Hints Vault",
                                description = "Best value hint bundle for difficult levels",
                                icon = Icons.Default.Lightbulb,
                                iconColor = Color(0xFFFFD700),
                                costCoins = 220,
                                userCoins = playerData.coins,
                                onBuy = {
                                    if (playerData.coins >= 220) {
                                        prefsManager.save(playerData.copy(coins = playerData.coins - 220, hints = playerData.hints + 10))
                                        soundManager.playCoin()
                                    }
                                }
                            )
                        }
                    }

                    ShopTab.UNDO -> {
                        item {
                            Text("UNDO BUNDLES", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        item {
                            PowerUpShopCard(
                                title = "5 Undos Pack",
                                description = "Step back safely after an obstructed tap",
                                icon = Icons.Default.Undo,
                                iconColor = Color(0xFF00FF87),
                                costCoins = 50,
                                userCoins = playerData.coins,
                                onBuy = {
                                    if (playerData.coins >= 50) {
                                        prefsManager.save(playerData.copy(coins = playerData.coins - 50, undos = playerData.undos + 5))
                                        soundManager.playCoin()
                                    }
                                }
                            )
                        }
                        item {
                            PowerUpShopCard(
                                title = "15 Undos Vault",
                                description = "Full safety net for complex labyrinths",
                                icon = Icons.Default.Undo,
                                iconColor = Color(0xFFB066FF),
                                costCoins = 120,
                                userCoins = playerData.coins,
                                onBuy = {
                                    if (playerData.coins >= 120) {
                                        prefsManager.save(playerData.copy(coins = playerData.coins - 120, undos = playerData.undos + 15))
                                        soundManager.playCoin()
                                    }
                                }
                            )
                        }
                    }

                    ShopTab.THEMES -> {
                        item {
                            Text("THEME SKINS", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        items(GameTheme.values()) { t ->
                            val isUnlocked = playerData.unlockedThemeIds.contains(t.id)
                            val isEquipped = playerData.activeThemeId == t.id

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, if (isEquipped) t.primary else Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(t.background)
                                                .padding(6.dp)
                                        ) {
                                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(t.primary))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(t.accent))
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(t.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(if (isEquipped) "Active" else if (isUnlocked) "Unlocked" else "${t.cost} Coins", color = Color.Gray, fontSize = 11.sp)
                                        }
                                    }

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
                                            containerColor = if (isEquipped) Color(0xFF1E293B) else if (isUnlocked) t.primary else Color(0xFFD97706)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = if (isEquipped) "Active" else if (isUnlocked) "Equip" else "Unlock",
                                            color = if (isEquipped) Color.Gray else if (isUnlocked) Color.Black else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Restore Purchases Link
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = { snackbarMessage = "Purchases restored successfully." }) {
                            Text("Restore Purchases", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PowerUpShopCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    costCoins: Int,
    userCoins: Int,
    onBuy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.15f))
                        .border(1.dp, iconColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(description, color = Color.Gray, fontSize = 11.sp)
                }
            }

            Button(
                onClick = onBuy,
                enabled = userCoins >= costCoins,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("$costCoins Coins", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
