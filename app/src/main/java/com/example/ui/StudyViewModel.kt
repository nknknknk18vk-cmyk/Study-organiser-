package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = StudyRepository(database)

    // Current screen navigation state
    var currentScreen by mutableStateOf("dashboard")

    // Database Feeds
    val allNotes: StateFlow<List<StudyNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVocab: StateFlow<List<VocabItem>> = repository.allVocab
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<ChatMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLogs: StateFlow<List<StudyLog>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClasses: StateFlow<List<OnlineClass>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newspaperArticles: StateFlow<List<GeneratedNewspaperArticle>> = repository.articles

    // Active Newspaper Article UI states
    var activeArticle by mutableStateOf<GeneratedNewspaperArticle?>(null)
    var isArticleSimplified by mutableStateOf(false)
    var activeArticleTopicInput by mutableStateOf("")
    var isGeneratingArticle by mutableStateOf(false)
    var activeVocabWordDefinition by mutableStateOf<NewspaperVocabItem?>(null)

    // Active Quiz states
    var activeQuizQuestionIndex by mutableStateOf(0)
    var selectedQuizOptionIndex by mutableStateOf<Int?>(null)
    var isQuizAnswerCorrect by mutableStateOf<Boolean?>(null)
    var showQuizResult by mutableStateOf(false)
    var quizScore by mutableStateOf(0)

    // Chat states
    var chatInput by mutableStateOf("")
    var isAiTutorResponding by mutableStateOf(false)

    // Notes states
    var noteTitleInput by mutableStateOf("")
    var noteContentInput by mutableStateOf("")
    var noteTagsInput by mutableStateOf("")
    var editingNote by mutableStateOf<StudyNote?>(null)
    var selectedTagFilter by mutableStateOf<String?>(null)
    var isAiAutoTagging by mutableStateOf(false)

    // Live Class PiP Sim state
    var activeClassStream by mutableStateOf<OnlineClass?>(null)
    var showPiPPlayer by mutableStateOf(false)

    // Time Management / Pomodoro Focus Mode state
    var isTimerRunning by mutableStateOf(false)
    var pomodoroTimeLeftSeconds by mutableStateOf(25 * 60) // 25 mins
    var selectedTimerDurationMinutes by mutableStateOf(25)
    var isFocusModeActive by mutableStateOf(false) // Blocks notifications / chat
    private var timerJob: Job? = null

    // Daily study stats
    val totalMinutesToday = allLogs.map { logs ->
        val todayStr = getTodayDateString()
        logs.filter { it.dateStr == todayStr }.sumOf { it.durationMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        activeArticle = repository.articles.value.firstOrNull()
    }

    private fun getTodayDateString(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    // --- Daily Journal AI Pipeline ---
    fun selectArticle(article: GeneratedNewspaperArticle) {
        activeArticle = article
        isArticleSimplified = false
        activeVocabWordDefinition = null
        activeQuizQuestionIndex = 0
        selectedQuizOptionIndex = null
        isQuizAnswerCorrect = null
        showQuizResult = false
        quizScore = 0
    }

    fun generateNewspaperArticle() {
        if (activeArticleTopicInput.isBlank()) return
        isGeneratingArticle = true
        viewModelScope.launch {
            val article = repository.generateAndAddArticle(activeArticleTopicInput)
            if (article != null) {
                activeArticle = article
                activeArticleTopicInput = ""
                isArticleSimplified = false
                activeVocabWordDefinition = null
            }
            isGeneratingArticle = false
        }
    }

    fun saveVocabItem(word: String, definition: String, context: String) {
        viewModelScope.launch {
            val existing = repository.allVocab.firstOrNull()?.find { it.word.lowercase() == word.lowercase() }
            if (existing == null) {
                repository.insertVocab(
                    VocabItem(
                        word = word,
                        definition = definition,
                        contextSentence = context
                    )
                )
            }
        }
    }

    fun submitQuizAnswer(optionIndex: Int) {
        val article = activeArticle ?: return
        val currentQuestion = article.quiz.getOrNull(activeQuizQuestionIndex) ?: return
        selectedQuizOptionIndex = optionIndex
        val correct = optionIndex == currentQuestion.correctIndex
        isQuizAnswerCorrect = correct
        if (correct) {
            quizScore++
        }
    }

    fun nextQuizQuestion() {
        val article = activeArticle ?: return
        if (activeQuizQuestionIndex + 1 < article.quiz.size) {
            activeQuizQuestionIndex++
            selectedQuizOptionIndex = null
            isQuizAnswerCorrect = null
        } else {
            showQuizResult = true
        }
    }

    // --- Collaborative Chat Section & AI Tutor ---
    fun sendChatMessage() {
        if (chatInput.isBlank()) return
        val text = chatInput
        chatInput = ""

        viewModelScope.launch {
            // Insert user message
            repository.insertChatMessage(
                ChatMessage(
                    sender = "You",
                    text = text,
                    isFromAi = false
                )
            )

            // Trigger AI Tutor if @AI_Tutor is mentioned
            if (text.contains("@AI_Tutor", ignoreCase = true) || text.endsWith("?")) {
                isAiTutorResponding = true
                delay(1000) // Realistic typing delay

                val chatHistory = repository.allMessages.first()
                val response = GeminiService.getAiTutorResponse(chatHistory, text)

                repository.insertChatMessage(
                    ChatMessage(
                        sender = "AI Tutor ✨",
                        text = response,
                        isFromAi = true,
                        isAiTutor = true
                    )
                )
                isAiTutorResponding = false
            } else {
                // Occasional peer responses to make it a vibrant collaborative chat
                delay(1500)
                val peerResponses = listOf(
                    "Agreed! I found that part of the article incredibly useful.",
                    "Wait, can someone explain that first vocabulary term again?",
                    "Thanks for sharing! Let's review this together in the Study Pod tomorrow."
                )
                repository.insertChatMessage(
                    ChatMessage(
                        sender = "Chloe (Study Partner)",
                        text = peerResponses.random(),
                        isFromAi = false
                    )
                )
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
            repository.insertChatMessage(
                ChatMessage(
                    sender = "System",
                    text = "Welcome to the collaborative student chat! Discuss daily newspapers or ask questions to @AI_Tutor.",
                    isFromAi = false
                )
            )
        }
    }

    // --- Time Management (Focus Mode Timer) ---
    fun setTimerDuration(minutes: Int) {
        selectedTimerDurationMinutes = minutes
        if (!isTimerRunning) {
            pomodoroTimeLeftSeconds = minutes * 60
        }
    }

    fun toggleTimer() {
        if (isTimerRunning) {
            stopTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        isTimerRunning = true
        timerJob = viewModelScope.launch {
            while (pomodoroTimeLeftSeconds > 0) {
                delay(1000)
                pomodoroTimeLeftSeconds--
            }
            // Timer finished!
            repository.insertStudyLog(selectedTimerDurationMinutes, "Focus Session")
            pomodoroTimeLeftSeconds = selectedTimerDurationMinutes * 60
            isTimerRunning = false
            isFocusModeActive = false
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        isTimerRunning = false
    }

    fun resetTimer() {
        stopTimer()
        pomodoroTimeLeftSeconds = selectedTimerDurationMinutes * 60
    }

    // --- Study Notes ---
    fun saveNote() {
        if (noteTitleInput.isBlank()) return
        viewModelScope.launch {
            val note = editingNote
            if (note == null) {
                repository.insertNote(
                    StudyNote(
                        title = noteTitleInput,
                        content = noteContentInput,
                        tags = noteTagsInput
                    )
                )
            } else {
                repository.updateNote(
                    note.copy(
                        title = noteTitleInput,
                        content = noteContentInput,
                        tags = noteTagsInput,
                        lastEdited = System.currentTimeMillis()
                    )
                )
            }
            clearNoteInputs()
        }
    }

    fun editNoteSelect(note: StudyNote) {
        editingNote = note
        noteTitleInput = note.title
        noteContentInput = note.content
        noteTagsInput = note.tags
    }

    fun deleteNote(note: StudyNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun clearNoteInputs() {
        editingNote = null
        noteTitleInput = ""
        noteContentInput = ""
        noteTagsInput = ""
    }

    fun triggerAiAutoTag() {
        if (noteContentInput.isBlank()) return
        isAiAutoTagging = true
        viewModelScope.launch {
            val prompt = """
                Analyze the following student study notes and generate 3 highly relevant tags (comma-separated, without '#' prefix) and a brief 1-sentence summary.
                
                STUDY NOTES:
                $noteContentInput
                
                Format the output exactly as JSON:
                {
                  "tags": "tag1, tag2, tag3",
                  "summary": "This is a 1-sentence summary of the topic."
                }
            """.trimIndent()

            val result = GeminiService.generateText(prompt, isJson = true)
            if (result != null) {
                try {
                    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                    val adapter = moshi.adapter(Map::class.java)
                    val map = adapter.fromJson(result)
                    val tags = map?.get("tags") as? String
                    val summary = map?.get("summary") as? String
                    if (tags != null) {
                        noteTagsInput = tags
                    }
                    if (summary != null) {
                        noteContentInput = noteContentInput + "\n\n✨ AI Summary: " + summary
                    }
                } catch (e: Exception) {
                    // Fallback
                    noteTagsInput = "AI-Generated, Study"
                }
            } else {
                noteTagsInput = "Study, Smart"
            }
            isAiAutoTagging = false
        }
    }

    // --- Vocab Review gamification ---
    fun submitVocabReview(vocab: VocabItem, rating: Int) {
        viewModelScope.launch {
            repository.reviewVocabWord(vocab, rating)
        }
    }

    // --- Live Class operations ---
    fun joinLiveClass(onlineClass: OnlineClass) {
        activeClassStream = onlineClass
        showPiPPlayer = true
        // Set up study focus timer automatically!
        setTimerDuration(45) // Set 45 mins class timer
        startTimer()
    }

    fun closePiPPlayer() {
        showPiPPlayer = false
        activeClassStream = null
    }
}
