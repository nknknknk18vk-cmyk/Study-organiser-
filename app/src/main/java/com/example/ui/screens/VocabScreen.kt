package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VocabItem
import com.example.ui.StudyViewModel
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PureMidnight
import com.example.ui.theme.SoftOrange

@Composable
fun VocabScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val vocabList by viewModel.allVocab.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var deckIndex by remember { mutableStateOf(0) }
    var cardFlipped by remember { mutableStateOf(false) }

    // Filter vocab items due for review (nextReviewDate <= current time)
    val now = System.currentTimeMillis()
    val dueVocab = remember(vocabList, now) {
        vocabList.filter { it.nextReviewDate <= now }
    }

    // Filter library list
    val filteredLibrary = remember(vocabList, searchQuery) {
        vocabList.filter {
            it.word.contains(searchQuery, ignoreCase = true) ||
            it.definition.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Stats Overview Card with Progress Ring
        item {
            VocabStatsOverviewCard(vocabList = vocabList, dueCount = dueVocab.size)
        }

        // Gamified Spaced Repetition Flashcard Review Deck
        item {
            Text(
                text = "Spaced Repetition Review Deck",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            if (dueVocab.isNotEmpty()) {
                val currentCard = dueVocab.getOrNull(deckIndex % dueVocab.size)
                if (currentCard != null) {
                    FlashcardReviewWidget(
                        vocab = currentCard,
                        cardFlipped = cardFlipped,
                        onFlip = { cardFlipped = !cardFlipped },
                        onReviewSubmit = { rating ->
                            viewModel.submitVocabReview(currentCard, rating)
                            cardFlipped = false
                            if (deckIndex + 1 >= dueVocab.size) {
                                deckIndex = 0
                            } else {
                                deckIndex++
                            }
                        }
                    )
                }
            } else {
                NoReviewsWidget(vocabListEmpty = vocabList.isEmpty())
            }
        }

        // Vocab Library Browser List with Search
        item {
            Text(
                text = "Vocabulary Library Browser",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search word or definition...") },
                modifier = Modifier.fillMaxWidth().testTag("vocab_search_field"),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MintGreen,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ),
                singleLine = true
            )
        }

        // Library items lists
        if (filteredLibrary.isNotEmpty()) {
            items(filteredLibrary) { item ->
                VocabLibraryItemCard(vocab = item)
            }
        } else {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No vocabulary items match your search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun VocabStatsOverviewCard(
    vocabList: List<VocabItem>,
    dueCount: Int
) {
    val totalCount = vocabList.size
    // Calculate retention metric (items in Box 3 or higher are considered 'retained')
    val retainedCount = vocabList.count { it.boxLevel >= 3 }
    val retentionRate = if (totalCount > 0) (retainedCount.toFloat() / totalCount * 100).toInt() else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vocab_stats_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = "Vocabulary Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("Total Words", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text("$totalCount", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Column {
                        Text("Due Now", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text("$dueCount", fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (dueCount > 0) SoftOrange else MintGreen)
                    }
                    Column {
                        Text("Mastered", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                        Text("$retainedCount", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MintGreen)
                    }
                }
            }

            // Stat progress ring
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = if (totalCount > 0) retainedCount.toFloat() / totalCount else 0f,
                    modifier = Modifier.fillMaxSize(),
                    color = MintGreen,
                    strokeWidth = 10.dp,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$retentionRate%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Mastery",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun FlashcardReviewWidget(
    vocab: VocabItem,
    cardFlipped: Boolean,
    onFlip: () -> Unit,
    onReviewSubmit: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("flashcard_review_widget"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Leitner Box ${vocab.boxLevel}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintGreen
                    )
                }
                Text(
                    text = "Tap Card to Flip",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Inner Flashcard Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .clickable { onFlip() }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                VocabCardContent(cardFlipped = cardFlipped, vocab = vocab)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Gamified Leitner rating selectors
            if (cardFlipped) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "How well did you know this word?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onReviewSubmit(1) }, // Forgot
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("vocab_forgot_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Forgot", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                        Button(
                            onClick = { onReviewSubmit(2) }, // Hard
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("vocab_hard_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Hard", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                        Button(
                            onClick = { onReviewSubmit(3) }, // Easy
                            colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                            modifier = Modifier.weight(1f).height(40.dp).testTag("vocab_easy_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Easy", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PureMidnight)
                        }
                    }
                }
            } else {
                Text(
                    text = "Tap on the card above to reveal definitions.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun NoReviewsWidget(vocabListEmpty: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("no_reviews_widget"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = MintGreen,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = if (vocabListEmpty) "Your Vocabulary Deck is Empty!" else "Deck Fully Reviewed! 🎉",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (vocabListEmpty) "Add new vocabulary items while reading the Daily Chronicle Newspaper or write custom notes." else "Outstanding work! All active vocabulary terms have been scheduled using Spaced Repetition.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun VocabLibraryItemCard(vocab: VocabItem) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("vocab_lib_item_${vocab.word}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vocab.word,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MintGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Box ${vocab.boxLevel}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = vocab.definition,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "\"${vocab.contextSentence}\"",
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun VocabCardContent(
    cardFlipped: Boolean,
    vocab: com.example.data.VocabItem
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = !cardFlipped,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = vocab.word,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        AnimatedVisibility(
            visible = cardFlipped,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = vocab.definition,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${vocab.contextSentence}\"",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
