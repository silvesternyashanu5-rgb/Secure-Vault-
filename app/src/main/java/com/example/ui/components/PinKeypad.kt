package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun PinDotsDisplay(
  pinLength: Int,
  maxPinLength: Int = 4,
  isError: Boolean = false,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (i in 0 until maxPinLength) {
      val isFilled = i < pinLength
      val dotColor = when {
        isError -> NeonRose
        isFilled -> NeonCyan
        else -> BorderHighlight
      }
      val dotSize = if (isFilled) 18.dp else 14.dp

      Box(
        modifier = Modifier
          .size(dotSize)
          .clip(CircleShape)
          .background(
            if (isFilled) {
              Brush.radialGradient(listOf(dotColor, dotColor.copy(alpha = 0.6f)))
            } else {
              Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
            }
          )
          .border(
            width = if (isFilled) 2.dp else 1.5.dp,
            color = dotColor,
            shape = CircleShape
          )
      )
    }
  }
}

@Composable
fun PinKeypad(
  onDigitClick: (String) -> Unit,
  onBackspaceClick: () -> Unit,
  onBiometricClick: () -> Unit,
  isBiometricAvailable: Boolean = true,
  modifier: Modifier = Modifier
) {
  val rows = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9")
  )

  Column(
    modifier = modifier.widthIn(max = 360.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    for (row in rows) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        for (digit in row) {
          KeypadButton(
            text = digit,
            testTag = "keypad_btn_$digit",
            onClick = { onDigitClick(digit) }
          )
        }
      }
    }

    // Bottom row: Biometric, 0, Backspace
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Biometric / Extra security key
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(
            if (isBiometricAvailable) NeonViolet.copy(alpha = 0.18f) else Color.Transparent
          )
          .border(
            1.dp,
            if (isBiometricAvailable) NeonViolet.copy(alpha = 0.45f) else Color.Transparent,
            CircleShape
          )
          .clickable(enabled = isBiometricAvailable) { onBiometricClick() }
          .testTag("keypad_biometric_button"),
        contentAlignment = Alignment.Center
      ) {
        if (isBiometricAvailable) {
          Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = "Unlock with Biometrics",
            tint = NeonVioletLight,
            modifier = Modifier.size(32.dp)
          )
        }
      }

      // "0" Key
      KeypadButton(
        text = "0",
        testTag = "keypad_btn_0",
        onClick = { onDigitClick("0") }
      )

      // Backspace Key
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(VaultSurfaceCard.copy(alpha = 0.6f))
          .border(1.dp, BorderHighlight, CircleShape)
          .clickable { onBackspaceClick() }
          .testTag("keypad_backspace_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Backspace,
          contentDescription = "Delete PIN Digit",
          tint = TextSecondary,
          modifier = Modifier.size(26.dp)
        )
      }
    }
  }
}

@Composable
private fun KeypadButton(
  text: String,
  testTag: String,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(72.dp)
      .clip(CircleShape)
      .background(VaultSurfaceCard)
      .border(1.dp, BorderHighlight, CircleShape)
      .clickable { onClick() }
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = TextPrimary,
      fontSize = 26.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}
