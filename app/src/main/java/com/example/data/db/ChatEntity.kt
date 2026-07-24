package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ChatType {
    DIRECT,
    GROUP
}

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val chatId: String,
    val title: String,
    val chatType: ChatType,
    val memberHandles: String, // Comma separated handles e.g. "@alex_cyber,@sarah_crypto"
    val avatarUrl: String? = null,
    val avatarColorHex: String = "#06B6D4",
    val disappearingSeconds: Int = 0, // 0 = Disabled, 5, 30, 60, 3600, 86400
    val lastMessageText: String = "",
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val encryptionSeed: String = chatId
)
