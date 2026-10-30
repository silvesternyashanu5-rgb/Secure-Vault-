package com.example.data.model

enum class VaultType {
  REAL,
  DECOY // Fake PIN/password unlocks this innocent decoy vault
}

enum class MediaType {
  PHOTO,
  VIDEO,
  AUDIO
}

enum class LockType(val label: String, val description: String) {
  PIN("Numeric PIN", "Fast 4-8 digit numeric passcode"),
  PASSWORD("Alphanumeric Password", "Custom password with letters, numbers & symbols")
}

data class VaultMediaItem(
  val id: String,
  val title: String,
  val type: MediaType,
  val uriString: String,
  val dateAdded: Long = System.currentTimeMillis(),
  val sizeBytes: Long = 1024 * 1024 * 2,
  val category: String = "Private",
  val isDecoy: Boolean = false,
  val duration: String? = null, // e.g. "03:45"
  val artist: String? = null,
  val isMovedFromStorage: Boolean = true, // Removed from public storage & moved inside app
  val originalSourcePath: String? = null
)

data class VaultFileItem(
  val id: String,
  val name: String,
  val extension: String,
  val mimeType: String,
  val sizeBytes: Long,
  val dateAdded: Long = System.currentTimeMillis(),
  val isDecoy: Boolean = false,
  val description: String = "",
  val category: String = "Documents",
  val contentPreview: String? = null,
  val isMovedFromStorage: Boolean = true
)

data class VaultNoteItem(
  val id: String,
  val title: String,
  val content: String,
  val colorHex: String = "#8B5CF6",
  val dateUpdated: Long = System.currentTimeMillis(),
  val isDecoy: Boolean = false,
  val isPinned: Boolean = false,
  val category: String = "Personal"
)

data class VaultAppItem(
  val packageName: String,
  val appName: String,
  val category: String = "Installed App",
  val isLocked: Boolean = true,
  val isCamouflaged: Boolean = false,
  val fakeCrashEnabled: Boolean = false,
  val isSystemApp: Boolean = false,
  val iconEmoji: String = "📱",
  val isMovedToVault: Boolean = true, // Moved from Home Screen layer to SecureVault app
  val homeScreenHidden: Boolean = true
)

enum class DisguiseSkin(val displayName: String, val subtitle: String) {
  NONE("Direct Lock Screen", "Standard biometric, PIN & custom password lock"),
  CALCULATOR("Calculator Disguise", "Secret vault opens by typing passcode and '='"),
  NOTES("Notes Disguise", "Secret vault opens via search bar code"),
  CLOCK("Minimal Clock", "World clock camouflage with hidden unlock"),
  WEATHER("Weather Camouflage", "Displays weather forecast decoy screen")
}

enum class DisguiseIconOption(
  val id: String,
  val label: String,
  val subtitle: String,
  val iconEmoji: String
) {
  SECURE_VAULT("vault", "SecureVault", "Minimalist Cyber Shield", "🛡️"),
  CALCULATOR("calc", "Calculator", "Standard Tools Calc", "🧮"),
  NOTES("notes", "Quick Notes", "Simple Sticky Notes", "📝"),
  CLOCK("clock", "Clock & Timer", "Everyday Timepiece", "⏰"),
  WEATHER("weather", "Weather", "Daily Forecast", "⛅")
}

data class BreakInAttempt(
  val id: String,
  val timestamp: Long,
  val enteredPin: String,
  val details: String = "Incorrect passcode entered at lock screen"
)

data class SecurityConfig(
  val lockType: LockType = LockType.PIN,
  val masterPin: String = "1234", // Used for PIN or Password
  val decoyPin: String = "9999",  // Used for Fake PIN or Fake Password
  val biometricsEnabled: Boolean = true,
  val disguiseSkin: DisguiseSkin = DisguiseSkin.NONE,
  val selectedIcon: DisguiseIconOption = DisguiseIconOption.SECURE_VAULT,
  val panicTriggerEnabled: Boolean = true,
  val isMediaPermissionGranted: Boolean = false,
  val isUsageAccessGranted: Boolean = false,
  val isOverlayPermissionGranted: Boolean = false
)
