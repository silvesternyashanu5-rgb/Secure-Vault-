package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaType
import com.example.data.model.VaultType
import com.example.ui.VaultTab
import com.example.ui.VaultViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.dialogs.*
import com.example.ui.screens.tabs.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(
  viewModel: VaultViewModel,
  onTestBiometrics: () -> Unit,
  onRequestMediaPermission: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentVaultType by viewModel.currentVaultType.collectAsState()
  val selectedTab by viewModel.selectedTab.collectAsState()
  val securityConfig by viewModel.securityConfig.collectAsState()
  val mediaList by viewModel.mediaList.collectAsState()
  val notesList by viewModel.notesList.collectAsState()
  val filesList by viewModel.filesList.collectAsState()
  val protectedApps by viewModel.protectedAppsList.collectAsState()
  val deviceApps by viewModel.deviceApps.collectAsState()
  val breakInLogs by viewModel.breakInLogs.collectAsState()
  val mediaFilter by viewModel.mediaTypeFilter.collectAsState()

  // In-App Viewer and Dialog States
  val viewingMedia by viewModel.selectedMediaForViewing.collectAsState()
  val viewingFile by viewModel.selectedFileForViewing.collectAsState()
  val editingNote by viewModel.selectedNoteForEditing.collectAsState()
  val isAddMediaOpen by viewModel.isAddMediaDialogOpen.collectAsState()
  val isAddNoteOpen by viewModel.isAddNoteDialogOpen.collectAsState()
  val isAddFileOpen by viewModel.isAddFileDialogOpen.collectAsState()
  val isSelectAppsOpen by viewModel.isSelectAppsDialogOpen.collectAsState()
  val isPermissionsOpen by viewModel.isPermissionsSheetOpen.collectAsState()

  // Music playback states
  val currentAudio by viewModel.currentPlayingAudio.collectAsState()
  val isAudioPlaying by viewModel.isAudioPlaying.collectAsState()
  val audioProgress by viewModel.audioPlaybackPosition.collectAsState()

  val isDecoy = currentVaultType == VaultType.DECOY

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = VaultObsidian,
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = if (isDecoy) "SecureVault" else "SecureVault",
              color = TextPrimary,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )

            // Status Badge: Real vs Decoy
            if (isDecoy) {
              StatusBadge(
                text = "DECOY MODE",
                color = NeonAmber,
                icon = "🎭"
              )
            } else {
              StatusBadge(
                text = "ENCRYPTED",
                color = NeonEmeraldLight,
                icon = "🔒"
              )
            }
          }
        },
        actions = {
          // Permissions setup button
          IconButton(
            onClick = { viewModel.setPermissionsSheetOpen(true) },
            modifier = Modifier.testTag("top_bar_permissions_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = "Security Permissions",
              tint = if (securityConfig.isUsageAccessGranted) NeonEmeraldLight else NeonAmber
            )
          }

          // Instant Lock Button
          IconButton(
            onClick = { viewModel.lockVault() },
            modifier = Modifier.testTag("top_bar_lock_vault_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Lock Vault Now",
              tint = NeonRose
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = VaultObsidian
        )
      )
    },
    floatingActionButton = {
      if (selectedTab != VaultTab.SETTINGS) {
        FloatingActionButton(
          onClick = {
            when (selectedTab) {
              VaultTab.MEDIA -> viewModel.setAddMediaDialogOpen(true)
              VaultTab.MUSIC -> viewModel.setAddMediaDialogOpen(true)
              VaultTab.APPS -> viewModel.setSelectAppsDialogOpen(true)
              VaultTab.FILES -> viewModel.openNoteEditor(null)
              VaultTab.SETTINGS -> {}
            }
          },
          containerColor = when (selectedTab) {
            VaultTab.MEDIA -> NeonViolet
            VaultTab.MUSIC -> NeonEmerald
            VaultTab.APPS -> NeonViolet
            VaultTab.FILES -> NeonCyan
            VaultTab.SETTINGS -> NeonViolet
          },
          shape = CircleShape,
          modifier = Modifier.testTag("vault_fab")
        ) {
          Icon(
            imageVector = when (selectedTab) {
              VaultTab.APPS -> Icons.Default.AddModerator
              else -> Icons.Default.Add
            },
            contentDescription = "Add Item",
            tint = Color.White
          )
        }
      }
    },
    bottomBar = {
      NavigationBar(
        containerColor = VaultSurfaceDark,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.border(1.dp, BorderHighlight, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
      ) {
        VaultTab.values().forEach { tab ->
          val isSelected = selectedTab == tab
          NavigationBarItem(
            selected = isSelected,
            onClick = { viewModel.setTab(tab) },
            icon = {
              Text(
                text = tab.iconEmoji,
                fontSize = if (isSelected) 22.sp else 18.sp
              )
            },
            label = {
              Text(
                text = when (tab) {
                  VaultTab.MEDIA -> "Media"
                  VaultTab.MUSIC -> "Audio"
                  VaultTab.APPS -> "Apps"
                  VaultTab.FILES -> "Files"
                  VaultTab.SETTINGS -> "Security"
                },
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NeonVioletLight,
              selectedTextColor = NeonVioletLight,
              indicatorColor = NeonViolet.copy(alpha = 0.25f),
              unselectedIconColor = TextSecondary,
              unselectedTextColor = TextSecondary
            ),
            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      when (selectedTab) {
        VaultTab.MEDIA -> {
          MediaVaultTab(
            viewModel = viewModel,
            mediaList = mediaList,
            selectedFilter = mediaFilter,
            onImportClick = { viewModel.setAddMediaDialogOpen(true) }
          )
        }
        VaultTab.MUSIC -> {
          MusicVaultTab(
            viewModel = viewModel,
            audioList = mediaList,
            currentPlaying = currentAudio,
            isPlaying = isAudioPlaying,
            playbackPosition = audioProgress,
            onImportClick = { viewModel.setAddMediaDialogOpen(true) }
          )
        }
        VaultTab.APPS -> {
          AppsVaultTab(
            viewModel = viewModel,
            protectedApps = protectedApps,
            securityConfig = securityConfig,
            onOpenSelectAppsDialog = { viewModel.setSelectAppsDialogOpen(true) },
            onOpenPermissionsDialog = { viewModel.setPermissionsSheetOpen(true) }
          )
        }
        VaultTab.FILES -> {
          FilesVaultTab(
            viewModel = viewModel,
            filesList = filesList,
            notesList = notesList,
            onAddNoteClick = { viewModel.openNoteEditor(null) },
            onAddFileClick = { viewModel.setAddFileDialogOpen(true) }
          )
        }
        VaultTab.SETTINGS -> {
          SettingsVaultTab(
            viewModel = viewModel,
            securityConfig = securityConfig,
            breakInLogs = breakInLogs,
            onTestBiometrics = onTestBiometrics,
            onOpenPermissionsDialog = { viewModel.setPermissionsSheetOpen(true) }
          )
        }
      }
    }
  }

  // Active Dialogs and In-App Viewers
  if (viewingMedia != null) {
    MediaViewerDialog(
      item = viewingMedia!!,
      onDismiss = { viewModel.closeMediaViewer() },
      onRestoreToStorage = { viewModel.restoreMediaToDeviceStorage(viewingMedia!!) },
      onDelete = { viewModel.deleteMedia(viewingMedia!!) }
    )
  }

  if (viewingFile != null) {
    FileReaderDialog(
      file = viewingFile!!,
      onDismiss = { viewModel.closeFileReader() },
      onDelete = { viewModel.deleteFile(viewingFile!!) }
    )
  }

  if (isAddMediaOpen) {
    AddMediaDialog(
      onConfirm = { title, type, uri, category, moveFromStorage ->
        viewModel.addMedia(title, type, uri, category, moveFromStorage)
      },
      onDismiss = { viewModel.setAddMediaDialogOpen(false) }
    )
  }

  if (isAddNoteOpen) {
    AddNoteDialog(
      existingNote = editingNote,
      onConfirm = { title, content, colorHex, category ->
        viewModel.saveNote(title, content, colorHex, category)
      },
      onDismiss = { viewModel.closeNoteEditor() }
    )
  }

  if (isAddFileOpen) {
    AddFileDialog(
      onConfirm = { name, extension, description, category, content ->
        viewModel.addFile(name, extension, description, category, content)
      },
      onDismiss = { viewModel.setAddFileDialogOpen(false) }
    )
  }

  if (isSelectAppsOpen) {
    val alreadyProtected = protectedApps.map { it.packageName }.toSet()
    SelectAppsDialog(
      deviceApps = deviceApps,
      alreadyProtectedPackageNames = alreadyProtected,
      onSelectApp = { app -> viewModel.hideSelectedDeviceApp(app) },
      onDismiss = { viewModel.setSelectAppsDialogOpen(false) }
    )
  }

  if (isPermissionsOpen) {
    PermissionsSheet(
      securityConfig = securityConfig,
      onRequestMediaPermission = onRequestMediaPermission,
      onRefreshPermissions = { viewModel.refreshPermissions() },
      onDismiss = { viewModel.setPermissionsSheetOpen(false) }
    )
  }
}
