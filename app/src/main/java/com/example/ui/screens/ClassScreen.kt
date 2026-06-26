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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OnlineClass
import com.example.ui.StudyViewModel
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PureMidnight
import com.example.ui.theme.SoftOrange

@Composable
fun ClassScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val classesList by viewModel.allClasses.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Banner Header
        item {
            OnlineClassesBanner()
        }

        // Live now section
        val liveClasses = classesList.filter { it.isLive }
        if (liveClasses.isNotEmpty()) {
            item {
                Text(
                    text = "Live Seminars",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(liveClasses) { item ->
                LiveClassCardItem(
                    onlineClass = item,
                    activeClassStream = viewModel.activeClassStream,
                    onJoin = { viewModel.joinLiveClass(item) },
                    onClose = { viewModel.closePiPPlayer() }
                )
            }
        }

        // Upcoming section
        val upcomingClasses = classesList.filter { !it.isLive }
        item {
            Text(
                text = "Upcoming Scheduled Class Lectures",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (upcomingClasses.isNotEmpty()) {
            items(upcomingClasses) { item ->
                UpcomingClassCardItem(onlineClass = item)
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No upcoming classes found.",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun OnlineClassesBanner() {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("classes_banner"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
            Column {
                Text(
                    text = "Interactive Seminar Lecture Hall",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Seamlessly join zoom seminars, take synchronized notes, and run focus timer modes together.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun LiveClassCardItem(
    onlineClass: OnlineClass,
    activeClassStream: OnlineClass?,
    onJoin: () -> Unit,
    onClose: () -> Unit
) {
    val isCurrentlyActive = activeClassStream?.id == onlineClass.id

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_class_card_${onlineClass.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyActive) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, MintGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEF4444))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE NOW",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = onlineClass.timeStr,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentlyActive) MintGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = onlineClass.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCurrentlyActive) Color.White else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Conducted by: ${onlineClass.instructor}",
                fontSize = 13.sp,
                color = if (isCurrentlyActive) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isCurrentlyActive) {
                OutlinedButton(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("disconnect_class_btn"),
                    border = BorderStroke(1.dp, Color.Red),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Disconnect Stream", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onJoin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("join_live_class_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = PureMidnight),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Enter Digital Room", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingClassCardItem(onlineClass: OnlineClass) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upcoming_class_card_${onlineClass.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = onlineClass.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Instructor: ${onlineClass.instructor}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = onlineClass.timeStr,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
