package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val handle: String, // e.g. "@alex_cyber"
    val displayName: String,
    val bio: String,
    val avatarUrl: String? = null,
    val avatarColorHex: String = "#10B981",
    val publicKeyFingerprint: String = "",
    val isSafetyVerified: Boolean = false,
    val status: String = "Online",
    val isCurrentUser: Boolean = false
)
