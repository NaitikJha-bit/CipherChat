package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CallType {
    AUDIO,
    VIDEO
}

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey val callId: String,
    val contactHandle: String,
    val contactName: String,
    val contactAvatarUrl: String? = null,
    val callType: CallType,
    val direction: CallDirection,
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val isE2EEVerified: Boolean = true
)
