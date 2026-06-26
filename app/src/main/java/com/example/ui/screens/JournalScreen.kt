package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeneratedNewspaperArticle
import com.example.data.NewspaperVocabItem
import com.example.ui.StudyViewModel
import com.example.ui.theme.MintGreen
import com.example.ui.theme.PureMidnight
import com.example.ui.theme.SoftOrange

@Composable
fun JournalScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val articles by viewModel.newspaperArticles.collectAsState()
    val activeArticle = viewModel.activeArticle

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // AI News Topic Generator Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("news_generator_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✨ Generate AI Newspaper Article",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Enter any topic to let Gemini write an AP-level article with integrated quiz & vocab!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = viewModel.activeArticleTopicInput,
                            onValueChange = { viewModel.activeArticleTopicInput = it },
                            placeholder = { Text("e.g. Black Holes, Photosynthesis") },
                            modifier = Modifier.weight(1f).height(52.dp).testTag("news_topic_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.generateNewspaperArticle() }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MintGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                        )
                        Button(
                            onClick = { viewModel.generateNewspaperArticle() },
                            modifier = Modifier.height(52.dp).testTag("generate_news_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = PureMidnight),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !viewModel.isGeneratingArticle && viewModel.activeArticleTopicInput.isNotBlank()
                        ) {
                            if (viewModel.isGeneratingArticle) {
                                CircularProgressIndicator(color = PureMidnight, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }

        // Horizontal list of articles (Chronicle Feed)
        item {
            Text(
                text = "Chronicle Library",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(articles) { article ->
                    val isSelected = article == activeArticle
                    Card(
                        modifier = Modifier
                            .width(220.dp)
                            .height(100.dp)
                            .clickable { viewModel.selectArticle(article) }
                            .testTag("article_selector_${article.title.take(10)}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp).fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = article.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "By ${article.author}",
                                    fontSize = 10.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "${article.readingTimeMin}m read",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isSelected) MintGreen else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Newspaper Content Block
        activeArticle?.let { article ->
            item {
                ActiveNewspaperView(
                    article = article,
                    viewModel = viewModel
                )
            }

            // Word definition overlay block
            viewModel.activeVocabWordDefinition?.let { vocab ->
                item {
                    VocabWordPopupCard(
                        vocab = vocab,
                        contextSentence = article.content,
                        onAdd = {
                            viewModel.saveVocabItem(vocab.word, vocab.definition, article.content)
                            viewModel.activeVocabWordDefinition = null
                        },
                        onClose = { viewModel.activeVocabWordDefinition = null }
                    )
                }
            }

            // Quiz Section
            item {
                QuizSectionView(
                    article = article,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun ActiveNewspaperView(
    article: GeneratedNewspaperArticle,
    viewModel: StudyViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("active_newspaper_view"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Title and Meta
            Text(
                text = article.title,
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Published by ${article.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontStyle = FontStyle.Italic
                )
                Text(
                    text = "${article.readingTimeMin} Min Read",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MintGreen
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            // AI Toggle Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = MintGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "✨ Simplify Complex Topic",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = viewModel.isArticleSimplified,
                    onCheckedChange = { viewModel.isArticleSimplified = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MintGreen,
                        checkedTrackColor = MintGreen.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("simplify_switch")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body content
            val originalText = article.content
            val simplifiedText = article.simplifiedContent

            if (viewModel.isArticleSimplified) {
                // Simplified content with a glowing soft background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MintGreen.copy(alpha = 0.05f))
                        .border(1.dp, MintGreen.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MintGreen, modifier = Modifier.size(16.dp))
                            Text("Simplified Explanation:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MintGreen)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = simplifiedText,
                            style = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            } else {
                // Original text with smart interactive highlights
                val vocabWords = article.vocabulary
                val annotatedString = buildAnnotatedString {
                    var currentIndex = 0
                    val fullText = originalText

                    // We sort vocabulary items by length descending to match longest spans first and prevent inside-word conflicts
                    val sortedVocab = vocabWords.sortedByDescending { it.word.length }

                    // A simple regex approach or index matching
                    // To keep it perfectly robust, we can split text into words and match
                    val words = fullText.split(" ")
                    for (i in words.indices) {
                        val wordClean = words[i].trim { !it.isLetterOrDigit() }
                        val matchingVocab = sortedVocab.find { it.word.lowercase() == wordClean.lowercase() }

                        if (matchingVocab != null) {
                            pushStringAnnotation(tag = "vocab", annotation = matchingVocab.word)
                            withStyle(
                                style = SpanStyle(
                                    color = MintGreen,
                                    fontWeight = FontWeight.ExtraBold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(words[i])
                            }
                            pop()
                        } else {
                            append(words[i])
                        }
                        if (i < words.size - 1) append(" ")
                    }
                }

                ClickableText(
                    text = annotatedString,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.testTag("interactive_article_text"),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "vocab", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                val matchedVocab = vocabWords.find { it.word == annotation.item }
                                if (matchedVocab != null) {
                                    viewModel.activeVocabWordDefinition = matchedVocab
                                }
                            }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                    Text(
                        text = "💡 Tap on highlighted Mint words for instant definitions!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun VocabWordPopupCard(
    vocab: NewspaperVocabItem,
    contextSentence: String,
    onAdd: () -> Unit,
    onClose: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vocab_popup_card")
            .border(2.dp, MintGreen, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MintGreen, modifier = Modifier.size(18.dp))
                    Text(
                        text = vocab.word,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Definition: ${vocab.definition}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 Exam/Writing Tip: ${vocab.examTip}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().testTag("add_to_vocab_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = PureMidnight),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Add to Vocabulary Deck (Leitner Box)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuizSectionView(
    article: GeneratedNewspaperArticle,
    viewModel: StudyViewModel
) {
    val quiz = article.quiz
    val currentIdx = viewModel.activeQuizQuestionIndex
    val activeQuestion = quiz.getOrNull(currentIdx) ?: return

    Card(
        modifier = Modifier.fillMaxWidth().testTag("quiz_section_view"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "📝 Journal Knowledge Quiz",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Review concepts from this article to test your retention.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!viewModel.showQuizResult) {
                // Question Tracker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${currentIdx + 1} of ${quiz.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MintGreen
                    )
                    Text(
                        text = "Score: ${viewModel.quizScore}/${quiz.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = (currentIdx + 1).toFloat() / quiz.size,
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = MintGreen,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = activeQuestion.question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Options list
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activeQuestion.options.forEachIndexed { optIdx, option ->
                        val isSelected = viewModel.selectedQuizOptionIndex == optIdx
                        val isCorrectAns = optIdx == activeQuestion.correctIndex
                        val hasAnswered = viewModel.selectedQuizOptionIndex != null

                        val containerColor = when {
                            isSelected && viewModel.isQuizAnswerCorrect == true -> MintGreen.copy(alpha = 0.15f)
                            isSelected && viewModel.isQuizAnswerCorrect == false -> Color.Red.copy(alpha = 0.1f)
                            hasAnswered && isCorrectAns -> MintGreen.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                        }

                        val borderColor = when {
                            isSelected && viewModel.isQuizAnswerCorrect == true -> MintGreen
                            isSelected && viewModel.isQuizAnswerCorrect == false -> Color.Red
                            hasAnswered && isCorrectAns -> MintGreen
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(containerColor)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable(enabled = !hasAnswered) {
                                    viewModel.submitQuizAnswer(optIdx)
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = option,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (hasAnswered && isCorrectAns) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = MintGreen)
                                } else if (isSelected && viewModel.isQuizAnswerCorrect == false) {
                                    Icon(Icons.Default.Cancel, contentDescription = "Incorrect", tint = Color.Red)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (viewModel.selectedQuizOptionIndex != null) {
                    Button(
                        onClick = { viewModel.nextQuizQuestion() },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("next_quiz_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (currentIdx + 1 == quiz.size) "Finish Quiz" else "Next Question",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Quiz completed view
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Success",
                        tint = SoftOrange,
                        modifier = Modifier.size(60.dp)
                    )
                    Text(
                        text = "Quiz Completed!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "You scored ${viewModel.quizScore} out of ${quiz.size} points.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.showQuizResult = false
                            viewModel.activeQuizQuestionIndex = 0
                            viewModel.selectedQuizOptionIndex = null
                            viewModel.isQuizAnswerCorrect = null
                            viewModel.quizScore = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen, contentColor = PureMidnight),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("restart_quiz_btn")
                    ) {
                        Text("Try Again", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
