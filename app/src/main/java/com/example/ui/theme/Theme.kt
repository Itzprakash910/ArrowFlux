package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArrowFluxColorScheme = darkColorScheme(
  primary = Color(0xFF00F0FF),
  secondary = Color(0xFF8B5CF6),
  tertiary = Color(0xFF00FF87),
  background = Color(0xFF070913),
  surface = Color(0xFF0F172A),
  onPrimary = Color.Black,
  onSecondary = Color.White,
  onBackground = Color.White,
  onSurface = Color.White
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(colorScheme = ArrowFluxColorScheme, typography = Typography, content = content)
}

