package com.example.calling

import com.example.data.db.CallType

enum class CallStatus {
    IDLE,
    OUTGOING_RINGING,
    INCOMING_RINGING,
    CONNECTED,
    ENDED
}

data class CallInfo(
    val callId: String = "",
    val contactHandle: String = "",
    val contactName: String = "",
    val contactAvatarUrl: String? = null,
    val callType: CallType = CallType.AUDIO,
    val callStatus: CallStatus = CallStatus.IDLE,
    val durationSeconds: Int = 0,
    val isCameraOn: Boolean = true,
    val isFrontCamera: Boolean = true,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isScreenSharing: Boolean = false,
    val e2eeFingerprint: String = "E2EE-AES-256-GCM"
)
