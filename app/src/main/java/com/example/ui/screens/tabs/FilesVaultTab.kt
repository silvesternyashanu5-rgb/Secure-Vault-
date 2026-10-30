package com.example.ui.screens.tabs

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
import com.example.data.model.VaultFileItem
import com.example.data.model.VaultNoteItem
import com.example.ui.VaultViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun FilesVaultTab(
  viewModel: VaultViewModel,
  filesList: List<VaultFileItem>,
  notesList: List<VaultNoteItem>,
  onAddNoteClick: () -> Unit,
  onAddFileClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedSubTab by remember { mutableStateOf(0) } // 0: Notes, 1: Documents

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    // Sub-tab switcher
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = selectedSubTab == 0,
        onClick = { selectedSubTab = 0 },
        label = { Text("Secret Notes (${notesList.size})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = NeonViolet.copy(alpha = 0.3f),
          selectedLabelColor = NeonVioletLight
        )
      )
      FilterChip(
        selected = selectedSubTab == 1,
        onClick = { selectedSubTab = 1 },
        label = { Text("Confidential Files (${filesList.size})") },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = NeonCyan.copy(alpha = 0.3f),
          selectedLabelColor = NeonCyanLight
        )
      )
    }

    if (selectedSubTab == 0) {
      // Notes List
      if (notesList.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.EditNote, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("No encrypted notes stored", color = TextSecondary, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = onAddNoteClick,
              colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Create Secret Note")
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
          items(notesList, key = { it.id }) { note ->
            NoteCard(
              note = note,
              onClick = { viewModel.openNoteEditor(note) },
              onDelete = { viewModel.deleteNote(note) }
            )
          }
        }
      }
    } else {
      // Files List
      if (filesList.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.FolderZip, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("No confidential files hidden", color = TextSecondary, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = onAddFileClick,
              colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Add Sensitive File")
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
          items(filesList, key = { it.id }) { file ->
            FileCard(
              file = file,
              onClick = { viewModel.openFileReader(file) },
              onDelete = { viewModel.deleteFile(file) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NoteCard(
  note: VaultNoteItem,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  val noteColor = try {
    Color(android.graphics.Color.parseColor(note.colorHex))
  } catch (_: Exception) {
    NeonViolet
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, noteColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .testTag("note_item_${note.id}"),
    color = VaultSurfaceCard
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(noteColor)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = note.title,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        if (note.isPinned) {
          StatusBadge(text = "PINNED", color = noteColor, icon = "📌")
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = note.content,
        color = TextSecondary,
        fontSize = 13.sp,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = note.category,
          color = TextTertiary,
          fontSize = 11.sp
        )
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Delete Note",
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun FileCard(
  file: VaultFileItem,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .border(1.dp, BorderHighlight, RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .testTag("file_item_${file.id}"),
    color = VaultSurfaceCard
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(46.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(NeonCyan.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Description,
          contentDescription = null,
          tint = NeonCyanLight,
          modifier = Modifier.size(26.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = file.name,
          color = TextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = "${file.category} • ${(file.sizeBytes / (1024 * 1024.0)).let { "%.1f MB".format(it) }}",
          color = TextSecondary,
          fontSize = 11.sp
        )
        if (file.description.isNotEmpty()) {
          Text(
            text = file.description,
            color = TextTertiary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Tap to read directly inside vault
      Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.testTag("read_file_${file.id}")
      ) {
        Text("Read", color = NeonCyanLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }

      IconButton(onClick = onDelete) {
        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete File", tint = TextTertiary)
      }
    }
  }
}
