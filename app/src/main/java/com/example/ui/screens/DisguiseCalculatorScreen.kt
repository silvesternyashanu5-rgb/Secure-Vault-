package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.VaultViewModel
import com.example.ui.theme.*

@Composable
fun DisguiseCalculatorScreen(
  viewModel: VaultViewModel,
  expression: String,
  result: String,
  onOpenDirectLock: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(VaultObsidian)
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(horizontal = 20.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Discreet top bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Calculator",
        color = TextSecondary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
      )
      // Discreet icon that allows switching to direct PIN screen if desired
      IconButton(
        onClick = onOpenDirectLock,
        modifier = Modifier.testTag("calc_switch_to_pin_lock")
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Switch to Standard Lock",
          tint = TextTertiary.copy(alpha = 0.5f)
        )
      }
    }

    // Calculator Display Screen
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp),
      horizontalAlignment = Alignment.End
    ) {
      Text(
        text = if (expression.isEmpty()) "0" else expression,
        color = TextSecondary,
        fontSize = 28.sp,
        textAlign = TextAlign.End,
        maxLines = 2,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("calc_expression_text")
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = result,
        color = TextPrimary,
        fontSize = 48.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.End,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("calc_result_text")
      )
    }

    // Calculator Keypad
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      val btnModifier = Modifier.weight(1f).aspectRatio(1f)

      // Row 1: AC, Backspace, %, ÷
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CalcButton("AC", btnModifier, VaultSurfaceElevated, NeonRose) { viewModel.onCalcClear() }
        CalcButton("⌫", btnModifier, VaultSurfaceElevated, TextSecondary) { viewModel.onCalcBackspace() }
        CalcButton("%", btnModifier, VaultSurfaceElevated, NeonCyan) { viewModel.onCalcDigit("%") }
        CalcButton("÷", btnModifier, NeonViolet.copy(alpha = 0.35f), NeonCyanLight) { viewModel.onCalcDigit("/") }
      }

      // Row 2: 7, 8, 9, ×
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CalcButton("7", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit("7") }
        CalcButton("8", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit("8") }
        CalcButton("9", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit("9") }
        CalcButton("×", btnModifier, NeonViolet.copy(alpha = 0.35f), NeonCyanLight) { viewModel.onCalcDigit("*") }
      }

      // Row 3: 4, 5, 6, -
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CalcButton("4", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit("4") }
        CalcButton("5", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit("5") }
        CalcButton("6", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit("6") }
        CalcButton("-", btnModifier, NeonViolet.copy(alpha = 0.35f), NeonCyanLight) { viewModel.onCalcDigit("-") }
      }

      // Row 4: 1, 2, 3, +
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CalcButton("1", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit("1") }
        CalcButton("2", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit("2") }
        CalcButton("3", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit("3") }
        CalcButton("+", btnModifier, NeonViolet.copy(alpha = 0.35f), NeonCyanLight) { viewModel.onCalcDigit("+") }
      }

      // Row 5: 00, 0, ., = (The magic unlock trigger button!)
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CalcButton("00", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit("00") }
        CalcButton("0", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit("0") }
        CalcButton(".", btnModifier, VaultSurfaceCard, TextPrimary) { viewModel.onCalcDigit(".") }
        CalcButton(
          label = "=",
          modifier = btnModifier,
          bg = NeonViolet,
          textColor = Color.White
        ) {
          viewModel.onCalcEquals()
        }
      }
    }
  }
}

@Composable
private fun CalcButton(
  label: String,
  modifier: Modifier = Modifier,
  bg: Color,
  textColor: Color,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .clip(CircleShape)
      .background(bg)
      .clickable { onClick() }
      .testTag("calc_btn_$label"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 22.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}
