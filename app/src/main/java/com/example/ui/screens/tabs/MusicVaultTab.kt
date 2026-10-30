package com.example.ui.screens.tabs

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaType
import com.example.data.model.VaultMediaItem
import com.example.ui.VaultViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun MusicVaultTab(
  viewModel: VaultViewModel,
  audioList: List<VaultMediaItem>,
  currentPlaying: VaultMediaItem?,
  isPlaying: Boolean,
  playbackPosition: Float,
  onImportClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val musicItems = audioList.filter { it.type == MediaType.AUDIO }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    // Header Info Banner
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp, bottom = 12.dp),
      shape = RoundedCornerShape(16.dp),
      color = VaultSurfaceCard,
      border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f))
    ) {
      Row(
        modifier = Modifier.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(NeonEmerald.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = null,
            tint = NeonEmeraldLight,
            modifier = Modifier.size(24.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "In-App Audio Player",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Play hidden songs & sensitive voice memos without leaving vault",
            color = TextSecondary,
            fontSize = 12.sp
          )
        }
      }
    }

    // Now Playing Card (if an audio track is selected)
    if (currentPlaying != null) {
      NowPlayingCard(
        item = currentPlaying,
        isPlaying = isPlaying,
        playbackPosition = playbackPosition,
        onTogglePlay = { viewModel.toggleAudioPlayback() },
        onSeek = { viewModel.seekAudio(it) },
        onStop = { viewModel.stopAudio() },
        modifier = Modifier.padding(bottom = 12.dp)
      )
    }

    // Audio Tracks List
    if (musicItems.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Audiotrack,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No private audio or music files hidden",
            color = TextSecondary,
            fontSize = 15.sp
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = onImportClick,
            colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Hide Audio Track")
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(musicItems, key = { it.id }) { item ->
          AudioItemRow(
            item = item,
            isCurrent = currentPlaying?.id == item.id,
            isPlaying = isPlaying && currentPlaying?.id == item.id,
            onPlayClick = { viewModel.playAudio(item) },
            onDeleteClick = { viewModel.deleteMedia(item) }
          )
        }
      }
    }
  }
}

@Composable
private fun NowPlayingCard(
  item: VaultMediaItem,
  isPlaying: Boolean,
  playbackPosition: Float,
  onTogglePlay: () -> Unit,
  onSeek: (Float) -> Unit,
  onStop: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .border(
        1.dp,
        Brush.horizontalGradient(listOf(NeonEmerald, NeonCyan)),
        RoundedCornerShape(20.dp)
      ),
    color = VaultSurfaceCard
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Equalizer simulation indicator
        EqualizerWaves(isPlaying = isPlaying)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.title,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = item.artist ?: "Private Recording",
            color = NeonEmeraldLight,
            fontSize = 12.sp
          )
        }

        IconButton(onClick = onStop) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close player", tint = TextSecondary)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Progress Slider
      Slider(
        value = playbackPosition,
        onValueChange = onSeek,
        colors = SliderDefaults.colors(
          thumbColor = NeonEmeraldLight,
          activeTrackColor = NeonEmerald,
          inactiveTrackColor = BorderHighlight
        ),
        modifier = Modifier.fillMaxWidth().height(24.dp)
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        val totalSecs = 180
        val currentSecs = (totalSecs * playbackPosition).toInt()
        Text(
          text = "%02d:%02d".format(currentSecs / 60, currentSecs % 60),
          color = TextTertiary,
          fontSize = 11.sp
        )
        Text(
          text = item.duration ?: "03:00",
          color = TextTertiary,
          fontSize = 11.sp
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Controls row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { onSeek((playbackPosition - 0.1f).coerceAtLeast(0f)) }
        ) {
          Icon(imageVector = Icons.Default.Replay10, contentDescription = "Rewind", tint = TextPrimary)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(NeonEmerald)
            .clickable { onTogglePlay() },
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pause" else "Play",
            tint = Color.Black,
            modifier = Modifier.size(30.dp)
          )
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(
          onClick = { onSeek((playbackPosition + 0.1f).coerceAtMost(1f)) }
        ) {
          Icon(imageVector = Icons.Default.Forward10, contentDescription = "Forward", tint = TextPrimary)
        }
      }
    }
  }
}

@Composable
private fun AudioItemRow(
  item: VaultMediaItem,
  isCurrent: Boolean,
  isPlaying: Boolean,
  onPlayClick: () -> Unit,
  onDeleteClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(
        1.dp,
        if (isCurrent) NeonEmerald.copy(alpha = 0.8f) else BorderHighlight,
        RoundedCornerShape(14.dp)
      )
      .clickable { onPlayClick() }
      .testTag("audio_row_${item.id}"),
    color = if (isCurrent) VaultSurfaceCard.copy(alpha = 0.95f) else VaultSurfaceDark
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(CircleShape)
          .background(if (isCurrent) NeonEmerald.copy(alpha = 0.25f) else VaultSurfaceCard),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Default.Equalizer else Icons.Default.PlayArrow,
          contentDescription = "Play Audio",
          tint = if (isCurrent) NeonEmeraldLight else TextPrimary,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.title,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${item.artist ?: "Encrypted Audio"} • ${item.duration ?: "03:15"}",
          color = TextSecondary,
          fontSize = 12.sp
        )
      }

      IconButton(onClick = onDeleteClick) {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = "Delete Audio",
          tint = TextTertiary
        )
      }
    }
  }
}

@Composable
private fun EqualizerWaves(isPlaying: Boolean) {
  val infiniteTransition = rememberInfiniteTransition(label = "eq")
  val height1 by infiniteTransition.animateFloat(
    initialValue = 8f,
    targetValue = if (isPlaying) 26f else 8f,
    animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
    label = "h1"
  )
  val height2 by infiniteTransition.animateFloat(
    initialValue = 18f,
    targetValue = if (isPlaying) 10f else 18f,
    animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse),
    label = "h2"
  )
  val height3 by infiniteTransition.animateFloat(
    initialValue = 12f,
    targetValue = if (isPlaying) 24f else 12f,
    animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
    label = "h3"
  )

  Row(
    modifier = Modifier.size(36.dp),
    horizontalArrangement = Arrangement.spacedBy(3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.width(4.dp).height(height1.dp).clip(CircleShape).background(NeonEmerald))
    Box(modifier = Modifier.width(4.dp).height(height2.dp).clip(CircleShape).background(NeonCyan))
    Box(modifier = Modifier.width(4.dp).height(height3.dp).clip(CircleShape).background(NeonVioletLight))
  }
}
