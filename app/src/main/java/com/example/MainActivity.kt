package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudyViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PureMidnight
import com.example.ui.theme.SoftOrange

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize the view model directly using application instance
                val viewModel = remember { StudyViewModel(application) }

                MainContainer(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainContainer(viewModel: StudyViewModel) {
    var homeSubTab by remember { mutableStateOf("overview") } // "overview" or "classes"

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("main_scaffold"),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                val current = viewModel.currentScreen

                NavigationBarItem(
                    selected = current == "dashboard",
                    onClick = { viewModel.currentScreen = "dashboard" },
                    icon = { Icon(if (current == "dashboard") Icons.Filled.Dashboard else Icons.Outlined.Dashboard, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_dashboard")
                )

                NavigationBarItem(
                    selected = current == "news",
                    onClick = { viewModel.currentScreen = "news" },
                    icon = { Icon(if (current == "news") Icons.Filled.MenuBook else Icons.Outlined.MenuBook, contentDescription = "Chronicle") },
                    label = { Text("Chronicle", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_news")
                )

                NavigationBarItem(
                    selected = current == "vocab",
                    onClick = { viewModel.currentScreen = "vocab" },
                    icon = { Icon(if (current == "vocab") Icons.Filled.School else Icons.Outlined.School, contentDescription = "Vocab") },
                    label = { Text("Vocab", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_vocab")
                )

                NavigationBarItem(
                    selected = current == "chat",
                    onClick = { viewModel.currentScreen = "chat" },
                    icon = { Icon(if (current == "chat") Icons.Filled.Chat else Icons.Outlined.Chat, contentDescription = "Pod Chat") },
                    label = { Text("Pod Chat", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_chat")
                )

                NavigationBarItem(
                    selected = current == "notes",
                    onClick = { viewModel.currentScreen = "notes" },
                    icon = { Icon(if (current == "notes") Icons.Filled.StickyNote2 else Icons.Outlined.StickyNote2, contentDescription = "Notes") },
                    label = { Text("Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("nav_notes")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen router
            when (viewModel.currentScreen) {
                "dashboard" -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top segment tab selection for Home Dashboard
                        HomeHeaderSelector(
                            selectedTab = homeSubTab,
                            onTabChange = { homeSubTab = it }
                        )

                        AnimatedContent(
                            targetState = homeSubTab,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "home_tabs"
                        ) { tab ->
                            if (tab == "overview") {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigate = { target ->
                                        if (target == "classes") {
                                            homeSubTab = "classes"
                                            viewModel.currentScreen = "dashboard"
                                        } else {
                                            viewModel.currentScreen = target
                                        }
                                    }
                                )
                            } else {
                                ClassScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
                "news" -> JournalScreen(viewModel = viewModel)
                "vocab" -> VocabScreen(viewModel = viewModel)
                "chat" -> ChatScreen(viewModel = viewModel)
                "notes" -> NotesScreen(viewModel = viewModel)
            }

            // Global floating PiP video player simulation overlay
            if (viewModel.showPiPPlayer) {
                FloatingPiPPlayer(
                    viewModel = viewModel,
                    onExpand = {
                        homeSubTab = "classes"
                        viewModel.currentScreen = "dashboard"
                    }
                )
            }
        }
    }
}

@Composable
fun HomeHeaderSelector(
    selectedTab: String,
    onTabChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button(
            onClick = { onTabChange("overview") },
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .testTag("home_overview_tab"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == "overview") MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (selectedTab == "overview") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("Overview", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Button(
            onClick = { onTabChange("classes") },
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .testTag("home_classes_tab"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTab == "classes") MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (selectedTab == "classes") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tv, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("Lecture Hall", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun BoxScope.FloatingPiPPlayer(
    viewModel: StudyViewModel,
    onExpand: () -> Unit
) {
    val activeClass = viewModel.activeClassStream ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "pip_pulse")
    val waveHeight by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_height"
    )

    Card(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(bottom = 16.dp, end = 16.dp)
            .width(180.dp)
            .height(110.dp)
            .clickable { onExpand() }
            .testTag("pip_player_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureMidnight),
        border = BorderStroke(1.5.dp, MintGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mini Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                    Text(
                        text = "LIVE CLASS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        letterSpacing = 0.5.sp
                    )
                }
                IconButton(
                    onClick = { viewModel.closePiPPlayer() },
                    modifier = Modifier.size(20.dp).testTag("close_pip_btn")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }

            // Waveform simulation representing lecture audio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .drawBehind {
                        val brush = Brush.linearGradient(
                            colors = listOf(MintGreen, Color(0xFF00C896)),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                        // Draw animated wavy lines representing stream spectrum
                        val centerY = size.height / 2
                        val count = 8
                        val spacing = size.width / (count + 1)
                        for (i in 1..count) {
                            val x = i * spacing
                            val h = if (i % 2 == 0) waveHeight else waveHeight * 0.7f
                            drawLine(
                                brush = brush,
                                start = Offset(x, centerY - h),
                                end = Offset(x, centerY + h),
                                strokeWidth = 5.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                    }
            )

            // Lecture Meta info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activeClass.title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.OpenInFull,
                    contentDescription = "Expand",
                    tint = MintGreen,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
