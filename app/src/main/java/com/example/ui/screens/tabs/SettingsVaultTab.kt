package com.example.ui.screens.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.VaultViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsVaultTab(
  viewModel: VaultViewModel,
  securityConfig: SecurityConfig,
  breakInLogs: List<BreakInAttempt>,
  onTestBiometrics: () -> Unit,
  onOpenPermissionsDialog: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showMasterPasswordDialog by remember { mutableStateOf(false) }
  var showDecoyPasswordDialog by remember { mutableStateOf(false) }
  var showLockTypeDialog by remember { mutableStateOf(false) }
  var showDisguiseSkinDialog by remember { mutableStateOf(false) }
  var showDisguiseIconDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Password & Lock Type Configuration
    item {
      Text(
        text = "Password & Decoy Vault Protection",
        color = NeonVioletLight,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
      )

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = VaultSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderHighlight)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Lock Mode Row (Alphanumeric Password vs PIN)
          SettingItemRow(
            icon = Icons.Default.LockReset,
            iconTint = NeonCyanLight,
            title = "Lock Type",
            subtitle = "Active: ${securityConfig.lockType.label}",
            actionText = "Switch",
            onClick = { showLockTypeDialog = true },
            testTag = "switch_lock_type_btn"
          )

          HorizontalDivider(color = BorderHighlight.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

          // Master Password Row
          SettingItemRow(
            icon = Icons.Default.Key,
            iconTint = NeonVioletLight,
            title = "Master Vault Password",
            subtitle = "Unlocks your true encrypted vault",
            actionText = "Change",
            onClick = { showMasterPasswordDialog = true },
            testTag = "change_master_password_btn"
          )

          HorizontalDivider(color = BorderHighlight.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

          // Fake Decoy Password Row
          SettingItemRow(
            icon = Icons.Default.Masks,
            iconTint = NeonAmber,
            title = "Fake Decoy Password",
            subtitle = "Unlocks innocent decoy vault for complete discretion",
            actionText = "Change",
            onClick = { showDecoyPasswordDialog = true },
            testTag = "change_decoy_password_btn"
          )

          Spacer(modifier = Modifier.height(8.dp))

          Surface(
            color = NeonAmber.copy(alpha = 0.12f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "💡 You can set ANY password you want — including words, numbers, and symbols. If someone demands access to your phone, enter your Fake Password to reveal the decoy vault while keeping your real files completely hidden.",
              color = NeonAmber,
              fontSize = 11.sp,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      }
    }

    // 2. Biometric Security Section
    item {
      Text(
        text = "Biometric Authentication",
        color = NeonCyanLight,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
      )

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = VaultSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderHighlight)
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
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(NeonCyan.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Fingerprint,
                  contentDescription = null,
                  tint = NeonCyanLight,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Biometric Lock",
                  color = TextPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "Unlock Real Vault using fingerprint or face",
                  color = TextSecondary,
                  fontSize = 12.sp
                )
              }
            }

            Switch(
              checked = securityConfig.biometricsEnabled,
              onCheckedChange = { viewModel.setBiometricsEnabled(it) },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonCyan),
              modifier = Modifier.testTag("toggle_biometrics_switch")
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = onTestBiometrics,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxWidth().testTag("test_biometrics_btn")
          ) {
            Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = NeonCyanLight, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Test Biometric Sensor", color = NeonCyanLight, fontSize = 12.sp)
          }
        }
      }
    }

    // 3. Camouflage & Disguise Section
    item {
      Text(
        text = "Camouflage & Discretion",
        color = NeonEmeraldLight,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
      )

      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = VaultSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderHighlight)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          // Disguise Screen Skin
          SettingItemRow(
            icon = Icons.Default.Visibility,
            iconTint = NeonEmeraldLight,
            title = "Disguise Lock Screen",
            subtitle = "Active: ${securityConfig.disguiseSkin.displayName}",
            actionText = "Select",
            onClick = { showDisguiseSkinDialog = true },
            testTag = "select_disguise_skin_btn"
          )

          HorizontalDivider(color = BorderHighlight.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 10.dp))

          // App Icon & Name Disguise Selector
          SettingItemRow(
            icon = Icons.Default.AppShortcut,
            iconTint = NeonVioletLight,
            title = "Change App Icon & Name",
            subtitle = "${securityConfig.selectedIcon.iconEmoji} ${securityConfig.selectedIcon.label}",
            actionText = "Change",
            onClick = { showDisguiseIconDialog = true },
            testTag = "change_app_icon_name_btn"
          )
        }
      }
    }

    // 4. Permissions & Protection Center
    item {
      Text(
        text = "System Permissions Center",
        color = NeonRose,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
      )

      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onOpenPermissionsDialog() }
          .testTag("open_permissions_center_btn"),
        shape = RoundedCornerShape(16.dp),
        color = VaultSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderHighlight)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(NeonRose.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = NeonRose, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text("Permissions & Security Setup", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("Media Storage, Usage Access & Overlay Lock", color = TextSecondary, fontSize = 12.sp)
          }
          Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
        }
      }
    }

    // 5. Break-In Intruder Logs
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Intruder & Break-In Logs (${breakInLogs.size})",
          color = TextSecondary,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(start = 4.dp)
        )
        if (breakInLogs.isNotEmpty()) {
          TextButton(onClick = { viewModel.clearBreakInLogs() }) {
            Text("Clear", color = TextTertiary, fontSize = 12.sp)
          }
        }
      }

      if (breakInLogs.isEmpty()) {
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          color = VaultSurfaceCard
        ) {
          Text(
            text = "No unauthorized attempts recorded. All security checks normal.",
            color = TextTertiary,
            fontSize = 12.sp,
            modifier = Modifier.padding(14.dp)
          )
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          breakInLogs.take(5).forEach { log ->
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              color = VaultSurfaceCard,
              border = androidx.compose.foundation.BorderStroke(1.dp, NeonRose.copy(alpha = 0.3f))
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = NeonRose, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text("Wrong Code: ${log.enteredPin}", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                  Text(
                    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(log.timestamp)),
                    color = TextTertiary,
                    fontSize = 11.sp
                  )
                }
                StatusBadge(text = "ALERT", color = NeonRose)
              }
            }
          }
        }
      }
    }
  }

  // Master Password Dialog (Supports Any Custom Password)
  if (showMasterPasswordDialog) {
    CustomPasswordEditDialog(
      title = "Set Master Vault Password",
      subtitle = "Create ANY password you want (letters, numbers, symbols or PIN)",
      currentValue = securityConfig.masterPin,
      onConfirm = {
        viewModel.updateMasterPassword(it)
        showMasterPasswordDialog = false
      },
      onDismiss = { showMasterPasswordDialog = false }
    )
  }

  // Fake Decoy Password Dialog
  if (showDecoyPasswordDialog) {
    CustomPasswordEditDialog(
      title = "Set Fake Decoy Password",
      subtitle = "Entering this password unlocks innocent camouflage files",
      currentValue = securityConfig.decoyPin,
      onConfirm = {
        viewModel.updateDecoyPassword(it)
        showDecoyPasswordDialog = false
      },
      onDismiss = { showDecoyPasswordDialog = false }
    )
  }

  // Lock Type Switcher Dialog
  if (showLockTypeDialog) {
    AlertDialog(
      onDismissRequest = { showLockTypeDialog = false },
      title = { Text("Choose Preferred Lock Mode", color = TextPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          LockType.values().forEach { type ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  viewModel.setLockType(type)
                  showLockTypeDialog = false
                },
              color = if (securityConfig.lockType == type) NeonCyan.copy(alpha = 0.2f) else VaultSurfaceElevated
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = securityConfig.lockType == type,
                  onClick = {
                    viewModel.setLockType(type)
                    showLockTypeDialog = false
                  }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(type.label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                  Text(type.description, color = TextSecondary, fontSize = 11.sp)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showLockTypeDialog = false }) {
          Text("Close", color = TextSecondary)
        }
      },
      containerColor = VaultSurfaceCard
    )
  }

  // Disguise Skin Dialog
  if (showDisguiseSkinDialog) {
    AlertDialog(
      onDismissRequest = { showDisguiseSkinDialog = false },
      title = { Text("Choose Disguise Lock Screen", color = TextPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          DisguiseSkin.values().forEach { skin ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  viewModel.setDisguiseSkin(skin)
                  showDisguiseSkinDialog = false
                },
              color = if (securityConfig.disguiseSkin == skin) NeonViolet.copy(alpha = 0.2f) else VaultSurfaceElevated
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = securityConfig.disguiseSkin == skin,
                  onClick = {
                    viewModel.setDisguiseSkin(skin)
                    showDisguiseSkinDialog = false
                  }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(skin.displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                  Text(skin.subtitle, color = TextSecondary, fontSize = 11.sp)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showDisguiseSkinDialog = false }) {
          Text("Cancel", color = TextSecondary)
        }
      },
      containerColor = VaultSurfaceCard
    )
  }

  // Disguise Icon Dialog
  if (showDisguiseIconDialog) {
    AlertDialog(
      onDismissRequest = { showDisguiseIconDialog = false },
      title = { Text("Change App Icon & Disguise Name", color = TextPrimary) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          DisguiseIconOption.values().forEach { iconOption ->
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  viewModel.setDisguiseIcon(iconOption)
                  showDisguiseIconDialog = false
                },
              color = if (securityConfig.selectedIcon == iconOption) NeonCyan.copy(alpha = 0.2f) else VaultSurfaceElevated
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = iconOption.iconEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(iconOption.label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                  Text(iconOption.subtitle, color = TextSecondary, fontSize = 11.sp)
                }
                if (securityConfig.selectedIcon == iconOption) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = NeonCyanLight)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showDisguiseIconDialog = false }) {
          Text("Close", color = TextSecondary)
        }
      },
      containerColor = VaultSurfaceCard
    )
  }
}

@Composable
private fun SettingItemRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  actionText: String,
  onClick: () -> Unit,
  testTag: String
) {
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
          .size(38.dp)
          .clip(CircleShape)
          .background(iconTint.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = TextSecondary, fontSize = 11.sp)
      }
    }

    Button(
      onClick = onClick,
      colors = ButtonDefaults.buttonColors(containerColor = iconTint.copy(alpha = 0.2f)),
      shape = RoundedCornerShape(8.dp),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
      modifier = Modifier.testTag(testTag)
    ) {
      Text(actionText, color = iconTint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
private fun CustomPasswordEditDialog(
  title: String,
  subtitle: String,
  currentValue: String,
  onConfirm: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var passwordText by remember { mutableStateOf("") }
  var confirmPasswordText by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(subtitle, color = TextSecondary, fontSize = 12.sp)

        OutlinedTextField(
          value = passwordText,
          onValueChange = {
            passwordText = it
            error = null
          },
          label = { Text("New Custom Password / PIN") },
          placeholder = { Text("Any text, numbers or symbols") },
          singleLine = true,
          visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
              Icon(
                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null,
                tint = TextSecondary
              )
            }
          },
          modifier = Modifier.fillMaxWidth().testTag("custom_new_password_input")
        )

        OutlinedTextField(
          value = confirmPasswordText,
          onValueChange = {
            confirmPasswordText = it
            error = null
          },
          label = { Text("Confirm Password / PIN") },
          singleLine = true,
          visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth().testTag("custom_confirm_password_input")
        )

        if (error != null) {
          Text(error ?: "", color = NeonRose, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (passwordText.length < 3) {
            error = "Password must be at least 3 characters"
          } else if (passwordText != confirmPasswordText) {
            error = "Passwords do not match"
          } else {
            onConfirm(passwordText)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
        modifier = Modifier.testTag("save_custom_password_btn")
      ) {
        Text("Save Password")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = VaultSurfaceCard
  )
}
