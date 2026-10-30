package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SecurityConfig
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.util.DeviceAppHelper

@Composable
fun PermissionsSheet(
  securityConfig: SecurityConfig,
  onRequestMediaPermission: () -> Unit,
  onRefreshPermissions: () -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val isUsageGranted = securityConfig.isUsageAccessGranted
  val isOverlayGranted = securityConfig.isOverlayPermissionGranted

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.88f)
        .clip(RoundedCornerShape(24.dp))
        .border(1.dp, BorderHighlight, RoundedCornerShape(24.dp)),
      color = VaultSurfaceDark
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        // Top Title
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NeonViolet.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = NeonVioletLight,
                modifier = Modifier.size(22.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Security Permissions",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Required for App Lock & Vault protection",
                color = TextSecondary,
                fontSize = 12.sp
              )
            }
          }

          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // 1. Usage Stats Permission
          PermissionCard(
            icon = Icons.Default.QueryStats,
            iconColor = NeonVioletLight,
            title = "Usage Access (App Detection)",
            description = "Allows SecureVault to monitor which app is currently running in the foreground to instantly trigger the lock screen when a hidden app is opened.",
            isGranted = isUsageGranted,
            actionLabel = if (isUsageGranted) "Granted" else "Open Usage Settings",
            onClick = {
              DeviceAppHelper.openUsageAccessSettings(context)
              onRefreshPermissions()
            },
            testTag = "grant_usage_access_btn"
          )

          // 2. Display Over Other Apps / Overlay
          PermissionCard(
            icon = Icons.Default.Layers,
            iconColor = NeonCyanLight,
            title = "Display Over Other Apps (Overlay)",
            description = "Allows SecureVault to draw its secure PIN keypad or fake crash camouflage over locked apps to block prying eyes.",
            isGranted = isOverlayGranted,
            actionLabel = if (isOverlayGranted) "Granted" else "Open Overlay Settings",
            onClick = {
              DeviceAppHelper.openOverlaySettings(context)
              onRefreshPermissions()
            },
            testTag = "grant_overlay_btn"
          )

          // 3. Media & Storage
          PermissionCard(
            icon = Icons.Default.PermMedia,
            iconColor = NeonEmeraldLight,
            title = "Media Storage (Photos, Videos & Audio)",
            description = "Allows SecureVault to read your chosen photos, videos, and music files from the device and transfer them into encrypted private storage.",
            isGranted = securityConfig.isMediaPermissionGranted,
            actionLabel = if (securityConfig.isMediaPermissionGranted) "Granted" else "Grant Media Access",
            onClick = { onRequestMediaPermission() },
            testTag = "grant_media_perm_btn"
          )

          // 4. Biometric Protection
          PermissionCard(
            icon = Icons.Default.Fingerprint,
            iconColor = NeonAmber,
            title = "Biometric Authentication",
            description = "Uses Android Keymaster & BiometricPrompt to protect your vault with fingerprint or facial unlock as an extra layer of protection.",
            isGranted = securityConfig.biometricsEnabled,
            actionLabel = if (securityConfig.biometricsEnabled) "Active" else "Enable in Settings",
            onClick = {},
            testTag = "biometric_info_badge"
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Action Button
        Button(
          onClick = {
            onRefreshPermissions()
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("permissions_done_btn")
        ) {
          Text("Done & Check Status", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun PermissionCard(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconColor: Color,
  title: String,
  description: String,
  isGranted: Boolean,
  actionLabel: String,
  onClick: () -> Unit,
  testTag: String
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = VaultSurfaceCard,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isGranted) NeonEmerald.copy(alpha = 0.4f) else BorderHighlight
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(iconColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        StatusBadge(
          text = if (isGranted) "GRANTED" else "ACTION NEEDED",
          color = if (isGranted) NeonEmeraldLight else NeonAmber
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = description,
        color = TextSecondary,
        fontSize = 11.sp,
        lineHeight = 16.sp
      )

      if (!isGranted) {
        Spacer(modifier = Modifier.height(10.dp))
        Button(
          onClick = onClick,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = iconColor.copy(alpha = 0.25f)),
          modifier = Modifier.fillMaxWidth().testTag(testTag)
        ) {
          Text(actionLabel, color = iconColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
