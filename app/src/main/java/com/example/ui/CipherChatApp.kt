package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calling.CallInfo
import com.example.calling.CallManager
import com.example.calling.CallScreen
import com.example.calling.CallStatus
import com.example.data.db.CallType
import com.example.data.repository.ChatRepository
import com.example.ui.components.IncomingCallBanner
import com.example.ui.screens.*
import com.example.ui.theme.CipherChatTheme
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.launch

enum class NavigationTab(val label: String, val icon: ImageVector) {
    CHATS("Chats", Icons.Default.Chat),
    CALLS("Calls", Icons.Default.Call),
    CONTACTS("Contacts", Icons.Default.People),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun CipherChatApp(
    repository: ChatRepository,
    callManager: CallManager
) {
    CipherChatTheme(darkTheme = true) {
        var currentTab by remember { mutableStateOf(NavigationTab.CHATS) }
        var activeChatId by remember { mutableStateOf<String?>(null) }

        // Dialog states
        var showNewChatDialog by remember { mutableStateOf(false) }
        var showNewGroupDialog by remember { mutableStateOf(false) }
        var safetyKeysModalData by remember { mutableStateOf<Pair<String, String>?>(null) } // Name to Handle

        // Repository data states
        val chats by repository.allChats.collectAsStateWithLifecycle(initialValue = emptyList())
        val users by repository.allUsers.collectAsStateWithLifecycle(initialValue = emptyList())
        val currentUser by repository.currentUser.collectAsStateWithLifecycle(initialValue = null)
        val callLogs by repository.callLogs.collectAsStateWithLifecycle(initialValue = emptyList())

        // Call state
        val callInfo by callManager.callState.collectAsStateWithLifecycle(initialValue = CallInfo())

        val scope = rememberCoroutineScope()

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    if (activeChatId == null && callInfo.callStatus == CallStatus.IDLE) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            NavigationTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    label = { Text(tab.label) },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = EmeraldPrimary,
                                        selectedTextColor = EmeraldPrimary,
                                        indicatorColor = EmeraldPrimary.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (activeChatId != null) {
                        val activeChat = chats.find { it.chatId == activeChatId }
                        val activeMessages by repository.getMessagesForChat(activeChatId!!)
                            .collectAsStateWithLifecycle(initialValue = emptyList())

                        ChatDetailScreen(
                            chat = activeChat,
                            messages = activeMessages,
                            onBack = { activeChatId = null },
                            onSendMessage = { text ->
                                scope.launch {
                                    repository.sendMessage(activeChatId!!, text)
                                }
                            },
                            onSendMedia = { text, mediaType, url, durationMs, fileName, fileSize, lat, lng, addr ->
                                scope.launch {
                                    repository.sendMessage(
                                        chatId = activeChatId!!,
                                        plainText = text,
                                        mediaType = mediaType,
                                        mediaUrl = url,
                                        mediaDurationMs = durationMs,
                                        fileName = fileName,
                                        fileSizeFormatted = fileSize,
                                        locationLat = lat,
                                        locationLng = lng,
                                        locationAddress = addr
                                    )
                                }
                            },
                            onStartCall = { handle, name, isVideo ->
                                callManager.startCall(
                                    contactHandle = handle,
                                    contactName = name,
                                    callType = if (isVideo) CallType.VIDEO else CallType.AUDIO
                                )
                            },
                            onUpdateDisappearingTimer = { seconds ->
                                scope.launch {
                                    repository.setDisappearingTimer(activeChatId!!, seconds)
                                }
                            },
                            onVerifySafetyKeys = { name, handle ->
                                safetyKeysModalData = name to handle
                            },
                            onClearChat = {
                                scope.launch {
                                    repository.clearChat(activeChatId!!)
                                }
                            }
                        )
                    } else {
                        when (currentTab) {
                            NavigationTab.CHATS -> {
                                ChatsListScreen(
                                    chats = chats,
                                    onChatClick = { activeChatId = it },
                                    onNewDirectChatClick = { showNewChatDialog = true },
                                    onNewGroupChatClick = { showNewGroupDialog = true }
                                )
                            }
                            NavigationTab.CALLS -> {
                                CallsScreen(
                                    callLogs = callLogs,
                                    onStartCall = { handle, name, isVideo ->
                                        callManager.startCall(
                                            contactHandle = handle,
                                            contactName = name,
                                            callType = if (isVideo) CallType.VIDEO else CallType.AUDIO
                                        )
                                    },
                                    onSimulateIncomingCall = {
                                        callManager.receiveSimulatedIncomingCall(
                                            contactHandle = "@sarah_crypto",
                                            contactName = "Sarah Chen",
                                            callType = CallType.VIDEO
                                        )
                                    },
                                    onClearLogs = {
                                        scope.launch { repository.clearCallLogs() }
                                    }
                                )
                            }
                            NavigationTab.CONTACTS -> {
                                ContactsScreen(
                                    users = users,
                                    onSelectContact = { handle ->
                                        scope.launch {
                                            val id = repository.createDirectChat(handle)
                                            if (id.isNotEmpty()) activeChatId = id
                                        }
                                    },
                                    onStartCall = { handle, name, isVideo ->
                                        callManager.startCall(
                                            contactHandle = handle,
                                            contactName = name,
                                            callType = if (isVideo) CallType.VIDEO else CallType.AUDIO
                                        )
                                    },
                                    onVerifySafetyKeys = { name, handle ->
                                        safetyKeysModalData = name to handle
                                    }
                                )
                            }
                            NavigationTab.PROFILE -> {
                                ProfileScreen(
                                    currentUser = currentUser,
                                    onUpdateProfile = { handle, name, bio ->
                                        scope.launch {
                                            repository.updateProfile(handle, name, bio)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Incoming Call Heads-up Notification Banner
            IncomingCallBanner(
                callInfo = callInfo,
                onAccept = { callManager.acceptCall() },
                onDecline = { callManager.endCall() }
            )

            // Full-screen Active Call Screen Overlay (When Connected or Ringing Outgoing)
            if (callInfo.callStatus == CallStatus.CONNECTED || callInfo.callStatus == CallStatus.OUTGOING_RINGING) {
                CallScreen(
                    callInfo = callInfo,
                    onEndCall = { callManager.endCall() },
                    onToggleMute = { callManager.toggleMute() },
                    onToggleCamera = { callManager.toggleCamera() },
                    onSwitchCamera = { callManager.switchCamera() },
                    onToggleSpeaker = { callManager.toggleSpeaker() },
                    onToggleScreenShare = { callManager.toggleScreenShare() }
                )
            }

            // Dialogs
            if (showNewChatDialog) {
                NewChatDialog(
                    users = users,
                    onSelectUser = { handle ->
                        scope.launch {
                            val id = repository.createDirectChat(handle)
                            if (id.isNotEmpty()) activeChatId = id
                        }
                    },
                    onDismiss = { showNewChatDialog = false }
                )
            }

            if (showNewGroupDialog) {
                NewGroupDialog(
                    users = users,
                    onCreateGroup = { title, selected ->
                        scope.launch {
                            val groupId = repository.createGroupChat(title, selected)
                            if (groupId.isNotEmpty()) activeChatId = groupId
                        }
                    },
                    onDismiss = { showNewGroupDialog = false }
                )
            }

            safetyKeysModalData?.let { (name, handle) ->
                val contactUser = users.find { it.handle == handle }
                SafetyKeysDialog(
                    contactName = name,
                    contactHandle = handle,
                    isVerified = contactUser?.isSafetyVerified ?: false,
                    onToggleVerified = { isVerified ->
                        scope.launch {
                            repository.toggleSafetyVerified(handle, isVerified)
                        }
                    },
                    onDismiss = { safetyKeysModalData = null }
                )
            }
        }
    }
}
