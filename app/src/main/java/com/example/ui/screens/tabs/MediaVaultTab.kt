package com.example.ui.screens.tabs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.MediaType
import com.example.data.model.VaultMediaItem
import com.example.ui.VaultViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun MediaVaultTab(
  viewModel: VaultViewModel,
  mediaList: List<VaultMediaItem>,
  selectedFilter: MediaType?,
  onImportClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val filteredList = mediaList.filter { item ->
    when (selectedFilter) {
      null -> item.type == MediaType.PHOTO || item.type == MediaType.VIDEO
      else -> item.type == selectedFilter
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    // Filter chips row
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      FilterChip(
        selected = selectedFilter == null,
        onClick = { viewModel.setMediaFilter(null) },
        label = { Text("All (${mediaList.count { it.type != MediaType.AUDIO }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = NeonViolet.copy(alpha = 0.3f),
          selectedLabelColor = NeonVioletLight
        )
      )
      FilterChip(
        selected = selectedFilter == MediaType.PHOTO,
        onClick = { viewModel.setMediaFilter(MediaType.PHOTO) },
        label = { Text("Photos (${mediaList.count { it.type == MediaType.PHOTO }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = NeonCyan.copy(alpha = 0.3f),
          selectedLabelColor = NeonCyanLight
        )
      )
      FilterChip(
        selected = selectedFilter == MediaType.VIDEO,
        onClick = { viewModel.setMediaFilter(MediaType.VIDEO) },
        label = { Text("Videos (${mediaList.count { it.type == MediaType.VIDEO }})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = NeonEmerald.copy(alpha = 0.3f),
          selectedLabelColor = NeonEmeraldLight
        )
      )
    }

    if (filteredList.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(CircleShape)
              .background(VaultSurfaceCard),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PhotoLibrary,
              contentDescription = null,
              tint = TextTertiary,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No hidden media found",
            color = TextSecondary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Tap the + button to hide photos or videos securely",
            color = TextTertiary,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = onImportClick,
            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("empty_state_import_btn")
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Hide Photos or Videos")
          }
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        contentPadding = PaddingValues(bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredList, key = { it.id }) { item ->
          MediaGridCard(
            item = item,
            onClick = { viewModel.openMediaViewer(item) },
            onDelete = { viewModel.deleteMedia(item) }
          )
        }
      }
    }
  }
}

@Composable
private fun MediaGridCard(
  item: VaultMediaItem,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  val context = LocalContext.current

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(0.9f)
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, BorderHighlight, RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .testTag("media_card_${item.id}"),
    color = VaultSurfaceCard
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Image Thumbnail
      AsyncImage(
        model = ImageRequest.Builder(context)
          .data(if (item.uriString.startsWith("android.resource")) R.drawable.ic_vault_logo else item.uriString)
          .crossfade(true)
          .build(),
        contentDescription = item.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      // Subtle gradient overlay for readability
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
              startY = 150f
            )
          )
      )

      // Moved to app indicator badge
      Surface(
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(8.dp),
        color = Color.Black.copy(alpha = 0.75f),
        shape = RoundedCornerShape(8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = NeonEmeraldLight,
            modifier = Modifier.size(10.dp)
          )
          Spacer(modifier = Modifier.width(3.dp))
          Text(
            text = "Moved to App",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Video play indicator badge if video
      if (item.type == MediaType.VIDEO) {
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.65f))
            .border(1.dp, NeonCyan, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play Video",
            tint = NeonCyanLight,
            modifier = Modifier.size(28.dp)
          )
        }

        // Duration pill
        if (item.duration != null) {
          Surface(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp),
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = item.duration,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Bottom title & category
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .fillMaxWidth()
          .padding(10.dp)
      ) {
        Text(
          text = item.title,
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "${item.category} • ${(item.sizeBytes / (1024 * 1024.0)).let { "%.1f MB".format(it) }}",
          color = TextSecondary,
          fontSize = 10.sp
        )
      }
    }
  }
}
