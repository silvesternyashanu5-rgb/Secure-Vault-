package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.VaultFileItem
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun FileReaderDialog(
  file: VaultFileItem,
  onDismiss: () -> Unit,
  onDelete: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .fillMaxHeight(0.85f)
        .clip(RoundedCornerShape(24.dp))
        .border(1.dp, BorderHighlight, RoundedCornerShape(24.dp)),
      color = VaultSurfaceDark
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp)
      ) {
        // Top Toolbar
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = NeonCyanLight, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = file.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${file.category} • ${(file.sizeBytes / (1024 * 1024.0)).let { "%.2f MB".format(it) }}",
                color = TextSecondary,
                fontSize = 11.sp
              )
            }
          }

          IconButton(onClick = onDismiss, modifier = Modifier.testTag("file_reader_close_btn")) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Document Security Header
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = VaultSurfaceCard,
          border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f))
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = NeonEmeraldLight, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "In-App Secure Reader (Decrypted in RAM only)",
                color = NeonEmeraldLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
            StatusBadge(text = "INTERNAL", color = NeonCyanLight)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Document Content View
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          shape = RoundedCornerShape(16.dp),
          color = VaultObsidian,
          border = androidx.compose.foundation.BorderStroke(1.dp, BorderHighlight)
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
              .verticalScroll(rememberScrollState())
          ) {
            Text(
              text = file.contentPreview ?: "CONFIDENTIAL RECORD\n\nThis sensitive document is encrypted with internal sandbox protection. All content remains private inside SecureVault without exporting to external storage.",
              color = TextPrimary,
              fontSize = 13.sp,
              lineHeight = 20.sp,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.testTag("file_reader_content")
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Actions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          OutlinedButton(
            onClick = {
              onDelete()
              onDismiss()
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRose),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Delete File")
          }

          Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Close Reader", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
