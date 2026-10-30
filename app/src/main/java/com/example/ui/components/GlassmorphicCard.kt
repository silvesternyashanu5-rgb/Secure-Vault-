package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  borderColor: Color = BorderHighlight,
  backgroundColor: Color = VaultSurfaceCard.copy(alpha = 0.95f),
  shape: RoundedCornerShape = RoundedCornerShape(20.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  Surface(
    modifier = modifier
      .clip(shape)
      .border(
        BorderStroke(1.dp, Brush.verticalGradient(listOf(borderColor.copy(alpha = 0.6f), borderColor.copy(alpha = 0.2f)))),
        shape
      ),
    color = backgroundColor,
    shape = shape
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      content = content
    )
  }
}

@Composable
fun StatusBadge(
  text: String,
  color: Color,
  modifier: Modifier = Modifier,
  icon: String? = null
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(color.copy(alpha = 0.18f))
      .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (icon != null) {
      Text(text = icon, fontSize = 12.sp, modifier = Modifier.padding(end = 4.dp))
    }
    Text(
      text = text,
      color = color,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}
