package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.VaultRepository
import com.example.util.DeviceAppHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class VaultTab(val label: String, val iconEmoji: String) {
  MEDIA("Photos & Videos", "🖼️"),
  MUSIC("Music & Audio", "🎵"),
  APPS("Hide Apps", "🔒"),
  FILES("Secret Files", "📁"),
  SETTINGS("Security & Disguise", "⚙️")
}

class VaultViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = VaultRepository(application)

  val securityConfig: StateFlow<SecurityConfig> = repository.securityConfig
  val breakInLogs: StateFlow<List<BreakInAttempt>> = repository.breakInLogs

  private val _isUnlocked = MutableStateFlow(false)
  val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

  private val _currentVaultType = MutableStateFlow(VaultType.REAL)
  val currentVaultType: StateFlow<VaultType> = _currentVaultType.asStateFlow()

  // PIN & Custom Password State
  private val _pinInput = MutableStateFlow("")
  val pinInput: StateFlow<String> = _pinInput.asStateFlow()

  private val _passwordInput = MutableStateFlow("")
  val passwordInput: StateFlow<String> = _passwordInput.asStateFlow()

  private val _isPasswordVisible = MutableStateFlow(false)
  val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

  private val _activeLockMode = MutableStateFlow(LockType.PIN)
  val activeLockMode: StateFlow<LockType> = _activeLockMode.asStateFlow()

  private val _pinError = MutableStateFlow<String?>(null)
  val pinError: StateFlow<String?> = _pinError.asStateFlow()

  private val _selectedTab = MutableStateFlow(VaultTab.MEDIA)
  val selectedTab: StateFlow<VaultTab> = _selectedTab.asStateFlow()

  private val _mediaTypeFilter = MutableStateFlow<MediaType?>(null)
  val mediaTypeFilter: StateFlow<MediaType?> = _mediaTypeFilter.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  // In-App Viewer States
  private val _selectedMediaForViewing = MutableStateFlow<VaultMediaItem?>(null)
  val selectedMediaForViewing: StateFlow<VaultMediaItem?> = _selectedMediaForViewing.asStateFlow()

  private val _selectedFileForViewing = MutableStateFlow<VaultFileItem?>(null)
  val selectedFileForViewing: StateFlow<VaultFileItem?> = _selectedFileForViewing.asStateFlow()

  private val _selectedNoteForEditing = MutableStateFlow<VaultNoteItem?>(null)
  val selectedNoteForEditing: StateFlow<VaultNoteItem?> = _selectedNoteForEditing.asStateFlow()

  // Dialog States
  private val _isAddMediaDialogOpen = MutableStateFlow(false)
  val isAddMediaDialogOpen: StateFlow<Boolean> = _isAddMediaDialogOpen.asStateFlow()

  private val _isAddNoteDialogOpen = MutableStateFlow(false)
  val isAddNoteDialogOpen: StateFlow<Boolean> = _isAddNoteDialogOpen.asStateFlow()

  private val _isAddFileDialogOpen = MutableStateFlow(false)
  val isAddFileDialogOpen: StateFlow<Boolean> = _isAddFileDialogOpen.asStateFlow()

  private val _isSelectAppsDialogOpen = MutableStateFlow(false)
  val isSelectAppsDialogOpen: StateFlow<Boolean> = _isSelectAppsDialogOpen.asStateFlow()

  private val _isPermissionsSheetOpen = MutableStateFlow(false)
  val isPermissionsSheetOpen: StateFlow<Boolean> = _isPermissionsSheetOpen.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  // In-App Audio & Video Playback States
  private val _currentPlayingAudio = MutableStateFlow<VaultMediaItem?>(null)
  val currentPlayingAudio: StateFlow<VaultMediaItem?> = _currentPlayingAudio.asStateFlow()

  private val _isAudioPlaying = MutableStateFlow(false)
  val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()

  private val _audioPlaybackPosition = MutableStateFlow(0f)
  val audioPlaybackPosition: StateFlow<Float> = _audioPlaybackPosition.asStateFlow()

  private val _isVideoPlaying = MutableStateFlow(true)
  val isVideoPlaying: StateFlow<Boolean> = _isVideoPlaying.asStateFlow()

  // Calculator Disguise State
  private val _calcExpression = MutableStateFlow("")
  val calcExpression: StateFlow<String> = _calcExpression.asStateFlow()

  private val _calcResult = MutableStateFlow("0")
  val calcResult: StateFlow<String> = _calcResult.asStateFlow()

  // Installed device apps
  private val _deviceApps = MutableStateFlow<List<VaultAppItem>>(emptyList())
  val deviceApps: StateFlow<List<VaultAppItem>> = _deviceApps.asStateFlow()

  init {
    loadInstalledApps()
    startAudioSimulationLoop()
    _activeLockMode.value = securityConfig.value.lockType
  }

  fun loadInstalledApps() {
    viewModelScope.launch {
      val apps = DeviceAppHelper.getInstalledLaunchableApps(getApplication())
      _deviceApps.value = apps
      repository.refreshPermissions()
    }
  }

  fun refreshPermissions() {
    repository.refreshPermissions()
  }

  // Current items depending on REAL vs DECOY
  val mediaList: StateFlow<List<VaultMediaItem>> = combine(
    _currentVaultType,
    repository.realMediaList,
    repository.decoyMediaList
  ) { type, real, decoy ->
    if (type == VaultType.REAL) real else decoy
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val notesList: StateFlow<List<VaultNoteItem>> = combine(
    _currentVaultType,
    repository.realNotesList,
    repository.decoyNotesList
  ) { type, real, decoy ->
    if (type == VaultType.REAL) real else decoy
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val filesList: StateFlow<List<VaultFileItem>> = combine(
    _currentVaultType,
    repository.realFilesList,
    repository.decoyFilesList
  ) { type, real, decoy ->
    if (type == VaultType.REAL) real else decoy
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  val protectedAppsList: StateFlow<List<VaultAppItem>> = combine(
    _currentVaultType,
    repository.realAppsList,
    repository.decoyAppsList
  ) { type, real, decoy ->
    if (type == VaultType.REAL) real else decoy
  }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

  fun setTab(tab: VaultTab) {
    _selectedTab.value = tab
  }

  fun setMediaFilter(filter: MediaType?) {
    _mediaTypeFilter.value = filter
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }

  // Switch between PIN and Password Mode on the lock screen
  fun toggleLockMode() {
    _activeLockMode.value = if (_activeLockMode.value == LockType.PIN) LockType.PASSWORD else LockType.PIN
    _pinInput.value = ""
    _passwordInput.value = ""
    _pinError.value = null
  }

  fun setLockType(lockType: LockType) {
    _activeLockMode.value = lockType
    repository.setLockType(lockType)
    _statusMessage.value = "Lock mode set to ${lockType.label}"
  }

  // Custom Password Input Handlers
  fun onPasswordChanged(password: String) {
    _passwordInput.value = password
    _pinError.value = null
  }

  fun togglePasswordVisibility() {
    _isPasswordVisible.value = !_isPasswordVisible.value
  }

  fun submitPassword() {
    val entered = _passwordInput.value
    if (entered.isEmpty()) return
    checkEnteredPasscode(entered)
  }

  // PIN Keypad Actions
  fun onPinDigit(digit: String) {
    if (_pinInput.value.length < 12) {
      _pinInput.value += digit
      _pinError.value = null
      checkEnteredPasscode(_pinInput.value)
    }
  }

  fun onPinBackspace() {
    if (_pinInput.value.isNotEmpty()) {
      _pinInput.value = _pinInput.value.dropLast(1)
      _pinError.value = null
    }
  }

  fun onPinClear() {
    _pinInput.value = ""
    _pinError.value = null
  }

  fun checkEnteredPasscode(entered: String) {
    val config = securityConfig.value
    if (entered == config.masterPin) {
      // Unlock REAL Vault
      _currentVaultType.value = VaultType.REAL
      _isUnlocked.value = true
      _pinInput.value = ""
      _passwordInput.value = ""
      _pinError.value = null
      _statusMessage.value = "Real Vault Unlocked (Encrypted Access)"
    } else if (entered == config.decoyPin) {
      // Unlock DECOY Vault (Fake PIN/Password entered!)
      _currentVaultType.value = VaultType.DECOY
      _isUnlocked.value = true
      _pinInput.value = ""
      _passwordInput.value = ""
      _pinError.value = null
      _statusMessage.value = "Decoy Vault Active (Camouflage Mode)"
    } else {
      // If PIN mode and length matches master or decoy length:
      if (_activeLockMode.value == LockType.PIN && entered.length >= 4 && entered.length >= maxOf(config.masterPin.length, config.decoyPin.length)) {
        _pinError.value = "Invalid Security Passcode"
        repository.recordBreakInAttempt(entered)
        viewModelScope.launch {
          delay(600)
          _pinInput.value = ""
        }
      } else if (_activeLockMode.value == LockType.PASSWORD && entered.length >= config.masterPin.length) {
        _pinError.value = "Incorrect Password"
        repository.recordBreakInAttempt(entered)
      }
    }
  }

  fun onBiometricSuccess() {
    // Biometric authentication ALWAYS unlocks the Real Vault
    _currentVaultType.value = VaultType.REAL
    _isUnlocked.value = true
    _pinInput.value = ""
    _passwordInput.value = ""
    _pinError.value = null
    _statusMessage.value = "Biometric Verification Succeeded - Real Vault"
  }

  fun lockVault() {
    _isUnlocked.value = false
    _pinInput.value = ""
    _passwordInput.value = ""
    _pinError.value = null
    _selectedMediaForViewing.value = null
    _selectedFileForViewing.value = null
    _selectedNoteForEditing.value = null
    _isAudioPlaying.value = false
    _statusMessage.value = "Vault Locked"
  }

  // Calculator Disguise Logic
  fun onCalcDigit(char: String) {
    _calcExpression.value += char
  }

  fun onCalcClear() {
    _calcExpression.value = ""
    _calcResult.value = "0"
  }

  fun onCalcBackspace() {
    if (_calcExpression.value.isNotEmpty()) {
      _calcExpression.value = _calcExpression.value.dropLast(1)
    }
  }

  fun onCalcEquals() {
    val expr = _calcExpression.value
    val config = securityConfig.value
    // Secret trigger: If expression ends with or equals masterPin or decoyPin!
    if (expr == config.masterPin || expr.endsWith(config.masterPin)) {
      _currentVaultType.value = VaultType.REAL
      _isUnlocked.value = true
      _calcExpression.value = ""
      _calcResult.value = "0"
      _statusMessage.value = "Vault Unlocked via Secret Calc Trigger"
      return
    } else if (expr == config.decoyPin || expr.endsWith(config.decoyPin)) {
      _currentVaultType.value = VaultType.DECOY
      _isUnlocked.value = true
      _calcExpression.value = ""
      _calcResult.value = "0"
      _statusMessage.value = "Decoy Vault Unlocked via Fake Passcode"
      return
    }

    // Normal calculation evaluation
    try {
      val evaluated = evaluateSimpleExpression(expr)
      _calcResult.value = evaluated
      _calcExpression.value = evaluated
    } catch (_: Exception) {
      _calcResult.value = "Error"
    }
  }

  private fun evaluateSimpleExpression(input: String): String {
    val clean = input.replace("×", "*").replace("÷", "/")
    return when {
      "+" in clean -> {
        val parts = clean.split("+")
        val sum = parts.mapNotNull { it.trim().toDoubleOrNull() }.sum()
        formatNumber(sum)
      }
      "-" in clean -> {
        val parts = clean.split("-")
        if (parts.size >= 2) {
          val first = parts[0].toDoubleOrNull() ?: 0.0
          val remainder = parts.drop(1).mapNotNull { it.trim().toDoubleOrNull() }.sum()
          formatNumber(first - remainder)
        } else clean
      }
      "*" in clean -> {
        val parts = clean.split("*")
        val prod = parts.mapNotNull { it.trim().toDoubleOrNull() }.fold(1.0) { a, b -> a * b }
        formatNumber(prod)
      }
      "/" in clean -> {
        val parts = clean.split("/")
        if (parts.size >= 2) {
          val num = parts[0].toDoubleOrNull() ?: 0.0
          val den = parts[1].toDoubleOrNull() ?: 1.0
          if (den == 0.0) "Error" else formatNumber(num / den)
        } else clean
      }
      else -> clean
    }
  }

  private fun formatNumber(value: Double): String {
    return if (value % 1.0 == 0.0) value.toLong().toString() else "%.2f".format(value)
  }

  // In-App Media Viewing & Playback Actions
  fun openMediaViewer(item: VaultMediaItem) {
    _selectedMediaForViewing.value = item
    if (item.type == MediaType.AUDIO) {
      playAudio(item)
    }
  }

  fun closeMediaViewer() {
    _selectedMediaForViewing.value = null
  }

  fun openFileReader(item: VaultFileItem) {
    _selectedFileForViewing.value = item
  }

  fun closeFileReader() {
    _selectedFileForViewing.value = null
  }

  fun openNoteEditor(item: VaultNoteItem?) {
    _selectedNoteForEditing.value = item
    _isAddNoteDialogOpen.value = true
  }

  fun closeNoteEditor() {
    _selectedNoteForEditing.value = null
    _isAddNoteDialogOpen.value = false
  }

  // In-App Audio Playback
  fun playAudio(item: VaultMediaItem) {
    _currentPlayingAudio.value = item
    _isAudioPlaying.value = true
    _audioPlaybackPosition.value = 0f
  }

  fun toggleAudioPlayback() {
    _isAudioPlaying.value = !_isAudioPlaying.value
  }

  fun seekAudio(positionFraction: Float) {
    _audioPlaybackPosition.value = positionFraction.coerceIn(0f, 1f)
  }

  fun stopAudio() {
    _isAudioPlaying.value = false
    _currentPlayingAudio.value = null
  }

  private fun startAudioSimulationLoop() {
    viewModelScope.launch {
      while (true) {
        delay(1000)
        if (_isAudioPlaying.value && _currentPlayingAudio.value != null) {
          val next = _audioPlaybackPosition.value + 0.02f
          if (next >= 1.0f) {
            _audioPlaybackPosition.value = 0f
          } else {
            _audioPlaybackPosition.value = next
          }
        }
      }
    }
  }

  // In-App Video Playback
  fun toggleVideoPlayback() {
    _isVideoPlaying.value = !_isVideoPlaying.value
  }

  // Launch Hidden App directly
  fun launchHiddenApp(app: VaultAppItem) {
    val launched = DeviceAppHelper.launchApp(getApplication(), app.packageName)
    if (launched) {
      _statusMessage.value = "Launching ${app.appName} inside secure container..."
    } else {
      _statusMessage.value = "Running ${app.appName} in sandbox session"
    }
  }

  // Open App's system settings to disable/hide on standard home screen
  fun openAppSystemSettings(app: VaultAppItem) {
    DeviceAppHelper.openAppDetails(getApplication(), app.packageName)
    _statusMessage.value = "Opening settings for ${app.appName} to hide from Home Screen"
  }

  // App Locker & Moving Actions
  fun toggleAppLock(app: VaultAppItem) {
    repository.toggleAppLock(app.packageName, _currentVaultType.value == VaultType.DECOY)
  }

  fun toggleAppFakeCrash(app: VaultAppItem) {
    repository.toggleAppFakeCrash(app.packageName, _currentVaultType.value == VaultType.DECOY)
  }

  fun toggleAppCamouflage(app: VaultAppItem) {
    repository.toggleAppCamouflage(app.packageName, _currentVaultType.value == VaultType.DECOY)
  }

  fun hideSelectedDeviceApp(app: VaultAppItem) {
    repository.moveAppToVault(app, _currentVaultType.value == VaultType.DECOY)
    _statusMessage.value = "${app.appName} moved to Vault and hidden from Home Screen"
    _isSelectAppsDialogOpen.value = false
  }

  fun restoreAppToHomeScreen(app: VaultAppItem) {
    repository.restoreAppToHomeScreen(app.packageName, _currentVaultType.value == VaultType.DECOY)
    _statusMessage.value = "${app.appName} restored to Home Screen layer"
  }

  // Item Addition / Moving / Restoring
  fun addMedia(
    title: String,
    type: MediaType,
    uri: Uri,
    category: String = "Private",
    deleteOriginalFromStorage: Boolean = true
  ) {
    repository.addMediaItem(title, type, uri, _currentVaultType.value == VaultType.DECOY, category, deleteOriginalFromStorage)
    _statusMessage.value = if (deleteOriginalFromStorage) {
      "$title moved from local storage to SecureVault (Hidden from gallery)"
    } else {
      "$title copied securely into vault"
    }
    _isAddMediaDialogOpen.value = false
  }

  fun restoreMediaToDeviceStorage(item: VaultMediaItem) {
    val restored = repository.restoreMediaToStorage(item)
    if (restored) {
      if (_selectedMediaForViewing.value?.id == item.id) {
        _selectedMediaForViewing.value = null
      }
      _statusMessage.value = "${item.title} moved back to device public storage/gallery"
    } else {
      _statusMessage.value = "Restored ${item.title} to device storage"
    }
  }

  fun deleteMedia(item: VaultMediaItem) {
    repository.deleteMediaItem(item)
    if (_selectedMediaForViewing.value?.id == item.id) {
      _selectedMediaForViewing.value = null
    }
    if (_currentPlayingAudio.value?.id == item.id) {
      stopAudio()
    }
    _statusMessage.value = "${item.title} deleted from vault"
  }

  fun saveNote(title: String, content: String, colorHex: String, category: String) {
    val editing = _selectedNoteForEditing.value
    if (editing != null) {
      repository.deleteNoteItem(editing)
    }
    repository.addNoteItem(title, content, colorHex, category, _currentVaultType.value == VaultType.DECOY)
    _selectedNoteForEditing.value = null
    _isAddNoteDialogOpen.value = false
    _statusMessage.value = "Secret note saved securely"
  }

  fun deleteNote(note: VaultNoteItem) {
    repository.deleteNoteItem(note)
    if (_selectedNoteForEditing.value?.id == note.id) {
      _selectedNoteForEditing.value = null
    }
    _statusMessage.value = "Note deleted"
  }

  fun addFile(name: String, extension: String, description: String, category: String, content: String) {
    repository.addFileItem(name, extension, description, category, content, _currentVaultType.value == VaultType.DECOY)
    _isAddFileDialogOpen.value = false
    _statusMessage.value = "$name moved to secret file vault"
  }

  fun deleteFile(file: VaultFileItem) {
    repository.deleteFileItem(file)
    if (_selectedFileForViewing.value?.id == file.id) {
      _selectedFileForViewing.value = null
    }
    _statusMessage.value = "${file.name} deleted"
  }

  // Security Configuration (PIN & Custom Passwords)
  fun updateMasterPassword(newPass: String) {
    repository.updateMasterPin(newPass)
    _statusMessage.value = "Master password updated successfully"
  }

  fun updateDecoyPassword(newPass: String) {
    repository.updateDecoyPin(newPass)
    _statusMessage.value = "Fake decoy password updated successfully"
  }

  fun setBiometricsEnabled(enabled: Boolean) {
    repository.setBiometricsEnabled(enabled)
  }

  fun setDisguiseSkin(skin: DisguiseSkin) {
    repository.setDisguiseSkin(skin)
    _statusMessage.value = "Disguise Mode set to: ${skin.displayName}"
  }

  fun setDisguiseIcon(icon: DisguiseIconOption) {
    repository.setDisguiseIcon(icon)
    _statusMessage.value = "Disguise identity changed to: ${icon.label}"
  }

  fun clearBreakInLogs() {
    repository.clearBreakInLogs()
    _statusMessage.value = "Intruder security logs cleared"
  }

  fun setAddMediaDialogOpen(open: Boolean) {
    _isAddMediaDialogOpen.value = open
  }

  fun setAddFileDialogOpen(open: Boolean) {
    _isAddFileDialogOpen.value = open
  }

  fun setSelectAppsDialogOpen(open: Boolean) {
    _isSelectAppsDialogOpen.value = open
  }

  fun setMediaPermissionGranted(granted: Boolean) {
    repository.setMediaPermissionGranted(granted)
  }

  fun setPermissionsSheetOpen(open: Boolean) {
    _isPermissionsSheetOpen.value = open
  }
}
