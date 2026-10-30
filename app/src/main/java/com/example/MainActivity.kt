package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import com.example.data.model.DisguiseSkin
import com.example.ui.VaultViewModel
import com.example.ui.screens.DisguiseCalculatorScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.VaultHomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.BiometricAuthHelper

class MainActivity : FragmentActivity() {

  private val viewModel: VaultViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        val isUnlocked by viewModel.isUnlocked.collectAsState()
        val securityConfig by viewModel.securityConfig.collectAsState()
        val pinInput by viewModel.pinInput.collectAsState()
        val pinError by viewModel.pinError.collectAsState()
        val calcExpression by viewModel.calcExpression.collectAsState()
        val calcResult by viewModel.calcResult.collectAsState()
        val statusMessage by viewModel.statusMessage.collectAsState()

        // Local state for temporary switching between calculator and direct lock screen
        var temporaryShowDirectLock by remember { mutableStateOf(false) }

        // Observe and show toast messages
        LaunchedEffect(statusMessage) {
          statusMessage?.let { msg ->
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
          }
        }

        // Media permissions launcher
        val mediaPermissionsLauncher = rememberLauncherForActivityResult(
          contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsMap ->
          val allGranted = permissionsMap.values.all { it }
          viewModel.setMediaPermissionGranted(allGranted)
          if (allGranted) {
            Toast.makeText(this@MainActivity, "Media storage permission granted", Toast.LENGTH_SHORT).show()
          }
        }

        val triggerMediaPermissions = {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaPermissionsLauncher.launch(
              arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
              )
            )
          } else {
            @Suppress("DEPRECATION")
            mediaPermissionsLauncher.launch(
              arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            )
          }
        }

        val triggerBiometrics = {
          if (BiometricAuthHelper.isBiometricAvailable(this@MainActivity)) {
            BiometricAuthHelper.authenticate(
              activity = this@MainActivity,
              title = "SecureVault Authentication",
              subtitle = "Verify fingerprint or face to unlock real vault",
              negativeButtonText = "Use PIN",
              onSuccess = {
                viewModel.onBiometricSuccess()
              },
              onError = { err ->
                Toast.makeText(this@MainActivity, err, Toast.LENGTH_SHORT).show()
              }
            )
          } else {
            Toast.makeText(
              this@MainActivity,
              "Biometric hardware not enrolled or unavailable. Please use PIN.",
              Toast.LENGTH_LONG
            ).show()
          }
        }

        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          AnimatedContent(
            targetState = isUnlocked,
            transitionSpec = {
              fadeIn() togetherWith fadeOut()
            },
            label = "vault_auth_transition"
          ) { unlocked ->
            if (unlocked) {
              VaultHomeScreen(
                viewModel = viewModel,
                onTestBiometrics = triggerBiometrics,
                onRequestMediaPermission = triggerMediaPermissions
              )
            } else {
              val isCalculatorDisguise = securityConfig.disguiseSkin == DisguiseSkin.CALCULATOR

              if (isCalculatorDisguise && !temporaryShowDirectLock) {
                DisguiseCalculatorScreen(
                  viewModel = viewModel,
                  expression = calcExpression,
                  result = calcResult,
                  onOpenDirectLock = { temporaryShowDirectLock = true }
                )
              } else {
                LockScreen(
                  viewModel = viewModel,
                  pinInput = pinInput,
                  pinError = pinError,
                  onTriggerBiometrics = triggerBiometrics,
                  onSwitchToCalculator = {
                    if (isCalculatorDisguise) {
                      temporaryShowDirectLock = false
                    } else {
                      viewModel.setDisguiseSkin(DisguiseSkin.CALCULATOR)
                    }
                  }
                )
              }
            }
          }
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshPermissions()
    viewModel.loadInstalledApps()
  }
}
