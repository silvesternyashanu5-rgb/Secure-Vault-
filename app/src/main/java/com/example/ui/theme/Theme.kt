package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VaultDarkColorScheme = darkColorScheme(
  primary = NeonVioletLight,
  onPrimary = Color(0xFF1E1035),
  primaryContainer = Color(0xFF4C1D95),
  onPrimaryContainer = Color(0xFFEDE9FE),
  secondary = NeonCyanLight,
  onSecondary = Color(0xFF083344),
  secondaryContainer = Color(0xFF164E63),
  onSecondaryContainer = Color(0xFFCFFAFE),
  tertiary = NeonEmeraldLight,
  onTertiary = Color(0xFF022C22),
  tertiaryContainer = Color(0xFF064E3B),
  onTertiaryContainer = Color(0xFFD1FAE5),
  background = VaultObsidian,
  onBackground = TextPrimary,
  surface = VaultSurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = VaultSurfaceCard,
  onSurfaceVariant = TextSecondary,
  outline = BorderHighlight,
  error = NeonRose,
  onError = Color.White
)

private val VaultLightColorScheme = lightColorScheme(
  primary = NeonViolet,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFEDE9FE),
  onPrimaryContainer = Color(0xFF3B0764),
  secondary = NeonCyan,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFCFFAFE),
  onSecondaryContainer = Color(0xFF083344),
  tertiary = NeonEmerald,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFD1FAE5),
  onTertiaryContainer = Color(0xFF022C22),
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF0F172A),
  surface = Color.White,
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFE2E8F0),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFCBD5E1),
  error = NeonRose,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Vault default dark for high-tech security atmosphere
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) VaultDarkColorScheme else VaultLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
