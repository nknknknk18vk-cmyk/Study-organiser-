package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudyNote::class,
        VocabItem::class,
        ChatMessage::class,
        StudyLog::class,
        OnlineClass::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyNoteDao(): StudyNoteDao
    abstract fun vocabItemDao(): VocabItemDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun studyLogDao(): StudyLogDao
    abstract fun onlineClassDao(): OnlineClassDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "serene_study_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            // Populate classes
            val classDao = database.onlineClassDao()
            classDao.insertClass(
                OnlineClass(
                    title = "Quantum Physics: Wave-Particle Duality",
                    instructor = "Dr. Elizabeth Mercer",
                    timeStr = "Live Now (04:00 PM - 05:30 PM)",
                    zoomLink = "https://zoom.us/mock-quantum-class",
                    isLive = true
                )
            )
            classDao.insertClass(
                OnlineClass(
                    title = "Modern English Literature & Journalism",
                    instructor = "Prof. James Cordon",
                    timeStr = "Tomorrow, 10:30 AM",
                    zoomLink = "https://zoom.us/mock-literature-class",
                    isLive = false
                )
            )
            classDao.insertClass(
                OnlineClass(
                    title = "An Introduction to Machine Learning",
                    instructor = "Prof. Alan Turing",
                    timeStr = "Friday, 02:00 PM",
                    zoomLink = "https://zoom.us/mock-ml-class",
                    isLive = false
                )
            )

            // Populate some vocab items
            val vocabDao = database.vocabItemDao()
            vocabDao.insertVocab(
                VocabItem(
                    word = "Aesthetic",
                    definition = "Concerned with beauty or the appreciation of beauty.",
                    contextSentence = "The edtech app uses a bento grid to create a balanced visual aesthetic.",
                    boxLevel = 1
                )
            )
            vocabDao.insertVocab(
                VocabItem(
                    word = "Cognitive",
                    definition = "Relating to the mental action or process of acquiring knowledge and understanding through thought, experience, and the senses.",
                    contextSentence = "Spaced repetition algorithms significantly optimize our cognitive retention of vocabulary.",
                    boxLevel = 2
                )
            )
            vocabDao.insertVocab(
                VocabItem(
                    word = "Resilient",
                    definition = "Able to withstand or recover quickly from difficult conditions.",
                    contextSentence = "Being a resilient student means learning from failures and continuing to strive.",
                    boxLevel = 3
                )
            )

            // Populate some sample notes
            val notesDao = database.studyNoteDao()
            notesDao.insertNote(
                StudyNote(
                    title = "Wave-Particle Duality Notes",
                    content = "Key points discussed by Dr. Mercer:\n- Light behaves both as a wave and a stream of particles.\n- De Broglie equation: lambda = h / p.\n- Double-slit experiment shows wave-like interference even when single electrons are fired.",
                    tags = "Physics,Quantum,Lecture 1"
                )
            )
            notesDao.insertNote(
                StudyNote(
                    title = "Machine Learning Core Concepts",
                    content = "Supervised Learning:\n- Labeled training data\n- Regression vs. Classification\n\nUnsupervised Learning:\n- Unlabeled data\n- Clustering (K-Means) and Dimensionality Reduction (PCA)",
                    tags = "ML,AI,Intro"
                )
            )

            // Populate initial chat
            val chatDao = database.chatMessageDao()
            chatDao.insertMessage(
                ChatMessage(
                    sender = "System",
                    text = "Welcome to the Smart Study Collaborative Chat Room! You can collaborate with your peers, or chat with the AI Tutor by typing @AI_Tutor in your message.",
                    isFromAi = false,
                    isAiTutor = false
                )
            )
            chatDao.insertMessage(
                ChatMessage(
                    sender = "Chloe (Study Partner)",
                    text = "Hey guys, did anyone manage to finish the third question on the physics assignment?",
                    isFromAi = false,
                    isAiTutor = false
                )
            )
        }
    }
}
