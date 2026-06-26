package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyNoteDao {
    @Query("SELECT * FROM study_notes ORDER BY lastEdited DESC")
    fun getAllNotes(): Flow<List<StudyNote>>

    @Query("SELECT * FROM study_notes WHERE id = :id")
    suspend fun getNoteById(id: Int): StudyNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNote): Long

    @Update
    suspend fun updateNote(note: StudyNote)

    @Delete
    suspend fun deleteNote(note: StudyNote)
}

@Dao
interface VocabItemDao {
    @Query("SELECT * FROM vocab_items ORDER BY nextReviewDate ASC")
    fun getAllVocab(): Flow<List<VocabItem>>

    @Query("SELECT * FROM vocab_items WHERE word = :word LIMIT 1")
    suspend fun getVocabByWord(word: String): VocabItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocab(vocab: VocabItem): Long

    @Update
    suspend fun updateVocab(vocab: VocabItem)

    @Delete
    suspend fun deleteVocab(vocab: VocabItem)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}

@Dao
interface StudyLogDao {
    @Query("SELECT * FROM study_logs ORDER BY dateStr DESC")
    fun getAllLogs(): Flow<List<StudyLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StudyLog): Long
}

@Dao
interface OnlineClassDao {
    @Query("SELECT * FROM online_classes ORDER BY id ASC")
    fun getAllClasses(): Flow<List<OnlineClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(onlineClass: OnlineClass): Long

    @Update
    suspend fun updateClass(onlineClass: OnlineClass)
}
