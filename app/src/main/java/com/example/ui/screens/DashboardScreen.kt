package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudyViewModel
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PureMidnight
import com.example.ui.theme.SoftOrange

@Composable
fun DashboardScreen(
    viewModel: StudyViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.allNotes.collectAsState()
    val vocab by viewModel.allVocab.collectAsState()
    val chatMessages by viewModel.allMessages.collectAsState()
    val classes by viewModel.allClasses.collectAsState()
    val totalMinutesToday by viewModel.totalMinutesToday.collectAsState()

    val currentLiveClass = classes.find { it.isLive }
    val latestNote = notes.firstOrNull()
    val latestMessage = chatMessages.lastOrNull()
    val randomWord = remember(vocab) { vocab.randomOrNull() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Welcome and Streak Section
        item {
            WelcomeHeader(totalMinutesToday = totalMinutesToday)
        }

        // Bento Grid Layout Simulation using Rows
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Live Class Card (Pulsing Large Tile)
                LiveClassTile(
                    liveClass = currentLiveClass,
                    onJoin = { liveClass ->
                        viewModel.joinLiveClass(liveClass)
                        onNavigate("classes")
                    }
                )

                // Row for Vocab and Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        WordOfTheDayTile(
                            vocabWord = randomWord,
                            onNavigateToVocab = { onNavigate("vocab") }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        FocusTimerWidgetTile(
                            viewModel = viewModel,
                            onNavigateToTimer = { onNavigate("dashboard") } // Keep on dashboard but highlight timer
                        )
                    }
                }

                // 2. Daily Chronicle Newspaper Tile (Wide Card)
                viewModel.activeArticle?.let { article ->
                    DailyChronicleTile(
                        article = article,
                        onNavigateToNews = { onNavigate("news") }
                    )
                }

                // Row for Notes and Chat Pod
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        QuickNotesTile(
                            latestNote = latestNote,
                            onNavigateToNotes = { onNavigate("notes") }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        CollaborativeChatTile(
                            latestMessage = latestMessage,
                            onNavigateToChat = { onNavigate("chat") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeHeader(totalMinutesToday: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("welcome_header"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color(0x1110B981)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                    drawRect(brush)
                }
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, Scholar! 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ready to complete your knowledge loop?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                // Stats Badge
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Streak",
                            tint = SoftOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "5 Day Streak",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = SoftOrange
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalMinutesToday / 45 Mins Studied",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun LiveClassTile(
    liveClass: com.example.data.OnlineClass?,
    onJoin: (com.example.data.OnlineClass) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_class_tile"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .drawBehind {
                                drawCircle(
                                    color = MintGreen,
                                    radius = size.minDimension / 2 * scale
                                )
                            }
                    )
                    Text(
                        text = "LIVE LECTURE",
                        fontWeight = FontWeight.Bold,
                        color = MintGreen,
                        letterSpacing = 1.sp,
                        fontSize = 11.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (liveClass != null) {
                Text(
                    text = liveClass.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Instructor: ${liveClass.instructor}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onJoin(liveClass) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("join_lecture_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = PureMidnight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PlayCircleFilled, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text("Join & Start Study Loop", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            } else {
                Text(
                    text = "No Classes Active Right Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Check the schedule tab for upcoming reviews.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun WordOfTheDayTile(
    vocabWord: com.example.data.VocabItem?,
    onNavigateToVocab: () -> Unit
) {
    var flipped by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { flipped = !flipped }
            .testTag("word_day_tile"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "VOCAB BOOST",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = if (flipped) Icons.Default.Visibility else Icons.Default.RotateRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DashboardVocabContent(flipped = flipped, vocabWord = vocabWord)
            }

            Text(
                text = "Tap to Flip Card",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun FocusTimerWidgetTile(
    viewModel: StudyViewModel,
    onNavigateToTimer: () -> Unit
) {
    val totalTime = viewModel.selectedTimerDurationMinutes * 60
    val progress = if (totalTime > 0) viewModel.pomodoroTimeLeftSeconds.toFloat() / totalTime else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { viewModel.toggleTimer() }
            .testTag("focus_timer_tile"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (viewModel.isTimerRunning) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "FOCUS MODE",
                    fontWeight = FontWeight.Bold,
                    color = if (viewModel.isTimerRunning) MintGreen else MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = if (viewModel.isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = if (viewModel.isTimerRunning) MintGreen else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val mins = viewModel.pomodoroTimeLeftSeconds / 60
                val secs = viewModel.pomodoroTimeLeftSeconds % 60
                Text(
                    text = String.format("%02d:%02d", mins, secs),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = if (viewModel.isTimerRunning) MintGreen else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .width(70.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MintGreen,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }

            Text(
                text = if (viewModel.isTimerRunning) "Studying... Tap Pause" else "Tap to Start Timer",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isTimerRunning) MintGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun DailyChronicleTile(
    article: com.example.data.GeneratedNewspaperArticle,
    onNavigateToNews: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToNews() }
            .testTag("daily_chronicle_tile"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "DAILY CHRONICLE NEWSPAPER",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "${article.readingTimeMin} min read",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = article.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MintGreen, modifier = Modifier.size(14.dp))
                Text(
                    text = "Tap to Simplify with AI & Take Quiz",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MintGreen
                )
            }
        }
    }
}

@Composable
fun QuickNotesTile(
    latestNote: com.example.data.StudyNote?,
    onNavigateToNotes: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onNavigateToNotes() }
            .testTag("quick_notes_tile"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "STUDY NOTES",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }

            if (latestNote != null) {
                Column {
                    Text(
                        text = latestNote.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = latestNote.content,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = "No study notes written yet. Tap to write first notes!",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Text(
                text = "View Notes Library",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CollaborativeChatTile(
    latestMessage: com.example.data.ChatMessage?,
    onNavigateToChat: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onNavigateToChat() }
            .testTag("collaborative_chat_tile"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "COLLAB CHAT",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }

            if (latestMessage != null) {
                Column {
                    Text(
                        text = latestMessage.sender,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = MintGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = latestMessage.text,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = "Welcome study pod! No active chats today.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Text(
                text = "Chat with Pod & AI Tutor",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun DashboardVocabContent(
    flipped: Boolean,
    vocabWord: com.example.data.VocabItem?
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = !flipped,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Text(
                text = vocabWord?.word ?: "Aesthetic",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(
            visible = flipped,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Text(
                text = vocabWord?.definition ?: "Appreciation of beauty.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
