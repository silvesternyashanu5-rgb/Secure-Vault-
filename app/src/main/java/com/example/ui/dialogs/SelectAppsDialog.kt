package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.VaultAppItem
import com.example.ui.theme.*

@Composable
fun SelectAppsDialog(
  deviceApps: List<VaultAppItem>,
  alreadyProtectedPackageNames: Set<String>,
  onSelectApp: (VaultAppItem) -> Unit,
  onDismiss: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }

  val filteredApps = deviceApps.filter { app ->
    app.appName.contains(searchQuery, ignoreCase = true) ||
        app.packageName.contains(searchQuery, ignoreCase = true)
  }

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
        // Top Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Move App to Vault",
              color = TextPrimary,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Moves app from Home Screen layer into private vault space",
              color = TextSecondary,
              fontSize = 12.sp
            )
          }

          IconButton(onClick = onDismiss) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search installed apps...", color = TextTertiary) },
          leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_device_apps_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonViolet,
            unfocusedBorderColor = BorderHighlight
          ),
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // List of Installed Apps
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredApps, key = { it.packageName }) { app ->
            val isAlreadyProtected = alreadyProtectedPackageNames.contains(app.packageName)

            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(
                  1.dp,
                  if (isAlreadyProtected) NeonEmerald.copy(alpha = 0.5f) else BorderHighlight,
                  RoundedCornerShape(14.dp)
                )
                .clickable(enabled = !isAlreadyProtected) { onSelectApp(app) }
                .testTag("select_device_app_${app.packageName}"),
              color = if (isAlreadyProtected) VaultSurfaceCard.copy(alpha = 0.6f) else VaultSurfaceCard
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VaultSurfaceElevated),
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = app.iconEmoji, fontSize = 22.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = app.appName,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = app.packageName,
                    color = TextTertiary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                if (isAlreadyProtected) {
                  Text(
                    text = "Hidden",
                    color = NeonEmeraldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                } else {
                  Button(
                    onClick = { onSelectApp(app) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                  ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Move", fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
