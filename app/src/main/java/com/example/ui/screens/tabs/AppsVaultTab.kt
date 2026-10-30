package com.example.ui.screens.tabs

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SecurityConfig
import com.example.data.model.VaultAppItem
import com.example.ui.VaultViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun AppsVaultTab(
  viewModel: VaultViewModel,
  protectedApps: List<VaultAppItem>,
  securityConfig: SecurityConfig,
  onOpenSelectAppsDialog: () -> Unit,
  onOpenPermissionsDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  val allPermissionsGranted = securityConfig.isUsageAccessGranted && securityConfig.isOverlayPermissionGranted

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    // Explanation Banner: Moving Apps from Home Screen Layer to App
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 10.dp, bottom = 8.dp),
      shape = RoundedCornerShape(16.dp),
      color = VaultSurfaceCard,
      border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.35f))
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(NeonViolet.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = NeonVioletLight,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Home Screen App Hider",
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Apps moved here are hidden from standard home screen & run in vault sandbox",
            color = TextSecondary,
            fontSize = 11.sp
          )
        }
      }
    }

    // Permission Setup Status Banner
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 10.dp)
        .clickable { onOpenPermissionsDialog() }
        .testTag("app_lock_permission_banner"),
      shape = RoundedCornerShape(14.dp),
      color = if (allPermissionsGranted) NeonEmerald.copy(alpha = 0.12f) else NeonAmber.copy(alpha = 0.15f),
      border = androidx.compose.foundation.BorderStroke(
        1.dp,
        if (allPermissionsGranted) NeonEmerald.copy(alpha = 0.4f) else NeonAmber.copy(alpha = 0.5f)
      )
    ) {
      Row(
        modifier = Modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = if (allPermissionsGranted) Icons.Default.VerifiedUser else Icons.Default.WarningAmber,
          contentDescription = null,
          tint = if (allPermissionsGranted) NeonEmeraldLight else NeonAmber,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (allPermissionsGranted) "Protection Service Active" else "Permissions Setup Required",
            color = if (allPermissionsGranted) NeonEmeraldLight else NeonAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (allPermissionsGranted)
              "Usage Access & Overlay enabled for real-time background app locking"
            else
              "Tap to grant Usage Access & Overlay to detect & lock apps",
            color = TextSecondary,
            fontSize = 10.sp
          )
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
      }
    }

    // Action button: Move app from Home Screen into Vault
    Button(
      onClick = onOpenSelectAppsDialog,
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp)
        .testTag("hide_new_app_btn"),
      colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
      shape = RoundedCornerShape(14.dp)
    ) {
      Icon(imageVector = Icons.Default.AddModerator, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Move App from Home Screen to Vault", fontWeight = FontWeight.SemiBold)
    }

    // List of protected apps
    if (protectedApps.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.LockReset,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "No applications moved to Vault yet",
            color = TextSecondary,
            fontSize = 15.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Tap above to select apps and move them into this private layer",
            color = TextTertiary,
            fontSize = 13.sp
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(protectedApps, key = { it.packageName }) { app ->
          ProtectedAppCard(
            app = app,
            onLaunch = { viewModel.launchHiddenApp(app) },
            onToggleLock = { viewModel.toggleAppLock(app) },
            onToggleCrash = { viewModel.toggleAppFakeCrash(app) },
            onOpenSystemSettings = { viewModel.openAppSystemSettings(app) },
            onRestoreToHomeScreen = { viewModel.restoreAppToHomeScreen(app) }
          )
        }
      }
    }
  }
}

@Composable
private fun ProtectedAppCard(
  app: VaultAppItem,
  onLaunch: () -> Unit,
  onToggleLock: () -> Unit,
  onToggleCrash: () -> Unit,
  onOpenSystemSettings: () -> Unit,
  onRestoreToHomeScreen: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(
        1.dp,
        if (app.isLocked) NeonViolet.copy(alpha = 0.4f) else BorderHighlight,
        RoundedCornerShape(16.dp)
      )
      .testTag("app_item_${app.packageName}"),
    color = VaultSurfaceCard
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // App icon badge
        Box(
          modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(VaultSurfaceElevated),
          contentAlignment = Alignment.Center
        ) {
          Text(text = app.iconEmoji, fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = app.appName,
              color = TextPrimary,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            StatusBadge(text = "VAULT ONLY", color = NeonCyanLight)
          }
          Text(
            text = "Hidden from Home Screen",
            color = NeonEmeraldLight,
            fontSize = 11.sp
          )
        }

        // Direct Open In App Button (opens without unhiding!)
        Button(
          onClick = onLaunch,
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.testTag("launch_app_${app.packageName}")
        ) {
          Icon(
            imageVector = Icons.Default.Launch,
            contentDescription = "Open App",
            tint = NeonCyanLight,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Open",
            color = NeonCyanLight,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = BorderHighlight.copy(alpha = 0.5f), thickness = 0.8.dp)
      Spacer(modifier = Modifier.height(8.dp))

      // App protection controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Lock Switch
        Row(verticalAlignment = Alignment.CenterVertically) {
          Switch(
            checked = app.isLocked,
            onCheckedChange = { onToggleLock() },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = NeonViolet
            ),
            modifier = Modifier.scale(0.85f).testTag("switch_lock_${app.packageName}")
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (app.isLocked) "Locked" else "Unlocked",
            color = if (app.isLocked) NeonVioletLight else TextSecondary,
            fontSize = 12.sp
          )
        }

        // Hide on Home Screen action (opens Android settings for quick disable/hide)
        TextButton(
          onClick = onOpenSystemSettings,
          modifier = Modifier.testTag("btn_hide_homescreen_${app.packageName}")
        ) {
          Icon(
            imageVector = Icons.Default.VisibilityOff,
            contentDescription = null,
            tint = NeonVioletLight,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Hide on Home", color = NeonVioletLight, fontSize = 11.sp)
        }

        // Restore back to Home Screen
        TextButton(
          onClick = onRestoreToHomeScreen,
          modifier = Modifier.testTag("restore_app_${app.packageName}")
        ) {
          Icon(
            imageVector = Icons.Default.Undo,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text("Restore", color = TextSecondary, fontSize = 11.sp)
        }
      }
    }
  }
}
