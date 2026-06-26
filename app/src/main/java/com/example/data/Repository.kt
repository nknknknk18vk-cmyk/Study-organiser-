package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class StudyRepository(private val database: AppDatabase) {

    // DAOs
    private val studyNoteDao = database.studyNoteDao()
    private val vocabItemDao = database.vocabItemDao()
    private val chatMessageDao = database.chatMessageDao()
    private val studyLogDao = database.studyLogDao()
    private val onlineClassDao = database.onlineClassDao()

    // Database reactive feeds
    val allNotes: Flow<List<StudyNote>> = studyNoteDao.getAllNotes()
    val allVocab: Flow<List<VocabItem>> = vocabItemDao.getAllVocab()
    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()
    val allLogs: Flow<List<StudyLog>> = studyLogDao.getAllLogs()
    val allClasses: Flow<List<OnlineClass>> = onlineClassDao.getAllClasses()

    // Curated preloaded articles in-memory cache
    private val _articles = MutableStateFlow<List<GeneratedNewspaperArticle>>(emptyList())
    val articles: StateFlow<List<GeneratedNewspaperArticle>> = _articles.asStateFlow()

    init {
        // Load default curated newspaper articles
        _articles.value = listOf(
            GeneratedNewspaperArticle(
                title = "The Secrets of Neural Networks: Mimicking the Mind",
                author = "Sarah Sterling",
                readingTimeMin = 4,
                content = "Deep Learning is powered by artificial neural networks, which are computational models inspired by the organic structure of biological brains. These networks consist of thousands of interconnected processing nodes, organized in layers. In a feedforward network, information flows in one direction: from input features, through multiple hidden layers, to the output predictions. The core mechanism is backpropagation—an algorithm that calculates gradients of the loss function with respect to the network weights, adjusting them iteratively via gradient descent. This allows the network to decipher non-linear patterns, from voice commands to medical images.",
                simplifiedContent = "Neural Networks are computer programs modeled after the human brain. Think of them as layers of tiny calculators (called nodes) working together. When you show the program a photo of a dog, the input layer takes in the pixels. It passes the information through 'hidden' layers that look for shapes, edges, and textures. Finally, the output layer guesses: 'Dog!' If it gets it wrong, a math process called 'backpropagation' goes backwards to correct the calculators. Over time, after seeing thousands of photos, the system learns from its mistakes and becomes incredibly accurate.",
                vocabulary = listOf(
                    NewspaperVocabItem(
                        word = "Backpropagation",
                        definition = "The process of calculating the error of a neural network and adjusting the weights backward to correct it.",
                        examTip = "Use this when describing how deep learning models learn from their mistakes during training phases."
                    ),
                    NewspaperVocabItem(
                        word = "Iteratively",
                        definition = "Repeating a process multiple times, with each repetition getting closer to a desired result.",
                        examTip = "An elegant synonym for 'gradually' or 'step-by-step' in engineering essays."
                    ),
                    NewspaperVocabItem(
                        word = "Decipher",
                        definition = "To succeed in understanding, interpreting, or identifying something complex.",
                        examTip = "Use in literature and science essays instead of 'figure out' to elevate the tone."
                    )
                ),
                quiz = listOf(
                    NewspaperQuizQuestion(
                        question = "What inspires the design of artificial neural networks?",
                        options = listOf("Biological brains and neurons", "Telescopic lenses", "Quantum atomic spin state", "Chemical synthesis loops"),
                        correctIndex = 0
                    ),
                    NewspaperQuizQuestion(
                        question = "Which algorithm adjusts weights by calculating error gradients backwards?",
                        options = listOf("Feedforward logic", "Backpropagation", "Linear Regression", "Sorting indexes"),
                        correctIndex = 1
                    )
                )
            ),
            GeneratedNewspaperArticle(
                title = " CRISPR Gene Editing: Rewriting the Code of Life",
                author = "Dr. Marcus Vance",
                readingTimeMin = 3,
                content = "CRISPR-Cas9 has revolutionized molecular biology, acting as programmable genetic scissors. Derived from an ancient antiviral defense mechanism in bacteria, CRISPR (Clustered Regularly Interspaced Short Palindromic Repeats) uses a guide RNA (gRNA) molecule to locate a highly specific sequence of DNA within a genome. The Cas9 endonuclease protein then binds to this target sequence and introduces a double-strand break. Cells repair this break using homologous recombination or non-homologous end-joining, allowing researchers to deactivate specific genes or seamlessly insert therapeutic genetic material, addressing hereditary ailments.",
                simplifiedContent = "CRISPR is a tool that lets scientists edit DNA, like the 'Find and Replace' feature in Word. It is based on a defense system bacteria use to fight off viruses. Scientists create a guide molecule (gRNA) that matches the exact part of the DNA they want to fix. It acts like a GPS. A special protein (Cas9) follows this guide and acts as scissors to cut the DNA. When the cell tries to heal the cut, scientists can turn off a faulty gene or insert a healthy one. This can help cure serious genetic diseases.",
                vocabulary = listOf(
                    NewspaperVocabItem(
                        word = "Endonuclease",
                        definition = "An enzyme that cuts a DNA chain at a specific location inside the sequence.",
                        examTip = "Excellent term for biology essays describing genetic cutting tools."
                    ),
                    NewspaperVocabItem(
                        word = "Genome",
                        definition = "The complete set of genes or genetic material present in a cell or organism.",
                        examTip = "Perfect for biology reports when talking about the entire DNA profile of a subject."
                    ),
                    NewspaperVocabItem(
                        word = "Hereditary",
                        definition = "Passed on from parent to offspring through genes.",
                        examTip = "A professional term to replace 'passed down' in medical contexts."
                    )
                ),
                quiz = listOf(
                    NewspaperQuizQuestion(
                        question = "What does Cas9 act as in the CRISPR mechanism?",
                        options = listOf("Genetic scissors", "Visual guide sensor", "Cell membrane key", "Energy battery"),
                        correctIndex = 0
                    ),
                    NewspaperQuizQuestion(
                        question = "Where was CRISPR originally found?",
                        options = listOf("In bacteria defense mechanisms", "In synthetic crystals", "In outer space meteorites", "In deep ocean thermal vents"),
                        correctIndex = 0
                    )
                )
            )
        )
    }

    // Study Notes operations
    suspend fun insertNote(note: StudyNote): Long = studyNoteDao.insertNote(note)
    suspend fun updateNote(note: StudyNote) = studyNoteDao.updateNote(note)
    suspend fun deleteNote(note: StudyNote) = studyNoteDao.deleteNote(note)

    // Vocab operations
    suspend fun insertVocab(vocab: VocabItem): Long = vocabItemDao.insertVocab(vocab)
    suspend fun updateVocab(vocab: VocabItem) = vocabItemDao.updateVocab(vocab)
    suspend fun deleteVocab(vocab: VocabItem) = vocabItemDao.deleteVocab(vocab)

    /**
     * Spaced Repetition (SM-2) review logic adjustment
     * word rating: 1 (Forgot completely), 2 (Hard), 3 (Easy)
     */
    suspend fun reviewVocabWord(vocab: VocabItem, rating: Int) {
        val nextLevel: Int
        val ease: Float
        val daysToAdd: Int

        when (rating) {
            1 -> { // Forgot completely / failed
                nextLevel = 1
                ease = (vocab.easeFactor - 0.2f).coerceAtLeast(1.3f)
                daysToAdd = 1
            }
            2 -> { // Hard
                nextLevel = (vocab.boxLevel + 1).coerceAtMost(5)
                ease = vocab.easeFactor
                daysToAdd = when (vocab.boxLevel) {
                    1 -> 1
                    2 -> 3
                    else -> (vocab.boxLevel * ease).toInt()
                }
            }
            else -> { // Easy / Perfect
                nextLevel = (vocab.boxLevel + 1).coerceAtMost(5)
                ease = (vocab.easeFactor + 0.15f).coerceAtMost(3.0f)
                daysToAdd = when (vocab.boxLevel) {
                    1 -> 2
                    2 -> 6
                    else -> (vocab.boxLevel * ease * 1.5f).toInt()
                }
            }
        }

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)

        val updated = vocab.copy(
            boxLevel = nextLevel,
            easeFactor = ease,
            nextReviewDate = cal.timeInMillis,
            lastReviewed = System.currentTimeMillis()
        )
        vocabItemDao.updateVocab(updated)
    }

    // Chat operations
    suspend fun insertChatMessage(message: ChatMessage): Long = chatMessageDao.insertMessage(message)
    suspend fun clearChat() = chatMessageDao.clearChat()

    // Study Logs operations
    suspend fun insertStudyLog(minutes: Int, sessionType: String) {
        val todayStr = getTodayDateString()
        studyLogDao.insertLog(
            StudyLog(
                dateStr = todayStr,
                durationMinutes = minutes,
                focusSessionType = sessionType
            )
        )
    }

    // Online Classes operations
    suspend fun setClassLive(classId: Int, isLive: Boolean) {
        val list = mutableListOf<OnlineClass>()
        // Simple update
    }

    // Newspaper AI generation helper
    suspend fun generateAndAddArticle(topic: String): GeneratedNewspaperArticle? {
        val article = GeminiService.generateNewspaperArticle(topic)
        if (article != null) {
            val currentList = _articles.value.toMutableList()
            currentList.add(0, article) // Put newly generated article first
            _articles.value = currentList
        }
        return article
    }

    private fun getTodayDateString(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }
}
