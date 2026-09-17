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
import com.example.audio.SoundManager
import com.example.haptics.HapticManager
import com.example.model.GameTheme
import com.example.model.PlayerData
import com.example.model.PreferencesManager

/**
 * Settings screen strictly matching Panel 15 in design reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    playerData: PlayerData,
    theme: GameTheme,
    prefsManager: PreferencesManager,
    soundManager: SoundManager,
    hapticManager: HapticManager,
    onBack: () -> Unit
) {
    var showResetDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    var soundEffects by remember { mutableStateOf(playerData.soundEnabled) }
    var musicEnabled by remember { mutableStateOf(soundManager.isMusicEnabled) }
    var vibrationEnabled by remember { mutableStateOf(playerData.vibrationEnabled) }
    var reducedMotion by remember { mutableStateOf(playerData.reducedMotion) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("English") }

    Scaffold(
        containerColor = Color(0xFF070B16),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
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
            // Toggles Card (Sound, Music, Vibration, Reduced Motion, Notifications)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        SettingsToggleRow(
                            title = "Sound",
                            icon = Icons.Default.VolumeUp,
                            iconColor = Color(0xFF00E5FF),
                            checked = soundEffects,
                            onCheckedChange = {
                                soundEffects = it
                                soundManager.isSoundEnabled = it
                                prefsManager.save(playerData.copy(soundEnabled = it))
                            }
                        )
                        Divider(color = Color(0x15FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsToggleRow(
                            title = "Music",
                            icon = Icons.Default.MusicNote,
                            iconColor = Color(0xFFB066FF),
                            checked = musicEnabled,
                            onCheckedChange = {
                                musicEnabled = it
                                soundManager.isMusicEnabled = it
                            }
                        )
                        Divider(color = Color(0x15FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsToggleRow(
                            title = "Vibration",
                            icon = Icons.Default.Vibration,
                            iconColor = Color(0xFF00FF87),
                            checked = vibrationEnabled,
                            onCheckedChange = {
                                vibrationEnabled = it
                                hapticManager.isVibrationEnabled = it
                                prefsManager.save(playerData.copy(vibrationEnabled = it))
                            }
                        )
                        Divider(color = Color(0x15FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsToggleRow(
                            title = "Reduced Motion",
                            icon = Icons.Default.Visibility,
                            iconColor = Color(0xFFFF9E00),
                            checked = reducedMotion,
                            onCheckedChange = {
                                reducedMotion = it
                                prefsManager.save(playerData.copy(reducedMotion = it))
                            }
                        )
                        Divider(color = Color(0x15FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsToggleRow(
                            title = "Notifications",
                            icon = Icons.Default.Notifications,
                            iconColor = Color(0xFFFF2A6D),
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }
            }

            // General Navigation Rows (Language, Privacy, About)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1424)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsNavRow(
                            title = "Language",
                            trailingText = selectedLanguage,
                            icon = Icons.Default.Language,
                            iconColor = Color(0xFF38BDF8),
                            onClick = { showLanguageDialog = true }
                        )
                        Divider(color = Color(0x15FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsNavRow(
                            title = "Privacy",
                            trailingText = null,
                            icon = Icons.Default.Security,
                            iconColor = Color(0xFF00FF87),
                            onClick = { showPrivacyDialog = true }
                        )
                        Divider(color = Color(0x15FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsNavRow(
                            title = "About",
                            trailingText = "v1.0.0",
                            icon = Icons.Default.Info,
                            iconColor = Color(0xFFFFD700),
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }

            // Red Outlined Pill Button: Reset Progress (from Panel 15)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFF2A6D)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF2A6D)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("reset_progress_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFFF2A6D), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Progress",
                        color = Color(0xFFFF2A6D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("English", "Hindi (हिन्दी)", "Spanish (Español)", "German (Deutsch)", "French (Français)").forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = lang.split(" ")[0]
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang, color = Color.White, fontSize = 14.sp)
                            if (selectedLanguage == lang.split(" ")[0]) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF00E5FF))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF0D1424)
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "ArrowFlux respects user privacy:\n\n• 100% Offline gameplay capability.\n• No tracking, telemetry, or selling of personal information.\n• All puzzle saves, stars, coins, and levels are safely stored locally on your device.",
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = Color(0xFF00E5FF))
                }
            },
            containerColor = Color(0xFF0D1424)
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("ArrowFlux - Think. Rotate. Escape.", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Engine: Jetpack Compose + Modern Android", color = Color.LightGray, fontSize = 13.sp)
                    Text("Game Modes: Classic 2D, Neon Maze, 3D Rotating Cube", color = Color.LightGray, fontSize = 13.sp)
                    Text("Sound: Realtime Synthesized Audio Engine", color = Color.LightGray, fontSize = 13.sp)
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

    // Reset Progress Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Progress?", color = Color(0xFFFF2A6D), fontWeight = FontWeight.Bold) },
            text = {
                Text("This will delete all completed levels, stars, coins, and inventory. This action cannot be undone.", color = Color.LightGray)
            },
            confirmButton = {
                Button(
                    onClick = {
                        prefsManager.resetProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF2A6D))
                ) {
                    Text("Confirm Reset", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF0D1424)
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00E5FF),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF1E293B)
            )
        )
    }
}

@Composable
private fun SettingsNavRow(
    title: String,
    trailingText: String?,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (trailingText != null) {
                Text(trailingText, color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
