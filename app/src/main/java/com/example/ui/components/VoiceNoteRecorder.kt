package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun VoiceNoteRecorder(
    onCancel: () -> Unit,
    onSendVoiceNote: (durationMs: Long) -> Unit
) {
    var recordTimeSeconds by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(true) }

    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            recordTimeSeconds++
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Cancel button
            IconButton(
                onClick = {
                    isRecording = false
                    onCancel()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Cancel Recording",
                    tint = DangerRed
                )
            }

            // Recording indicator & amplitude waves
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(DangerRed.copy(alpha = pulseAlpha))
                )

                Text(
                    text = String.format("%02d:%02d", recordTimeSeconds / 60, recordTimeSeconds % 60),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Animated waveform bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(24.dp)
                ) {
                    repeat(12) { index ->
                        val barHeight = remember(recordTimeSeconds) { Random.nextInt(8, 24).dp }
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(barHeight)
                                .background(
                                    color = if (index % 2 == 0) EmeraldPrimary else MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }

            // Send voice note
            IconButton(
                onClick = {
                    isRecording = false
                    val duration = (recordTimeSeconds.coerceAtLeast(1) * 1000).toLong()
                    onSendVoiceNote(duration)
                },
                modifier = Modifier
                    .background(EmeraldPrimary, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Voice Note",
                    tint = Color.Black
                )
            }
        }
    }
}
