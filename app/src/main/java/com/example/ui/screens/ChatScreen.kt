package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.ui.StudyViewModel
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PureMidnight
import com.example.ui.theme.SoftOrange
import com.example.ui.theme.TextMuted

@Composable
fun ChatScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val chatHistory by viewModel.allMessages.collectAsState()
    val isTimerActive = viewModel.isTimerRunning
    val listState = rememberLazyListState()

    // Keep chat auto-scrolled to the bottom when new messages arrive
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Main Chat Layout (Blurred if timer is running)
        val blurRadius = if (isTimerActive) 16.dp else 0.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
        ) {
            // Chat Info Bar
            Card(
                modifier = Modifier.fillMaxWidth().testTag("chat_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = MintGreen)
                        Column {
                            Text("Study Pod: Alpha", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Active: You, Chloe, @AI_Tutor", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        }
                    }
                    IconButton(
                        onClick = { viewModel.clearChatHistory() },
                        modifier = Modifier.testTag("clear_chat_btn")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chat Feed List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatHistory) { message ->
                    ChatBubble(message = message)
                }

                if (viewModel.isAiTutorResponding) {
                    item {
                        AiTypingIndicator()
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Message Input bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.chatInput,
                    onValueChange = { viewModel.chatInput = it },
                    placeholder = { Text("Ask @AI_Tutor or chat with pod...") },
                    modifier = Modifier.weight(1f).testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MintGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    ),
                    maxLines = 3
                )
                FloatingActionButton(
                    onClick = { viewModel.sendChatMessage() },
                    modifier = Modifier.size(52.dp).testTag("send_chat_btn"),
                    containerColor = MintGreen,
                    contentColor = PureMidnight,
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }
        }

        // Frosted / Blur Focus Blocker Overlay Gate
        AnimatedVisibility(
            visible = isTimerActive,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FocusBlockerOverlay(viewModel = viewModel)
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isMe = message.sender == "You"
    val isSystem = message.sender == "System"
    val isAi = message.isAiTutor

    if (isSystem) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            Column(
                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = message.sender,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isMe -> MintGreen
                        isAi -> SoftOrange
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                val bubbleColor = when {
                    isMe -> MintGreen
                    isAi -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val textColor = when {
                    isMe -> PureMidnight
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Card(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 0.dp,
                        bottomEnd = if (isMe) 0.dp else 16.dp
                    ),
                    colors = CardDefaults.cardColors(containerColor = bubbleColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun AiTypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = SoftOrange, modifier = Modifier.size(16.dp))
                Text(
                    text = "AI Tutor is writing...",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftOrange
                )
            }
        }
    }
}

@Composable
fun FocusBlockerOverlay(viewModel: StudyViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("focus_blocker_gate")
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LockClock,
                contentDescription = "Focus",
                tint = MintGreen,
                modifier = Modifier.size(60.dp)
            )

            Text(
                text = "Study Session In Progress! ⏳",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            val mins = viewModel.pomodoroTimeLeftSeconds / 60
            val secs = viewModel.pomodoroTimeLeftSeconds % 60
            Text(
                text = String.format("Focus Mode is Active\nTime Remaining: %02d:%02d", mins, secs),
                fontSize = 15.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Text(
                text = "Chat notifications and group rooms are locked to optimize cognitive focus and block instant dopamine loops.",
                fontSize = 12.sp,
                color = TextMuted.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = { viewModel.toggleTimer() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("stop_focus_btn")
            ) {
                Text("Pause Study & Unlock Chat", fontWeight = FontWeight.Bold)
            }
        }
    }
}
