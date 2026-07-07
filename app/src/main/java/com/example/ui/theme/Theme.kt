package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SophisticatedColorScheme = darkColorScheme(
    primary = SophisticatedPrimary,
    onPrimary = SophisticatedOnPrimary,
    secondary = SophisticatedSecondary,
    onSecondary = SophisticatedOnSecondary,
    background = SophisticatedBackground,
    onBackground = SophisticatedOnSurface,
    surface = SophisticatedSurface,
    onSurface = SophisticatedOnSurface,
    surfaceVariant = SophisticatedSurfaceVariant,
    onSurfaceVariant = SophisticatedOnSurfaceVariant,
    outline = SophisticatedOutline
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Sophisticated Dark for a premium aesthetic
  dynamicColor: Boolean = false, // Set false to preserve our tailored palette
  content: @Composable () -> Unit,
) {
  val colorScheme = SophisticatedColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
