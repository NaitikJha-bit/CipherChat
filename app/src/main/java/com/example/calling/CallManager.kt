package com.example.calling

import com.example.data.db.CallDirection
import com.example.data.db.CallType
import com.example.data.repository.ChatRepository
import com.example.security.EncryptionEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CallManager(private val repository: ChatRepository) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    private val _callState = MutableStateFlow(CallInfo())
    val callState: StateFlow<CallInfo> = _callState.asStateFlow()

    fun startCall(contactHandle: String, contactName: String, callType: CallType) {
        val callId = UUID.randomUUID().toString()
        val fp = EncryptionEngine.getShortFingerprint(contactHandle + System.currentTimeMillis())

        _callState.value = CallInfo(
            callId = callId,
            contactHandle = contactHandle,
            contactName = contactName,
            callType = callType,
            callStatus = CallStatus.OUTGOING_RINGING,
            durationSeconds = 0,
            isCameraOn = callType == CallType.VIDEO,
            e2eeFingerprint = "AES-256-GCM | $fp"
        )

        // Simulate contact answering after 3 seconds
        scope.launch {
            delay(3000)
            if (_callState.value.callStatus == CallStatus.OUTGOING_RINGING) {
                acceptCall()
            }
        }
    }

    fun receiveSimulatedIncomingCall(contactHandle: String, contactName: String, callType: CallType) {
        val callId = UUID.randomUUID().toString()
        val fp = EncryptionEngine.getShortFingerprint(contactHandle + System.currentTimeMillis())

        _callState.value = CallInfo(
            callId = callId,
            contactHandle = contactHandle,
            contactName = contactName,
            callType = callType,
            callStatus = CallStatus.INCOMING_RINGING,
            durationSeconds = 0,
            e2eeFingerprint = "AES-256-GCM | $fp"
        )
    }

    fun acceptCall() {
        val current = _callState.value
        _callState.value = current.copy(
            callStatus = CallStatus.CONNECTED,
            durationSeconds = 0
        )

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_callState.value.callStatus == CallStatus.CONNECTED) {
                delay(1000)
                _callState.value = _callState.value.copy(
                    durationSeconds = _callState.value.durationSeconds + 1
                )
            }
        }
    }

    fun endCall() {
        val current = _callState.value
        if (current.callStatus != CallStatus.IDLE) {
            val direction = when (current.callStatus) {
                CallStatus.OUTGOING_RINGING, CallStatus.CONNECTED -> CallDirection.OUTGOING
                CallStatus.INCOMING_RINGING -> CallDirection.MISSED
                else -> CallDirection.INCOMING
            }
            val duration = current.durationSeconds

            scope.launch {
                repository.addCallLog(
                    contactHandle = current.contactHandle,
                    contactName = current.contactName,
                    callType = current.callType,
                    direction = direction,
                    durationSeconds = duration
                )
            }
        }

        timerJob?.cancel()
        _callState.value = CallInfo(callStatus = CallStatus.ENDED)

        scope.launch {
            delay(1200)
            _callState.value = CallInfo(callStatus = CallStatus.IDLE)
        }
    }

    fun toggleMute() {
        val current = _callState.value
        _callState.value = current.copy(isMuted = !current.isMuted)
    }

    fun toggleCamera() {
        val current = _callState.value
        _callState.value = current.copy(isCameraOn = !current.isCameraOn)
    }

    fun switchCamera() {
        val current = _callState.value
        _callState.value = current.copy(isFrontCamera = !current.isFrontCamera)
    }

    fun toggleSpeaker() {
        val current = _callState.value
        _callState.value = current.copy(isSpeakerOn = !current.isSpeakerOn)
    }

    fun toggleScreenShare() {
        val current = _callState.value
        _callState.value = current.copy(isScreenSharing = !current.isScreenSharing)
    }
}
