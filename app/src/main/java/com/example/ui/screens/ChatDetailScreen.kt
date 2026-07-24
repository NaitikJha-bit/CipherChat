package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.db.ChatEntity
import com.example.data.db.MediaType
import com.example.data.db.MessageEntity
import com.example.security.EncryptionEngine
import com.example.ui.components.AttachmentPickerSheet
import com.example.ui.components.RawCiphertextDialog
import com.example.ui.components.VoiceNoteRecorder
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    chat: ChatEntity?,
    messages: List<MessageEntity>,
    onBack: () -> Unit,
    onSendMessage: (text: String) -> Unit,
    onSendMedia: (text: String, mediaType: MediaType, mediaUrl: String?, mediaDurationMs: Long, fileName: String?, fileSize: String?, lat: Double, lng: Double, address: String?) -> Unit,
    onStartCall: (contactHandle: String, contactName: String, isVideo: Boolean) -> Unit,
    onUpdateDisappearingTimer: (seconds: Int) -> Unit,
    onVerifySafetyKeys: (contactName: String, contactHandle: String) -> Unit,
    onClearChat: () -> Unit
) {
    if (chat == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EmeraldPrimary)
        }
        return
    }

    var textInput by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showVoiceRecorder by remember { mutableStateOf(false) }

    var selectedMessageForInspector by remember { mutableStateOf<MessageEntity?>(null) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (chat.chatType == com.example.data.db.ChatType.GROUP) Icons.Default.Groups else Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Black
                            )
                        }

                        Column {
                            Text(
                                text = chat.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "E2EE Encrypted",
                                    fontSize = 11.sp,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val handle = chat.memberHandles.split(",").firstOrNull { it != "@cyber_alex" } ?: chat.title
                    
                    // Audio Call
                    IconButton(onClick = { onStartCall(handle, chat.title, false) }) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Audio Call", tint = EmeraldPrimary)
                    }

                    // Video Call
                    IconButton(onClick = { onStartCall(handle, chat.title, true) }) {
                        Icon(imageVector = Icons.Default.Videocam, contentDescription = "Video Call", tint = EmeraldPrimary)
                    }

                    // More options
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Disappearing Messages") },
                            leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, tint = SecurityGold) },
                            onClick = {
                                showMenu = false
                                showTimerDialog = true
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Verify Safety Keys") },
                            leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldPrimary) },
                            onClick = {
                                showMenu = false
                                onVerifySafetyKeys(chat.title, handle)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Clear Chat History") },
                            leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = DangerRed) },
                            onClick = {
                                showMenu = false
                                onClearChat()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Disappearing Messages Banner
            if (chat.disappearingSeconds > 0) {
                val timerLabel = when (chat.disappearingSeconds) {
                    5 -> "5 seconds"
                    30 -> "30 seconds"
                    60 -> "1 minute"
                    3600 -> "1 hour"
                    else -> "24 hours"
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SecurityGold.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = SecurityGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Disappearing Messages ON: Auto-expires after $timerLabel",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecurityGold
                        )
                    }
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    E2EEHeaderCard(channelSeed = chat.encryptionSeed)
                }

                items(messages, key = { it.messageId }) { msg ->
                    MessageBubble(
                        message = msg,
                        channelSeed = chat.encryptionSeed,
                        onInspectCiphertext = { selectedMessageForInspector = msg }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Input Bar / Voice Recorder
            if (showVoiceRecorder) {
                VoiceNoteRecorder(
                    onCancel = { showVoiceRecorder = false },
                    onSendVoiceNote = { durationMs ->
                        showVoiceRecorder = false
                        onSendMedia("Voice Message", MediaType.VOICE_NOTE, null, durationMs, null, null, 0.0, 0.0, null)
                    }
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = { showAttachmentSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Attachments",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Message (End-to-End Encrypted)...") },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f),
                            maxLines = 4
                        )

                        if (textInput.trim().isEmpty()) {
                            IconButton(onClick = { showVoiceRecorder = true }) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Note",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    val text = textInput.trim()
                                    if (text.isNotEmpty()) {
                                        onSendMessage(text)
                                        textInput = ""
                                    }
                                },
                                modifier = Modifier.background(EmeraldPrimary, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Attachment Sheet
    if (showAttachmentSheet) {
        AttachmentPickerSheet(
            onDismiss = { showAttachmentSheet = false },
            onSelectImage = { url ->
                onSendMedia("Encrypted Image", MediaType.IMAGE, url, 0, null, null, 0.0, 0.0, null)
            },
            onSelectVoiceNote = { showVoiceRecorder = true },
            onSelectDocument = { fileName, fileSize ->
                onSendMedia("Encrypted Document", MediaType.DOCUMENT, null, 0, fileName, fileSize, 0.0, 0.0, null)
            },
            onSelectLocation = { lat, lng, addr ->
                onSendMedia("Shared Location", MediaType.LOCATION, null, 0, null, null, lat, lng, addr)
            }
        )
    }

    // Timer Selector Dialog
    if (showTimerDialog) {
        DisappearingTimerDialog(
            currentSeconds = chat.disappearingSeconds,
            onSelectSeconds = { sec ->
                onUpdateDisappearingTimer(sec)
                showTimerDialog = false
            },
            onDismiss = { showTimerDialog = false }
        )
    }

    // Raw Ciphertext Inspector Modal
    selectedMessageForInspector?.let { msg ->
        RawCiphertextDialog(
            ciphertext = msg.encryptedText,
            iv = msg.iv,
            channelSeed = chat.encryptionSeed,
            onDismiss = { selectedMessageForInspector = null }
        )
    }
}

@Composable
fun E2EEHeaderCard(channelSeed: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = EmeraldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "End-to-End Encrypted Channel",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = EmeraldPrimary
            )
            Text(
                text = "Messages and media are encrypted using zero-knowledge AES-256-GCM. Tap any message to inspect raw ciphertext.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    channelSeed: String,
    onInspectCiphertext: () -> Unit
) {
    val decryptedText = remember(message.encryptedText, message.iv) {
        EncryptionEngine.decrypt(message.encryptedText, message.iv, channelSeed)
    }

    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    // Live countdown timer calculation for disappearing messages
    var remainingSeconds by remember { mutableStateOf(0L) }
    if (message.expiresAtTimestamp > 0) {
        LaunchedEffect(message.expiresAtTimestamp) {
            while (true) {
                val now = System.currentTimeMillis()
                val diff = (message.expiresAtTimestamp - now) / 1000
                remainingSeconds = diff.coerceAtLeast(0)
                if (diff <= 0) break
                delay(1000)
            }
        }
    }

    val alignment = if (message.isFromMe) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isFromMe) OutgoingBubble else IncomingBubble
    val textColor = TextPrimary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInspectCiphertext() },
        horizontalAlignment = alignment
    ) {
        if (!message.isFromMe) {
            Text(
                text = message.senderName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldPrimary,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromMe) 16.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 16.dp
            ),
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 290.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {

                // Media Attachments
                when (message.mediaType) {
                    MediaType.IMAGE -> {
                        message.mediaUrl?.let { url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Encrypted Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                    MediaType.VOICE_NOTE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Voice Note",
                                tint = EmeraldPrimary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🎙️ Voice Message",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${message.mediaDurationMs / 1000}s • Encrypted Audio",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    MediaType.DOCUMENT -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = "Document",
                                tint = CyberCyan
                            )
                            Column {
                                Text(
                                    text = message.fileName ?: "Document.pdf",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${message.fileSizeFormatted ?: "1.2 MB"} • Encrypted File",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    MediaType.LOCATION -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = SecurityGold
                            )
                            Column {
                                Text(
                                    text = "📍 Shared Location Pin",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = message.locationAddress ?: "San Francisco, CA",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    else -> {}
                }

                // Plaintext content
                Text(
                    text = decryptedText,
                    color = textColor,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Footer with time, encrypted icon, and disappearing countdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.expiresAtTimestamp > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Expiring",
                                tint = SecurityGold,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "${remainingSeconds}s",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecurityGold
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = EmeraldPrimary,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(10.dp)
                    )

                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun DisappearingTimerDialog(
    currentSeconds: Int,
    onSelectSeconds: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        0 to "Off",
        5 to "5 Seconds (Testing)",
        30 to "30 Seconds",
        60 to "1 Minute",
        3600 to "1 Hour",
        86400 to "24 Hours"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = SecurityGold
                )
                Text(
                    text = "Disappearing Messages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "When enabled, new messages sent in this chat will auto-delete for everyone after the selected timer expires.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                options.forEach { (sec, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectSeconds(sec) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontWeight = if (currentSeconds == sec) FontWeight.Bold else FontWeight.Normal,
                            color = if (currentSeconds == sec) SecurityGold else MaterialTheme.colorScheme.onSurface
                        )
                        RadioButton(
                            selected = currentSeconds == sec,
                            onClick = { onSelectSeconds(sec) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
