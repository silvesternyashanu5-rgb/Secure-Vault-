package com.example.ui.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.MediaType
import com.example.data.model.VaultMediaItem
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun MediaViewerDialog(
  item: VaultMediaItem,
  onDismiss: () -> Unit,
  onRestoreToStorage: () -> Unit,
  onDelete: () -> Unit
) {
  val context = LocalContext.current
  var isPlaying by remember { mutableStateOf(true) }
  var progress by remember { mutableStateOf(0.35f) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black),
      color = Color.Black
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
      ) {
        // Center Content
        if (item.type == MediaType.PHOTO) {
          // Photo Viewer
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(vertical = 80.dp),
            contentAlignment = Alignment.Center
          ) {
            AsyncImage(
              model = ImageRequest.Builder(context)
                .data(if (item.uriString.startsWith("android.resource")) R.drawable.ic_vault_logo else item.uriString)
                .crossfade(true)
                .build(),
              contentDescription = item.title,
              contentScale = ContentScale.Fit,
              modifier = Modifier.fillMaxWidth().testTag("media_viewer_photo")
            )
          }
        } else if (item.type == MediaType.VIDEO) {
          // Video Player inside SecureVault
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Video Display Screen Box
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, Brush.horizontalGradient(listOf(NeonViolet, NeonCyan)), RoundedCornerShape(20.dp))
                .background(VaultSurfaceDark),
              contentAlignment = Alignment.Center
            ) {
              AsyncImage(
                model = ImageRequest.Builder(context)
                  .data(if (item.uriString.startsWith("android.resource")) R.drawable.ic_vault_logo else item.uriString)
                  .crossfade(true)
                  .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.6f)
              )

              // Center Play/Pause button
              Box(
                modifier = Modifier
                  .size(64.dp)
                  .clip(CircleShape)
                  .background(Color.Black.copy(alpha = 0.7f))
                  .border(1.5.dp, NeonCyan, CircleShape)
                  .clickable { isPlaying = !isPlaying }
                  .testTag("video_player_play_pause_btn"),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                  contentDescription = if (isPlaying) "Pause" else "Play",
                  tint = NeonCyanLight,
                  modifier = Modifier.size(36.dp)
                )
              }

              // Top Live Security Watermark
              Row(
                modifier = Modifier
                  .align(Alignment.TopStart)
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                StatusBadge(text = "PLAYING IN SECURE VAULT", color = NeonEmeraldLight, icon = "🔒")
              }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Video Controls & Progress Slider
            Slider(
              value = progress,
              onValueChange = { progress = it },
              colors = SliderDefaults.colors(
                thumbColor = NeonCyanLight,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = BorderHighlight
              ),
              modifier = Modifier.fillMaxWidth().testTag("video_scrub_slider")
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              val currentSecs = (165 * progress).toInt()
              Text(
                text = "%02d:%02d".format(currentSecs / 60, currentSecs % 60),
                color = TextSecondary,
                fontSize = 12.sp
              )
              Text(
                text = item.duration ?: "02:45",
                color = TextSecondary,
                fontSize = 12.sp
              )
            }
          }
        } else {
          // Audio Player Viewer
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(NeonEmerald.copy(alpha = 0.2f))
                .border(2.dp, NeonEmerald, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = NeonEmeraldLight, modifier = Modifier.size(54.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(item.title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(item.artist ?: "Private Recording", color = NeonEmeraldLight, fontSize = 14.sp)
          }
        }

        // Top Toolbar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onDismiss, modifier = Modifier.testTag("media_viewer_close_btn")) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Close", tint = Color.White)
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = item.title,
              color = Color.White,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Moved to App • Hidden from Phone Storage",
              color = NeonEmeraldLight,
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Restore back to public device storage button
            IconButton(
              onClick = {
                onRestoreToStorage()
                onDismiss()
              },
              modifier = Modifier.testTag("restore_media_to_storage_btn")
            ) {
              Icon(
                imageVector = Icons.Default.DriveFileMove,
                contentDescription = "Move Back to Device Storage",
                tint = NeonCyanLight
              )
            }

            IconButton(onClick = {
              onDelete()
              onDismiss()
            }) {
              Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = NeonRose)
            }
          }
        }

        // Bottom Banner explaining the Move status
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(Color.Black.copy(alpha = 0.8f))
            .padding(14.dp),
          color = Color.Transparent
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = NeonEmeraldLight, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Private in Vault Storage", color = Color.White, fontSize = 12.sp)
            }

            TextButton(
              onClick = {
                onRestoreToStorage()
                onDismiss()
              }
            ) {
              Icon(imageVector = Icons.Default.DriveFileMove, contentDescription = null, tint = NeonCyanLight, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Move to Public Gallery", color = NeonCyanLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
