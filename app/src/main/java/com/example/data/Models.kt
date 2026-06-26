package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "study_notes")
data class StudyNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val tags: String, // Comma-separated tags
    val lastEdited: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "vocab_items")
data class VocabItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val definition: String,
    val contextSentence: String, // The sentence from the news article
    val boxLevel: Int = 1, // Leitner box or spacing level (1 to 5)
    val easeFactor: Float = 2.5f, // SM-2 Ease Factor
    val nextReviewDate: Long = System.currentTimeMillis(), // Next review timestamp
    val lastReviewed: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromAi: Boolean = false,
    val isAiTutor: Boolean = false
) : Serializable

@Entity(tableName = "study_logs")
data class StudyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateStr: String, // e.g., "2026-06-25"
    val durationMinutes: Int,
    val focusSessionType: String // e.g., "Pomodoro", "Standard"
) : Serializable

@Entity(tableName = "online_classes")
data class OnlineClass(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val instructor: String,
    val timeStr: String, // e.g., "Today, 4:00 PM"
    val zoomLink: String,
    val isLive: Boolean = false
) : Serializable
