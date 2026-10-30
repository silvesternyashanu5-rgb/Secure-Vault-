package com.example.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaType
import com.example.data.model.VaultNoteItem
import com.example.ui.theme.*

@Composable
fun AddMediaDialog(
  onConfirm: (title: String, type: MediaType, uri: Uri, category: String, moveFromStorage: Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var title by remember { mutableStateOf("") }
  var selectedType by remember { mutableStateOf(MediaType.PHOTO) }
  var category by remember { mutableStateOf("Personal") }
  var selectedUri by remember { mutableStateOf<Uri?>(null) }
  var moveFromStorage by remember { mutableStateOf(true) }

  // Modern Android Zero-Permission Photo Picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedUri = uri
      if (title.isEmpty()) {
        title = "Imported_${System.currentTimeMillis() % 10000}"
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Move Media from Storage to Vault",
        color = TextPrimary,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Select photos, videos, or music to move into the app's encrypted private sandbox.",
          color = TextSecondary,
          fontSize = 12.sp
        )

        // Type Selector Chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          MediaType.values().forEach { type ->
            FilterChip(
              selected = selectedType == type,
              onClick = { selectedType = type },
              label = {
                Text(
                  when (type) {
                    MediaType.PHOTO -> "Photo"
                    MediaType.VIDEO -> "Video"
                    MediaType.AUDIO -> "Music/Audio"
                  }
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = NeonViolet.copy(alpha = 0.3f),
                selectedLabelColor = NeonVioletLight
              )
            )
          }
        }

        // Title Input
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title / File Name") },
          placeholder = { Text("e.g. Confidential_Document_Photo") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("add_media_title_input")
        )

        // Category Input
        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text("Category (e.g. Identity, Personal, Music)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("add_media_category_input")
        )

        // Android Photo Picker Action
        Button(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(
                when (selectedType) {
                  MediaType.PHOTO -> ActivityResultContracts.PickVisualMedia.ImageOnly
                  MediaType.VIDEO -> ActivityResultContracts.PickVisualMedia.VideoOnly
                  MediaType.AUDIO -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
                }
              )
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.fillMaxWidth().testTag("launch_photo_picker_btn")
        ) {
          Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = NeonCyanLight)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (selectedUri != null) "Media Selected ✓" else "Choose from Device Storage",
            color = NeonCyanLight,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Move from storage toggle
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = VaultSurfaceElevated
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Move from local storage",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Hides from phone gallery; accessible only in app",
                color = TextSecondary,
                fontSize = 10.sp
              )
            }
            Switch(
              checked = moveFromStorage,
              onCheckedChange = { moveFromStorage = it },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonEmerald)
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val finalTitle = title.ifEmpty { "Vault_${selectedType.name.lowercase()}_${System.currentTimeMillis() % 1000}" }
          val finalUri = selectedUri ?: Uri.parse("android.resource://${context.packageName}/drawable/ic_vault_logo")
          onConfirm(finalTitle, selectedType, finalUri, category, moveFromStorage)
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
        modifier = Modifier.testTag("confirm_add_media_btn")
      ) {
        Text("Move to Vault")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = VaultSurfaceCard
  )
}

@Composable
fun AddNoteDialog(
  existingNote: VaultNoteItem?,
  onConfirm: (title: String, content: String, colorHex: String, category: String) -> Unit,
  onDismiss: () -> Unit
) {
  var title by remember { mutableStateOf(existingNote?.title ?: "") }
  var content by remember { mutableStateOf(existingNote?.content ?: "") }
  var category by remember { mutableStateOf(existingNote?.category ?: "Personal") }
  var selectedColor by remember { mutableStateOf(existingNote?.colorHex ?: "#7C3AED") }

  val colorPalette = listOf("#7C3AED", "#06B6D4", "#10B981", "#F59E0B", "#F43F5E", "#3B82F6")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (existingNote != null) "Edit Encrypted Note" else "New Secret Note",
        color = TextPrimary,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Note Title") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("note_title_input")
        )

        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Confidential Note Content") },
          minLines = 4,
          maxLines = 8,
          modifier = Modifier.fillMaxWidth().testTag("note_content_input")
        )

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text("Category (e.g. Passwords, Crypto, Private)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("note_category_input")
        )

        // Color selector
        Text("Tag Color", color = TextSecondary, fontSize = 12.sp)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          colorPalette.forEach { hex ->
            val color = Color(android.graphics.Color.parseColor(hex))
            val isSelected = selectedColor == hex
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
                .border(if (isSelected) 2.5.dp else 0.dp, Color.White, CircleShape)
                .clickable { selectedColor = hex }
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotEmpty()) {
            onConfirm(title, content, selectedColor, category)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
        modifier = Modifier.testTag("save_note_btn")
      ) {
        Text("Save Note")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = VaultSurfaceCard
  )
}

@Composable
fun AddFileDialog(
  onConfirm: (name: String, extension: String, description: String, category: String, content: String) -> Unit,
  onDismiss: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Legal") }
  var description by remember { mutableStateOf("") }
  var content by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Confidential Document", color = TextPrimary, fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Document Name (e.g. Deed_2026.pdf)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("file_name_input")
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("file_desc_input")
        )

        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          label = { Text("Document Content / Details") },
          minLines = 3,
          maxLines = 6,
          modifier = Modifier.fillMaxWidth().testTag("file_content_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotEmpty()) {
            val ext = if (name.contains(".")) name.substringAfterLast(".") else "pdf"
            onConfirm(name, ext, description, category, content)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
        modifier = Modifier.testTag("save_file_btn")
      ) {
        Text("Save Document", color = Color.Black)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextSecondary)
      }
    },
    containerColor = VaultSurfaceCard
  )
}
