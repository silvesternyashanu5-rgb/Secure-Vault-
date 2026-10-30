package com.example.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.example.data.model.VaultAppItem

object DeviceAppHelper {

  fun isUsageAccessGranted(context: Context): Boolean {
    return try {
      val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
      val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
          AppOpsManager.OPSTR_GET_USAGE_STATS,
          Process.myUid(),
          context.packageName
        )
      } else {
        @Suppress("DEPRECATION")
        appOps.checkOpNoThrow(
          AppOpsManager.OPSTR_GET_USAGE_STATS,
          Process.myUid(),
          context.packageName
        )
      }
      mode == AppOpsManager.MODE_ALLOWED
    } catch (_: Exception) {
      false
    }
  }

  fun isOverlayPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Settings.canDrawOverlays(context)
    } else {
      true
    }
  }

  fun openUsageAccessSettings(context: Context) {
    try {
      val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {
      try {
        val fallback = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallback)
      } catch (_: Exception) {}
    }
  }

  fun openOverlaySettings(context: Context) {
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val intent = Intent(
          Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:${context.packageName}")
        ).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
      }
    } catch (_: Exception) {
      try {
        val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallback)
      } catch (_: Exception) {}
    }
  }

  fun getInstalledLaunchableApps(context: Context): List<VaultAppItem> {
    val pm = context.packageManager
    val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
      addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfos = try {
      pm.queryIntentActivities(mainIntent, 0)
    } catch (_: Exception) {
      emptyList()
    }

    val installedApps = resolveInfos.mapNotNull { resolveInfo ->
      val pkg = resolveInfo.activityInfo.packageName
      if (pkg == context.packageName) return@mapNotNull null
      val appName = resolveInfo.loadLabel(pm).toString()
      val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
      val emoji = when {
        appName.contains("photo", ignoreCase = true) || appName.contains("gallery", ignoreCase = true) -> "🖼️"
        appName.contains("camera", ignoreCase = true) -> "📷"
        appName.contains("chat", ignoreCase = true) || appName.contains("message", ignoreCase = true) || appName.contains("whatsapp", ignoreCase = true) -> "💬"
        appName.contains("mail", ignoreCase = true) -> "✉️"
        appName.contains("music", ignoreCase = true) || appName.contains("audio", ignoreCase = true) -> "🎵"
        appName.contains("video", ignoreCase = true) || appName.contains("tube", ignoreCase = true) -> "🎬"
        appName.contains("bank", ignoreCase = true) || appName.contains("pay", ignoreCase = true) -> "🏦"
        appName.contains("game", ignoreCase = true) -> "🎮"
        appName.contains("browser", ignoreCase = true) || appName.contains("chrome", ignoreCase = true) -> "🌐"
        appName.contains("note", ignoreCase = true) -> "📝"
        appName.contains("clock", ignoreCase = true) -> "⏰"
        else -> "📱"
      }
      VaultAppItem(
        packageName = pkg,
        appName = appName,
        category = if (isSystem) "System App" else "User App",
        isLocked = false,
        isCamouflaged = false,
        fakeCrashEnabled = false,
        isSystemApp = isSystem,
        iconEmoji = emoji
      )
    }.distinctBy { it.packageName }

    return if (installedApps.isNotEmpty()) {
      installedApps.sortedBy { it.appName.lowercase() }
    } else {
      // High-realism default app list fallback for emulator/testing
      listOf(
        VaultAppItem("com.whatsapp", "WhatsApp Messenger", "Social & Chat", isLocked = false, iconEmoji = "💬"),
        VaultAppItem("com.instagram.android", "Instagram", "Social", isLocked = false, iconEmoji = "📸"),
        VaultAppItem("org.telegram.messenger", "Telegram", "Secure Chat", isLocked = false, iconEmoji = "✈️"),
        VaultAppItem("com.google.android.apps.photos", "Google Photos", "Gallery", isLocked = false, iconEmoji = "🖼️"),
        VaultAppItem("com.google.android.youtube", "YouTube", "Video & Music", isLocked = false, iconEmoji = "▶️"),
        VaultAppItem("com.spotify.music", "Spotify Music", "Audio & Music", isLocked = false, iconEmoji = "🎵"),
        VaultAppItem("com.chase.sig.android", "Chase Banking", "Finance", isLocked = false, iconEmoji = "🏦"),
        VaultAppItem("com.paypal.android.p2pmobile", "PayPal", "Finance", isLocked = false, iconEmoji = "💳"),
        VaultAppItem("com.twitter.android", "X (Twitter)", "Social", isLocked = false, iconEmoji = "🌐"),
        VaultAppItem("com.snapchat.android", "Snapchat", "Social", isLocked = false, iconEmoji = "👻")
      )
    }
  }

  fun launchApp(context: Context, packageName: String): Boolean {
    return try {
      val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
      if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        true
      } else {
        false
      }
    } catch (_: Exception) {
      false
    }
  }

  fun openAppDetails(context: Context, packageName: String) {
    try {
      val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:$packageName")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {}
  }
}
