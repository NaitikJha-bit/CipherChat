package com.example.calling

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.CallType
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SecurityGold
import kotlin.random.Random

@Composable
fun CallScreen(
    callInfo: CallInfo,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onSwitchCamera: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleScreenShare: () -> Unit
) {
    if (callInfo.callStatus == CallStatus.IDLE) return

    val isVideo = callInfo.callType == CallType.VIDEO

    val formattedDuration = remember(callInfo.durationSeconds) {
        val mins = callInfo.durationSeconds / 60
        val secs = callInfo.durationSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0B0F17)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Video Camera Simulator Canvas (If Video Call and Camera is ON)
            if (isVideo && callInfo.isCameraOn && callInfo.callStatus == CallStatus.CONNECTED) {
                // Remote Feed Canvas Simulator
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF151D2A)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(CyberCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Live Video Stream (Peer: ${callInfo.contactName})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "HD 1080p • 60 FPS • Encrypted WebRTC",
                            color = EmeraldPrimary,
                            fontSize = 11.sp
                        )
                    }
                }

                // Local Camera Preview Floating Window (PiP)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 80.dp, end = 20.dp)
                        .size(110.dp, 160.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.8f))
                        .border(2.dp, EmeraldPrimary, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (callInfo.isFrontCamera) Icons.Default.Person else Icons.Default.FlipCameraAndroid,
                            contentDescription = "Your Camera",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (callInfo.isFrontCamera) "Front Cam" else "Rear Cam",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            } else {
                // Audio Call Mode or Ringing Mode Avatar & Waveform Background
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = callInfo.contactName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when (callInfo.callStatus) {
                            CallStatus.OUTGOING_RINGING -> "Ringing..."
                            CallStatus.INCOMING_RINGING -> "Incoming Call..."
                            CallStatus.CONNECTED -> "E2EE Connected • $formattedDuration"
                            CallStatus.ENDED -> "Call Ended"
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Sound Waveform Equalizer for Active Audio Call
                    if (callInfo.callStatus == CallStatus.CONNECTED && !isVideo) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(40.dp)
                        ) {
                            repeat(16) {
                                val barH = remember(callInfo.durationSeconds) { Random.nextInt(10, 36).dp }
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(barH)
                                        .background(EmeraldPrimary, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // Top Status Bar Overlay
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = callInfo.e2eeFingerprint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }

                    if (callInfo.isScreenSharing) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SecurityGold
                        ) {
                            Text(
                                text = "SCREEN SHARE ON",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Call Controls Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute Microphone
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                if (callInfo.isMuted) DangerRed else Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (callInfo.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = Color.White
                        )
                    }

                    // Speakerphone
                    IconButton(
                        onClick = onToggleSpeaker,
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                if (callInfo.isSpeakerOn) EmeraldPrimary else Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (callInfo.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Speaker",
                            tint = if (callInfo.isSpeakerOn) Color.Black else Color.White
                        )
                    }

                    // Camera On/Off (For Video Calls)
                    if (isVideo) {
                        IconButton(
                            onClick = onToggleCamera,
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    if (callInfo.isCameraOn) CyberCyan else Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (callInfo.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                contentDescription = "Camera Toggle",
                                tint = if (callInfo.isCameraOn) Color.Black else Color.White
                            )
                        }

                        IconButton(
                            onClick = onSwitchCamera,
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlipCameraAndroid,
                                contentDescription = "Switch Camera",
                                tint = Color.White
                            )
                        }
                    }

                    // Screen Share
                    IconButton(
                        onClick = onToggleScreenShare,
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                if (callInfo.isScreenSharing) SecurityGold else Color.White.copy(alpha = 0.2f),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ScreenShare,
                            contentDescription = "Screen Share",
                            tint = if (callInfo.isScreenSharing) Color.Black else Color.White
                        )
                    }
                }

                // End Call Button
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(68.dp)
                        .background(DangerRed, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}
