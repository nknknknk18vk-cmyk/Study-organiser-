package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.StudyNote
import com.example.ui.StudyViewModel
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PureMidnight
import com.example.ui.theme.SoftOrange

@Composable
fun NotesScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val notesList by viewModel.allNotes.collectAsState()
    var isComposeOpen by remember { mutableStateOf(false) }

    // Unique list of all tags present in the library for filtering
    val allTags = remember(notesList) {
        notesList.flatMap { note ->
            note.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }.distinct()
    }

    // Filter notes list by selected tag
    val filteredNotes = remember(notesList, viewModel.selectedTagFilter) {
        val filter = viewModel.selectedTagFilter
        if (filter == null) notesList else {
            notesList.filter { note ->
                note.tags.split(",").map { it.trim().lowercase() }.contains(filter.lowercase())
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Tag filtering chips row
            if (allTags.isNotEmpty() && !isComposeOpen) {
                Text(
                    text = "Filter by Tag",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "All" chip
                    FilterChip(
                        selected = viewModel.selectedTagFilter == null,
                        onClick = { viewModel.selectedTagFilter = null },
                        label = { Text("All Notes") },
                        modifier = Modifier.testTag("tag_filter_all")
                    )

                    allTags.forEach { tag ->
                        FilterChip(
                            selected = viewModel.selectedTagFilter == tag,
                            onClick = { viewModel.selectedTagFilter = tag },
                            label = { Text(tag) },
                            modifier = Modifier.testTag("tag_filter_$tag")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Screen State A: Compose Note Editor
            if (isComposeOpen || viewModel.editingNote != null) {
                NoteEditorView(
                    viewModel = viewModel,
                    onClose = {
                        viewModel.clearNoteInputs()
                        isComposeOpen = false
                    }
                )
            } else {
                // Screen State B: List notes library
                if (filteredNotes.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                    ) {
                        items(filteredNotes) { note ->
                            NoteCardItem(
                                note = note,
                                onEdit = {
                                    viewModel.editNoteSelect(note)
                                    isComposeOpen = true
                                },
                                onDelete = { viewModel.deleteNote(note) }
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.StickyNote2,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                modifier = Modifier.size(60.dp)
                            )
                            Text(
                                text = "Your Notes Library is Empty",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Tap the floating plus icon to write study notes, or use AI auto-tagging.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button to create a note
        if (!isComposeOpen && viewModel.editingNote == null) {
            FloatingActionButton(
                onClick = {
                    viewModel.clearNoteInputs()
                    isComposeOpen = true
                },
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .testTag("create_note_fab"),
                containerColor = MintGreen,
                contentColor = PureMidnight,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Note", modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun NoteCardItem(
    note: StudyNote,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Note", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.content,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            if (note.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                ) {
                    note.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteEditorView(
    viewModel: StudyViewModel,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("note_editor_view"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (viewModel.editingNote == null) "Write New Study Note" else "Edit Study Note",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Note Inputs
            OutlinedTextField(
                value = viewModel.noteTitleInput,
                onValueChange = { viewModel.noteTitleInput = it },
                label = { Text("Note Title") },
                modifier = Modifier.fillMaxWidth().testTag("note_title_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintGreen
                )
            )

            OutlinedTextField(
                value = viewModel.noteContentInput,
                onValueChange = { viewModel.noteContentInput = it },
                label = { Text("Study details, points discussed, links...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("note_content_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintGreen
                ),
                maxLines = 10
            )

            // AI Assisted auto tag block
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ai_tag_assistant_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = SoftOrange, modifier = Modifier.size(18.dp))
                            Text("✨ AI Note Assistant", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Button(
                            onClick = { viewModel.triggerAiAutoTag() },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftOrange, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !viewModel.isAiAutoTagging && viewModel.noteContentInput.isNotBlank(),
                            modifier = Modifier.height(34.dp).testTag("trigger_ai_assist_btn")
                        ) {
                            if (viewModel.isAiAutoTagging) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Text("Auto-Tag & Summary", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Analyzes notes with Gemini to generate smart tags and a 1-sentence recap.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            OutlinedTextField(
                value = viewModel.noteTagsInput,
                onValueChange = { viewModel.noteTagsInput = it },
                label = { Text("Tags (comma separated)") },
                modifier = Modifier.fillMaxWidth().testTag("note_tags_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintGreen
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.saveNote()
                    onClose()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_note_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = PureMidnight),
                shape = RoundedCornerShape(12.dp),
                enabled = viewModel.noteTitleInput.isNotBlank()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Save Study Note", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
