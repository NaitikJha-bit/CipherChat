package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MediaType {
    NONE,
    IMAGE,
    AUDIO,
    VOICE_NOTE,
    DOCUMENT,
    LOCATION
}

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ
}

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val chatId: String,
    val senderHandle: String,
    val senderName: String,
    val encryptedText: String,
    val iv: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaType: MediaType = MediaType.NONE,
    val mediaUrl: String? = null,
    val mediaDurationMs: Long = 0,
    val fileName: String? = null,
    val fileSizeFormatted: String? = null,
    val locationLat: Double = 0.0,
    val locationLng: Double = 0.0,
    val locationAddress: String? = null,
    val disappearingSeconds: Int = 0,
    val expiresAtTimestamp: Long = 0, // 0 = never expires, >0 = timestamp when auto-deleted
    val status: MessageStatus = MessageStatus.READ,
    val isFromMe: Boolean = true
)
