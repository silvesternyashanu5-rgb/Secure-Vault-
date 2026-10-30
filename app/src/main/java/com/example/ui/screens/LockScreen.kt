package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LockType
import com.example.ui.VaultViewModel
import com.example.ui.components.PinDotsDisplay
import com.example.ui.components.PinKeypad
import com.example.ui.theme.*

@Composable
fun LockScreen(
  viewModel: VaultViewModel,
  pinInput: String,
  pinError: String?,
  onTriggerBiometrics: () -> Unit,
  onSwitchToCalculator: () -> Unit,
  modifier: Modifier = Modifier
) {
  val activeLockMode by viewModel.activeLockMode.collectAsState()
  val passwordInput by viewModel.passwordInput.collectAsState()
  val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
  val securityConfig by viewModel.securityConfig.collectAsState()

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            VaultObsidian,
            Color(0xFF0F172A),
            VaultObsidian
          )
        )
      )
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 24.dp, vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 420.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top header with minimalist logo & app name
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                listOf(NeonViolet.copy(alpha = 0.3f), Color.Transparent)
              )
            )
            .border(
              width = 1.5.dp,
              brush = Brush.linearGradient(listOf(NeonCyan, NeonViolet)),
              shape = CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(id = R.drawable.ic_vault_logo),
            contentDescription = "SecureVault Logo",
            modifier = Modifier.size(52.dp).clip(CircleShape)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "SecureVault",
          color = TextPrimary,
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = if (activeLockMode == LockType.PIN)
            "Enter PIN or scan Biometrics"
          else
            "Enter your custom Password or scan Biometrics",
          color = TextSecondary,
          fontSize = 12.sp,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Lock mode switcher pill (PIN vs Custom Password)
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(VaultSurfaceCard)
            .border(1.dp, BorderHighlight, RoundedCornerShape(20.dp))
            .padding(4.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .clickable { if (activeLockMode != LockType.PIN) viewModel.toggleLockMode() },
            color = if (activeLockMode == LockType.PIN) NeonViolet.copy(alpha = 0.35f) else Color.Transparent
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Pin,
                contentDescription = null,
                tint = if (activeLockMode == LockType.PIN) NeonVioletLight else TextSecondary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "PIN Mode",
                color = if (activeLockMode == LockType.PIN) Color.White else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .clickable { if (activeLockMode != LockType.PASSWORD) viewModel.toggleLockMode() },
            color = if (activeLockMode == LockType.PASSWORD) NeonCyan.copy(alpha = 0.35f) else Color.Transparent
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Password,
                contentDescription = null,
                tint = if (activeLockMode == LockType.PASSWORD) NeonCyanLight else TextSecondary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Password Mode",
                color = if (activeLockMode == LockType.PASSWORD) Color.White else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // Middle: Input display or Text Field
      if (activeLockMode == LockType.PIN) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(vertical = 14.dp)
        ) {
          PinDotsDisplay(
            pinLength = pinInput.length,
            maxPinLength = securityConfig.masterPin.length.coerceAtLeast(4),
            isError = pinError != null
          )

          AnimatedVisibility(visible = pinError != null) {
            Text(
              text = pinError ?: "",
              color = NeonRose,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(top = 8.dp)
            )
          }
        }

        // Numeric Keypad
        PinKeypad(
          onDigitClick = { viewModel.onPinDigit(it) },
          onBackspaceClick = { viewModel.onPinBackspace() },
          onBiometricClick = onTriggerBiometrics,
          isBiometricAvailable = true
        )
      } else {
        // Alphanumeric Custom Password Mode
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          OutlinedTextField(
            value = passwordInput,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text("Enter Vault Password") },
            placeholder = { Text("Any letters, numbers or symbols") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
              Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = NeonCyanLight)
            },
            trailingIcon = {
              IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = if (isPasswordVisible) "Hide Password" else "Show Password",
                  tint = TextSecondary
                )
              }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { viewModel.submitPassword() }),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = NeonCyan,
              unfocusedBorderColor = BorderHighlight
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("custom_password_input")
          )

          AnimatedVisibility(visible = pinError != null) {
            Text(
              text = pinError ?: "",
              color = NeonRose,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(top = 8.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Biometric button
            OutlinedButton(
              onClick = onTriggerBiometrics,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.weight(1f).height(50.dp).testTag("password_biometric_btn"),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonVioletLight),
              border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f))
            ) {
              Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = NeonVioletLight)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Biometric")
            }

            // Submit Password button
            Button(
              onClick = { viewModel.submitPassword() },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.weight(1.5f).height(50.dp).testTag("submit_password_btn"),
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
              Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = Color.Black)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Unlock Vault", color = Color.Black, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Bottom Bar with Camouflage shortcut
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextButton(
          onClick = onSwitchToCalculator,
          modifier = Modifier.testTag("switch_to_calculator_disguise_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Calculate,
            contentDescription = null,
            tint = NeonVioletLight,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Switch to Calculator Disguise",
            color = NeonVioletLight,
            fontSize = 13.sp
          )
        }
      }
    }
  }
}
