package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.model.*
import com.example.util.DeviceAppHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class VaultRepository(private val context: Context) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE)

  private val _securityConfig = MutableStateFlow(loadSecurityConfig())
  val securityConfig: StateFlow<SecurityConfig> = _securityConfig.asStateFlow()

  private val _realMediaList = MutableStateFlow<List<VaultMediaItem>>(emptyList())
  val realMediaList: StateFlow<List<VaultMediaItem>> = _realMediaList.asStateFlow()

  private val _decoyMediaList = MutableStateFlow<List<VaultMediaItem>>(emptyList())
  val decoyMediaList: StateFlow<List<VaultMediaItem>> = _decoyMediaList.asStateFlow()

  private val _realFilesList = MutableStateFlow<List<VaultFileItem>>(emptyList())
  val realFilesList: StateFlow<List<VaultFileItem>> = _realFilesList.asStateFlow()

  private val _decoyFilesList = MutableStateFlow<List<VaultFileItem>>(emptyList())
  val decoyFilesList: StateFlow<List<VaultFileItem>> = _decoyFilesList.asStateFlow()

  private val _realNotesList = MutableStateFlow<List<VaultNoteItem>>(emptyList())
  val realNotesList: StateFlow<List<VaultNoteItem>> = _realNotesList.asStateFlow()

  private val _decoyNotesList = MutableStateFlow<List<VaultNoteItem>>(emptyList())
  val decoyNotesList: StateFlow<List<VaultNoteItem>> = _decoyNotesList.asStateFlow()

  private val _realAppsList = MutableStateFlow<List<VaultAppItem>>(emptyList())
  val realAppsList: StateFlow<List<VaultAppItem>> = _realAppsList.asStateFlow()

  private val _decoyAppsList = MutableStateFlow<List<VaultAppItem>>(emptyList())
  val decoyAppsList: StateFlow<List<VaultAppItem>> = _decoyAppsList.asStateFlow()

  private val _breakInLogs = MutableStateFlow<List<BreakInAttempt>>(emptyList())
  val breakInLogs: StateFlow<List<BreakInAttempt>> = _breakInLogs.asStateFlow()

  init {
    initializeInitialData()
  }

  fun refreshPermissions() {
    val usageGranted = DeviceAppHelper.isUsageAccessGranted(context)
    val overlayGranted = DeviceAppHelper.isOverlayPermissionGranted(context)
    val current = _securityConfig.value
    if (current.isUsageAccessGranted != usageGranted || current.isOverlayPermissionGranted != overlayGranted) {
      val updated = current.copy(
        isUsageAccessGranted = usageGranted,
        isOverlayPermissionGranted = overlayGranted
      )
      saveSecurityConfig(updated)
    }
  }

  private fun loadSecurityConfig(): SecurityConfig {
    val lockTypeStr = prefs.getString("lock_type", LockType.PIN.name) ?: LockType.PIN.name
    val masterPin = prefs.getString("master_pin", "1234") ?: "1234"
    val decoyPin = prefs.getString("decoy_pin", "9999") ?: "9999"
    val biometrics = prefs.getBoolean("biometrics_enabled", true)
    val disguiseStr = prefs.getString("disguise_skin", DisguiseSkin.NONE.name) ?: DisguiseSkin.NONE.name
    val iconStr = prefs.getString("disguise_icon", DisguiseIconOption.SECURE_VAULT.name) ?: DisguiseIconOption.SECURE_VAULT.name
    val panic = prefs.getBoolean("panic_enabled", true)
    val mediaPerm = prefs.getBoolean("media_perm", false)

    val lockType = try {
      LockType.valueOf(lockTypeStr)
    } catch (_: Exception) {
      LockType.PIN
    }
    val disguise = try {
      DisguiseSkin.valueOf(disguiseStr)
    } catch (_: Exception) {
      DisguiseSkin.NONE
    }
    val icon = try {
      DisguiseIconOption.valueOf(iconStr)
    } catch (_: Exception) {
      DisguiseIconOption.SECURE_VAULT
    }

    return SecurityConfig(
      lockType = lockType,
      masterPin = masterPin,
      decoyPin = decoyPin,
      biometricsEnabled = biometrics,
      disguiseSkin = disguise,
      selectedIcon = icon,
      panicTriggerEnabled = panic,
      isMediaPermissionGranted = mediaPerm,
      isUsageAccessGranted = DeviceAppHelper.isUsageAccessGranted(context),
      isOverlayPermissionGranted = DeviceAppHelper.isOverlayPermissionGranted(context)
    )
  }

  private fun saveSecurityConfig(config: SecurityConfig) {
    prefs.edit()
      .putString("lock_type", config.lockType.name)
      .putString("master_pin", config.masterPin)
      .putString("decoy_pin", config.decoyPin)
      .putBoolean("biometrics_enabled", config.biometricsEnabled)
      .putString("disguise_skin", config.disguiseSkin.name)
      .putString("disguise_icon", config.selectedIcon.name)
      .putBoolean("panic_enabled", config.panicTriggerEnabled)
      .putBoolean("media_perm", config.isMediaPermissionGranted)
      .apply()
    _securityConfig.value = config
  }

  private fun initializeInitialData() {
    // Initial Real Vault Items (Moved from device storage into encrypted in-app storage)
    _realMediaList.value = listOf(
      VaultMediaItem(
        id = "real_media_1",
        title = "National_ID_Front_Scan.jpg",
        type = MediaType.PHOTO,
        uriString = "android.resource://${context.packageName}/drawable/ic_vault_logo",
        sizeBytes = 2450000,
        category = "Identity",
        isDecoy = false,
        isMovedFromStorage = true
      ),
      VaultMediaItem(
        id = "real_media_2",
        title = "Confidential_Security_Briefing.mp4",
        type = MediaType.VIDEO,
        uriString = "android.resource://${context.packageName}/drawable/ic_vault_logo",
        sizeBytes = 18400000,
        category = "Security",
        isDecoy = false,
        duration = "02:45",
        isMovedFromStorage = true
      ),
      VaultMediaItem(
        id = "real_media_3",
        title = "Boardroom_Meeting_Audio_Recording.mp3",
        type = MediaType.AUDIO,
        uriString = "android.resource://${context.packageName}/drawable/ic_vault_logo",
        sizeBytes = 5600000,
        category = "Audio Memos",
        isDecoy = false,
        duration = "04:12",
        artist = "Voice Memo Vault",
        isMovedFromStorage = true
      ),
      VaultMediaItem(
        id = "real_media_4",
        title = "Property_Deed_Notarized.jpg",
        type = MediaType.PHOTO,
        uriString = "android.resource://${context.packageName}/drawable/ic_vault_logo",
        sizeBytes = 3840000,
        category = "Legal",
        isDecoy = false,
        isMovedFromStorage = true
      )
    )

    // Initial Decoy Vault Items (Innocuous / Camouflage)
    _decoyMediaList.value = listOf(
      VaultMediaItem(
        id = "decoy_media_1",
        title = "Summer_Beach_Sunset.jpg",
        type = MediaType.PHOTO,
        uriString = "android.resource://${context.packageName}/drawable/ic_vault_logo",
        sizeBytes = 1250000,
        category = "Vacation",
        isDecoy = true,
        isMovedFromStorage = true
      ),
      VaultMediaItem(
        id = "decoy_media_2",
        title = "Acoustic_Guitar_Relaxation.mp3",
        type = MediaType.AUDIO,
        uriString = "android.resource://${context.packageName}/drawable/ic_vault_logo",
        sizeBytes = 4200000,
        category = "Music",
        isDecoy = true,
        duration = "03:15",
        artist = "Chill Acoustic",
        isMovedFromStorage = true
      )
    )

    // Real Notes
    _realNotesList.value = listOf(
      VaultNoteItem(
        id = "real_note_1",
        title = "Crypto Hardware Seed Phrases",
        content = "1. marble 2. orbit 3. flame 4. canyon 5. silent 6. timber\n7. glacier 8. quantum 9. beacon 10. velvet 11. solar 12. anchor\n\n⚠️ NEVER photograph or upload to cloud storage.",
        colorHex = "#7C3AED",
        isPinned = true,
        category = "Finance"
      ),
      VaultNoteItem(
        id = "real_note_2",
        title = "Offshore Banking & IBAN Keys",
        content = "Bank: Swiss Privacy Trust\nAccount: CH93-0000-8841-2938-1920\nTax PIN: 984210\nSwift: SPTCH22X\nEmergency Contact: +41 22 900 1144",
        colorHex = "#06B6D4",
        isPinned = true,
        category = "Banking"
      ),
      VaultNoteItem(
        id = "real_note_3",
        title = "Master Passwords Vault Recovery",
        content = "Primary email 2FA Backup: 8712-4491-0021-3982\nAuthenticator backup seeds verified.\nKeep SecureVault updated.",
        colorHex = "#10B981",
        category = "Security"
      )
    )

    // Decoy Notes
    _decoyNotesList.value = listOf(
      VaultNoteItem(
        id = "decoy_note_1",
        title = "Weekend Grocery Shopping List",
        content = "- Organic almond milk\n- Fresh avocados & sourdough\n- Colombian coffee roast\n- Dish soap & paper towels\n- Greek yogurt",
        colorHex = "#F59E0B",
        category = "Shopping"
      ),
      VaultNoteItem(
        id = "decoy_note_2",
        title = "Grandma's Cinnamon Rolls Recipe",
        content = "Ingredients: 3 cups flour, 1 cup warm milk, 2 tbsp butter, cinnamon sugar.\nBake at 375°F for 22 minutes until golden brown.\nTop with vanilla cream icing.",
        colorHex = "#10B981",
        category = "Cooking"
      )
    )

    // Real Sensitive Files
    _realFilesList.value = listOf(
      VaultFileItem(
        id = "real_file_1",
        name = "Tax_Return_Confidential_2025.pdf",
        extension = "pdf",
        mimeType = "application/pdf",
        sizeBytes = 2840000,
        description = "IRS Certified Complete Filing with Schedule C and investment disclosures.",
        category = "Tax",
        contentPreview = "INTERNAL REVENUE SERVICE - FORM 1040\nTax Year 2025\nFiling Status: Single\nAdjusted Gross Income: $184,200\nTotal Tax Paid: $38,400\nRefund Due: $3,210\nStatus: Verified & Encrypted in App Storage"
      ),
      VaultFileItem(
        id = "real_file_2",
        name = "Medical_Health_Records_Encrypted.pdf",
        extension = "pdf",
        mimeType = "application/pdf",
        sizeBytes = 1920000,
        description = "Bloodwork, immunization history, and medical records.",
        category = "Health",
        contentPreview = "COMPREHENSIVE MEDICAL REPORT\nPatient ID: 9482-XA\nBlood Type: O Positive\nAllergies: Penicillin\nCholesterol: Normal (172 mg/dL)\nImmunizations: Up to date (2026)\nPhysician: Dr. H. Vance, MD"
      )
    )

    // Decoy Files
    _decoyFilesList.value = listOf(
      VaultFileItem(
        id = "decoy_file_1",
        name = "Car_Owner_Manual_QuickGuide.pdf",
        extension = "pdf",
        mimeType = "application/pdf",
        sizeBytes = 940000,
        description = "Basic maintenance, tire pressure specs, and fuse diagram.",
        category = "Vehicle",
        contentPreview = "VEHICLE QUICK REFERENCE GUIDE\nEngine Oil: 0W-20 Synthetic\nTire Pressure: 32 PSI Front / 32 PSI Rear\nFuel Grade: 87 Octane Regular\nService Interval: Every 7,500 miles"
      )
    )

    // Initial Real Hidden Apps (Moved from Home Screen layer to Secure Space)
    _realAppsList.value = listOf(
      VaultAppItem("com.whatsapp", "WhatsApp Messenger", "Social & Chat", isLocked = true, fakeCrashEnabled = true, iconEmoji = "💬", isMovedToVault = true, homeScreenHidden = true),
      VaultAppItem("com.instagram.android", "Instagram", "Social", isLocked = true, isCamouflaged = true, iconEmoji = "📸", isMovedToVault = true, homeScreenHidden = true),
      VaultAppItem("com.chase.sig.android", "Chase Mobile", "Banking", isLocked = true, isCamouflaged = false, iconEmoji = "🏦", isMovedToVault = true, homeScreenHidden = true),
      VaultAppItem("org.telegram.messenger", "Telegram", "Secret Chat", isLocked = true, fakeCrashEnabled = false, iconEmoji = "✈️", isMovedToVault = true, homeScreenHidden = true),
      VaultAppItem("com.google.android.apps.photos", "Google Photos", "Gallery", isLocked = true, isCamouflaged = true, iconEmoji = "🖼️", isMovedToVault = true, homeScreenHidden = true),
      VaultAppItem("com.coinbase.android", "Coinbase", "Crypto", isLocked = true, fakeCrashEnabled = true, iconEmoji = "🪙", isMovedToVault = true, homeScreenHidden = true)
    )

    // Initial Decoy Hidden Apps
    _decoyAppsList.value = listOf(
      VaultAppItem("com.solitaire.free", "Classic Solitaire", "Games", isLocked = false, iconEmoji = "🃏", isMovedToVault = true, homeScreenHidden = true),
      VaultAppItem("com.google.android.keep", "Google Keep", "Productivity", isLocked = false, iconEmoji = "📝", isMovedToVault = true, homeScreenHidden = true)
    )

    _breakInLogs.value = listOf(
      BreakInAttempt(
        id = "log_init",
        timestamp = System.currentTimeMillis() - 3600000 * 18,
        enteredPin = "0000",
        details = "Failed passcode attempt recorded"
      )
    )
  }

  fun setLockType(lockType: LockType) {
    val updated = _securityConfig.value.copy(lockType = lockType)
    saveSecurityConfig(updated)
  }

  fun updateMasterPin(newPin: String) {
    val updated = _securityConfig.value.copy(masterPin = newPin)
    saveSecurityConfig(updated)
  }

  fun updateDecoyPin(newPin: String) {
    val updated = _securityConfig.value.copy(decoyPin = newPin)
    saveSecurityConfig(updated)
  }

  fun setBiometricsEnabled(enabled: Boolean) {
    val updated = _securityConfig.value.copy(biometricsEnabled = enabled)
    saveSecurityConfig(updated)
  }

  fun setDisguiseSkin(skin: DisguiseSkin) {
    val updated = _securityConfig.value.copy(disguiseSkin = skin)
    saveSecurityConfig(updated)
  }

  fun setDisguiseIcon(icon: DisguiseIconOption) {
    val updated = _securityConfig.value.copy(selectedIcon = icon)
    saveSecurityConfig(updated)
  }

  fun setMediaPermissionGranted(granted: Boolean) {
    val updated = _securityConfig.value.copy(isMediaPermissionGranted = granted)
    saveSecurityConfig(updated)
  }

  fun recordBreakInAttempt(enteredPin: String) {
    val attempt = BreakInAttempt(
      id = UUID.randomUUID().toString(),
      timestamp = System.currentTimeMillis(),
      enteredPin = enteredPin,
      details = "Unauthorized attempt on vault lock screen"
    )
    _breakInLogs.value = listOf(attempt) + _breakInLogs.value.take(30)
  }

  fun clearBreakInLogs() {
    _breakInLogs.value = emptyList()
  }

  // Move Media from device storage into app's private sandbox
  fun addMediaItem(
    title: String,
    type: MediaType,
    uri: Uri,
    isDecoy: Boolean,
    category: String = "Private",
    deleteOriginalFromStorage: Boolean = true
  ): VaultMediaItem {
    val vaultDir = File(context.filesDir, "vault_media").apply { mkdirs() }
    val extension = when (type) {
      MediaType.PHOTO -> "jpg"
      MediaType.VIDEO -> "mp4"
      MediaType.AUDIO -> "mp3"
    }
    val safeName = "vault_${System.currentTimeMillis()}_${UUID.randomUUID()}.$extension"
    val targetFile = File(vaultDir, safeName)

    try {
      context.contentResolver.openInputStream(uri)?.use { input: InputStream ->
        FileOutputStream(targetFile).use { output ->
          input.copyTo(output)
        }
      }

      // If user requested moving from local storage, attempt to delete original via content resolver
      if (deleteOriginalFromStorage) {
        try {
          context.contentResolver.delete(uri, null, null)
        } catch (_: Exception) {
          // May require user confirmation on Android 11+ via MediaStore createDeleteRequest
        }
      }
    } catch (_: Exception) {}

    val finalUri = if (targetFile.exists()) Uri.fromFile(targetFile).toString() else uri.toString()
    val size = if (targetFile.exists()) targetFile.length() else 1024 * 1024 * 3

    val item = VaultMediaItem(
      id = UUID.randomUUID().toString(),
      title = title,
      type = type,
      uriString = finalUri,
      sizeBytes = size,
      category = category,
      isDecoy = isDecoy,
      duration = when (type) {
        MediaType.VIDEO -> "01:15"
        MediaType.AUDIO -> "03:20"
        MediaType.PHOTO -> null
      },
      artist = if (type == MediaType.AUDIO) "Private Recording" else null,
      isMovedFromStorage = true,
      originalSourcePath = uri.toString()
    )

    if (isDecoy) {
      _decoyMediaList.value = listOf(item) + _decoyMediaList.value
    } else {
      _realMediaList.value = listOf(item) + _realMediaList.value
    }
    return item
  }

  // Restore media back from Vault into Public Device Storage / Gallery
  fun restoreMediaToStorage(item: VaultMediaItem): Boolean {
    return try {
      val file = if (item.uriString.startsWith("file://")) {
        File(Uri.parse(item.uriString).path ?: "")
      } else null

      if (file != null && file.exists()) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
          put(MediaStore.MediaColumns.DISPLAY_NAME, "Restored_${item.title}")
          put(
            MediaStore.MediaColumns.MIME_TYPE,
            when (item.type) {
              MediaType.PHOTO -> "image/jpeg"
              MediaType.VIDEO -> "video/mp4"
              MediaType.AUDIO -> "audio/mpeg"
            }
          )
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(
              MediaStore.MediaColumns.RELATIVE_PATH,
              when (item.type) {
                MediaType.PHOTO -> Environment.DIRECTORY_PICTURES
                MediaType.VIDEO -> Environment.DIRECTORY_MOVIES
                MediaType.AUDIO -> Environment.DIRECTORY_MUSIC
              }
            )
          }
        }

        val targetCollection = when (item.type) {
          MediaType.PHOTO -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
          MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
          MediaType.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val targetUri = resolver.insert(targetCollection, contentValues)
        if (targetUri != null) {
          resolver.openOutputStream(targetUri)?.use { output ->
            FileInputStream(file).use { input ->
              input.copyTo(output)
            }
          }
          // Remove from vault once restored back to device storage
          deleteMediaItem(item)
          true
        } else false
      } else {
        false
      }
    } catch (_: Exception) {
      false
    }
  }

  fun deleteMediaItem(item: VaultMediaItem) {
    if (item.isDecoy) {
      _decoyMediaList.value = _decoyMediaList.value.filter { it.id != item.id }
    } else {
      _realMediaList.value = _realMediaList.value.filter { it.id != item.id }
    }
    if (item.uriString.startsWith("file://")) {
      try {
        val file = File(Uri.parse(item.uriString).path ?: "")
        if (file.exists()) file.delete()
      } catch (_: Exception) {}
    }
  }

  fun addNoteItem(
    title: String,
    content: String,
    colorHex: String,
    category: String,
    isDecoy: Boolean
  ): VaultNoteItem {
    val note = VaultNoteItem(
      id = UUID.randomUUID().toString(),
      title = title,
      content = content,
      colorHex = colorHex,
      category = category,
      isDecoy = isDecoy
    )
    if (isDecoy) {
      _decoyNotesList.value = listOf(note) + _decoyNotesList.value
    } else {
      _realNotesList.value = listOf(note) + _realNotesList.value
    }
    return note
  }

  fun deleteNoteItem(note: VaultNoteItem) {
    if (note.isDecoy) {
      _decoyNotesList.value = _decoyNotesList.value.filter { it.id != note.id }
    } else {
      _realNotesList.value = _realNotesList.value.filter { it.id != note.id }
    }
  }

  fun addFileItem(
    name: String,
    extension: String,
    description: String,
    category: String,
    contentPreview: String,
    isDecoy: Boolean
  ): VaultFileItem {
    val file = VaultFileItem(
      id = UUID.randomUUID().toString(),
      name = name,
      extension = extension,
      mimeType = "application/octet-stream",
      sizeBytes = (1024..5000000).random().toLong(),
      description = description,
      category = category,
      contentPreview = contentPreview,
      isDecoy = isDecoy,
      isMovedFromStorage = true
    )
    if (isDecoy) {
      _decoyFilesList.value = listOf(file) + _decoyFilesList.value
    } else {
      _realFilesList.value = listOf(file) + _realFilesList.value
    }
    return file
  }

  fun deleteFileItem(file: VaultFileItem) {
    if (file.isDecoy) {
      _decoyFilesList.value = _decoyFilesList.value.filter { it.id != file.id }
    } else {
      _realFilesList.value = _realFilesList.value.filter { it.id != file.id }
    }
  }

  fun toggleAppLock(packageName: String, isDecoy: Boolean) {
    val updater = { list: List<VaultAppItem> ->
      list.map { app ->
        if (app.packageName == packageName) app.copy(isLocked = !app.isLocked) else app
      }
    }
    if (isDecoy) {
      _decoyAppsList.value = updater(_decoyAppsList.value)
    } else {
      _realAppsList.value = updater(_realAppsList.value)
    }
  }

  fun toggleAppFakeCrash(packageName: String, isDecoy: Boolean) {
    val updater = { list: List<VaultAppItem> ->
      list.map { app ->
        if (app.packageName == packageName) app.copy(fakeCrashEnabled = !app.fakeCrashEnabled) else app
      }
    }
    if (isDecoy) {
      _decoyAppsList.value = updater(_decoyAppsList.value)
    } else {
      _realAppsList.value = updater(_realAppsList.value)
    }
  }

  fun toggleAppCamouflage(packageName: String, isDecoy: Boolean) {
    val updater = { list: List<VaultAppItem> ->
      list.map { app ->
        if (app.packageName == packageName) app.copy(isCamouflaged = !app.isCamouflaged) else app
      }
    }
    if (isDecoy) {
      _decoyAppsList.value = updater(_decoyAppsList.value)
    } else {
      _realAppsList.value = updater(_realAppsList.value)
    }
  }

  // Move App from Home Screen into Vault container
  fun moveAppToVault(app: VaultAppItem, isDecoy: Boolean) {
    val targetApp = app.copy(
      isLocked = true,
      isMovedToVault = true,
      homeScreenHidden = true
    )
    if (isDecoy) {
      if (_decoyAppsList.value.none { it.packageName == app.packageName }) {
        _decoyAppsList.value = listOf(targetApp) + _decoyAppsList.value
      }
    } else {
      if (_realAppsList.value.none { it.packageName == app.packageName }) {
        _realAppsList.value = listOf(targetApp) + _realAppsList.value
      }
    }
  }

  // Restore App back to Home Screen
  fun restoreAppToHomeScreen(packageName: String, isDecoy: Boolean) {
    if (isDecoy) {
      _decoyAppsList.value = _decoyAppsList.value.filter { it.packageName != packageName }
    } else {
      _realAppsList.value = _realAppsList.value.filter { it.packageName != packageName }
    }
  }

  fun addAppToProtect(app: VaultAppItem, isDecoy: Boolean) {
    moveAppToVault(app, isDecoy)
  }

  fun removeAppFromProtect(packageName: String, isDecoy: Boolean) {
    restoreAppToHomeScreen(packageName, isDecoy)
  }
}
