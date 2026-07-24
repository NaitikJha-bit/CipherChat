package com.example.data.repository

import com.example.data.db.*
import com.example.security.EncryptionEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(private val chatDao: ChatDao) {

    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    val allChats: Flow<List<ChatEntity>> = chatDao.getAllChats()
    val allUsers: Flow<List<UserEntity>> = chatDao.getAllUsers()
    val currentUser: Flow<UserEntity?> = chatDao.getCurrentUserFlow()
    val callLogs: Flow<List<CallLogEntity>> = chatDao.getAllCallLogs()

    init {
        // Start automatic background cleaner for disappearing messages
        repositoryScope.launch {
            while (true) {
                delay(1000)
                val now = System.currentTimeMillis()
                chatDao.deleteExpiredMessages(now)
            }
        }

        // Prepopulate default contacts & sample encrypted chats
        repositoryScope.launch {
            seedDatabaseIfEmpty()
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        val current = chatDao.getCurrentUserSync()
        if (current == null) {
            val myUser = UserEntity(
                handle = "@cyber_alex",
                displayName = "Alex Vance",
                bio = "🛡️ CipherChat Security Engineer | Key: P256-E2EE",
                avatarColorHex = "#10B981",
                publicKeyFingerprint = EncryptionEngine.generateSafetyNumbers("alex_vance_seed_2026"),
                isSafetyVerified = true,
                status = "Online",
                isCurrentUser = true
            )
            chatDao.insertUser(myUser)

            val sampleUsers = listOf(
                UserEntity(
                    handle = "@sarah_crypto",
                    displayName = "Sarah Chen",
                    bio = "Cryptographer & Distributed Systems Lead",
                    avatarColorHex = "#06B6D4",
                    publicKeyFingerprint = EncryptionEngine.generateSafetyNumbers("sarah_chen_seed"),
                    isSafetyVerified = true,
                    status = "Online"
                ),
                UserEntity(
                    handle = "@marcus_dev",
                    displayName = "Marcus Wright",
                    bio = "Mobile Protocol Architect",
                    avatarColorHex = "#F59E0B",
                    publicKeyFingerprint = EncryptionEngine.generateSafetyNumbers("marcus_wright_seed"),
                    isSafetyVerified = true,
                    status = "Last seen 5m ago"
                ),
                UserEntity(
                    handle = "@elena_sec",
                    displayName = "Elena Rostova",
                    bio = "Zero-Knowledge Proof Researcher",
                    avatarColorHex = "#EC4899",
                    publicKeyFingerprint = EncryptionEngine.generateSafetyNumbers("elena_rostova_seed"),
                    isSafetyVerified = false,
                    status = "Online"
                )
            )
            chatDao.insertUsers(sampleUsers)

            // Direct Chat with Sarah
            val sarahChatId = "chat_direct_sarah"
            val sarahChat = ChatEntity(
                chatId = sarahChatId,
                title = "Sarah Chen",
                chatType = ChatType.DIRECT,
                memberHandles = "@cyber_alex,@sarah_crypto",
                avatarColorHex = "#06B6D4",
                disappearingSeconds = 30, // 30s disappearing messages enabled by default for demo!
                lastMessageText = "Verified safety keys. The AES-256-GCM handshake is established.",
                lastMessageTimestamp = System.currentTimeMillis() - 10000,
                encryptionSeed = sarahChatId
            )
            chatDao.insertChat(sarahChat)

            val encSarah1 = EncryptionEngine.encrypt("Hey Alex! Is end-to-end encryption active on this channel?", sarahChatId)
            val encSarah2 = EncryptionEngine.encrypt("Yes Sarah, zero-knowledge AES-256-GCM is verified!", sarahChatId)
            val encSarah3 = EncryptionEngine.encrypt("Verified safety keys. The AES-256-GCM handshake is established.", sarahChatId)

            chatDao.insertMessages(
                listOf(
                    MessageEntity(
                        messageId = UUID.randomUUID().toString(),
                        chatId = sarahChatId,
                        senderHandle = "@sarah_crypto",
                        senderName = "Sarah Chen",
                        encryptedText = encSarah1.ciphertext,
                        iv = encSarah1.iv,
                        timestamp = System.currentTimeMillis() - 120000,
                        isFromMe = false
                    ),
                    MessageEntity(
                        messageId = UUID.randomUUID().toString(),
                        chatId = sarahChatId,
                        senderHandle = "@cyber_alex",
                        senderName = "Alex Vance",
                        encryptedText = encSarah2.ciphertext,
                        iv = encSarah2.iv,
                        timestamp = System.currentTimeMillis() - 60000,
                        isFromMe = true
                    ),
                    MessageEntity(
                        messageId = UUID.randomUUID().toString(),
                        chatId = sarahChatId,
                        senderHandle = "@sarah_crypto",
                        senderName = "Sarah Chen",
                        encryptedText = encSarah3.ciphertext,
                        iv = encSarah3.iv,
                        timestamp = System.currentTimeMillis() - 10000,
                        isFromMe = false
                    )
                )
            )

            // Group Chat: "Core Security Architecture"
            val groupChatId = "group_sec_team"
            val groupChat = ChatEntity(
                chatId = groupChatId,
                title = "Core Security Architecture",
                chatType = ChatType.GROUP,
                memberHandles = "@cyber_alex,@sarah_crypto,@marcus_dev,@elena_sec",
                avatarColorHex = "#10B981",
                disappearingSeconds = 0,
                lastMessageText = "Group key rotation scheduled for 02:00 UTC.",
                lastMessageTimestamp = System.currentTimeMillis() - 300000,
                encryptionSeed = groupChatId
            )
            chatDao.insertChat(groupChat)

            val encGrp1 = EncryptionEngine.encrypt("Welcome team to the encrypted core channel.", groupChatId)
            val encGrp2 = EncryptionEngine.encrypt("Group key rotation scheduled for 02:00 UTC.", groupChatId)

            chatDao.insertMessages(
                listOf(
                    MessageEntity(
                        messageId = UUID.randomUUID().toString(),
                        chatId = groupChatId,
                        senderHandle = "@cyber_alex",
                        senderName = "Alex Vance",
                        encryptedText = encGrp1.ciphertext,
                        iv = encGrp1.iv,
                        timestamp = System.currentTimeMillis() - 600000,
                        isFromMe = true
                    ),
                    MessageEntity(
                        messageId = UUID.randomUUID().toString(),
                        chatId = groupChatId,
                        senderHandle = "@marcus_dev",
                        senderName = "Marcus Wright",
                        encryptedText = encGrp2.ciphertext,
                        iv = encGrp2.iv,
                        timestamp = System.currentTimeMillis() - 300000,
                        isFromMe = false
                    )
                )
            )

            // Prepopulate Call Logs
            chatDao.insertCallLog(
                CallLogEntity(
                    callId = UUID.randomUUID().toString(),
                    contactHandle = "@sarah_crypto",
                    contactName = "Sarah Chen",
                    callType = CallType.VIDEO,
                    direction = CallDirection.INCOMING,
                    durationSeconds = 245,
                    timestamp = System.currentTimeMillis() - 3600000
                )
            )
            chatDao.insertCallLog(
                CallLogEntity(
                    callId = UUID.randomUUID().toString(),
                    contactHandle = "@marcus_dev",
                    contactName = "Marcus Wright",
                    callType = CallType.AUDIO,
                    direction = CallDirection.OUTGOING,
                    durationSeconds = 112,
                    timestamp = System.currentTimeMillis() - 86400000
                )
            )
        }
    }

    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> {
        return chatDao.getMessagesForChat(chatId)
    }

    fun getChatByIdFlow(chatId: String): Flow<ChatEntity?> {
        return chatDao.getChatByIdFlow(chatId)
    }

    suspend fun sendMessage(
        chatId: String,
        plainText: String,
        mediaType: MediaType = MediaType.NONE,
        mediaUrl: String? = null,
        mediaDurationMs: Long = 0,
        fileName: String? = null,
        fileSizeFormatted: String? = null,
        locationLat: Double = 0.0,
        locationLng: Double = 0.0,
        locationAddress: String? = null
    ) {
        val chat = chatDao.getChatById(chatId) ?: return
        val me = chatDao.getCurrentUserSync() ?: return

        val encryptedPayload = EncryptionEngine.encrypt(plainText, chat.encryptionSeed)

        val expiresAt = if (chat.disappearingSeconds > 0) {
            System.currentTimeMillis() + (chat.disappearingSeconds * 1000L)
        } else {
            0L
        }

        val msg = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            chatId = chatId,
            senderHandle = me.handle,
            senderName = me.displayName,
            encryptedText = encryptedPayload.ciphertext,
            iv = encryptedPayload.iv,
            timestamp = System.currentTimeMillis(),
            mediaType = mediaType,
            mediaUrl = mediaUrl,
            mediaDurationMs = mediaDurationMs,
            fileName = fileName,
            fileSizeFormatted = fileSizeFormatted,
            locationLat = locationLat,
            locationLng = locationLng,
            locationAddress = locationAddress,
            disappearingSeconds = chat.disappearingSeconds,
            expiresAtTimestamp = expiresAt,
            status = MessageStatus.SENT,
            isFromMe = true
        )

        chatDao.insertMessage(msg)
        val snippetText = when (mediaType) {
            MediaType.IMAGE -> "📷 Encrypted Photo"
            MediaType.VOICE_NOTE -> "🎙️ Voice Message (${mediaDurationMs / 1000}s)"
            MediaType.AUDIO -> "🎵 Audio File"
            MediaType.DOCUMENT -> "📄 $fileName"
            MediaType.LOCATION -> "📍 Shared Location"
            MediaType.NONE -> plainText
        }
        chatDao.updateLastMessage(chatId, snippetText, System.currentTimeMillis())

        // Trigger simulated contact response for interactive experience
        triggerSimulatedResponse(chat, plainText, mediaType)
    }

    private fun triggerSimulatedResponse(chat: ChatEntity, userMessage: String, mediaType: MediaType) {
        repositoryScope.launch {
            delay(2000)
            val responseText = when {
                chat.chatType == ChatType.GROUP -> {
                    val responders = listOf(
                        "@sarah_crypto" to "Sarah Chen",
                        "@marcus_dev" to "Marcus Wright",
                        "@elena_sec" to "Elena Rostova"
                    )
                    val chosen = responders.random()
                    val text = when (mediaType) {
                        MediaType.IMAGE -> "Received photo securely. Processing key hash."
                        MediaType.VOICE_NOTE -> "Listening to audio note over encrypted channel."
                        MediaType.LOCATION -> "Location coordinates received and verified."
                        else -> "Acknowledged! Shared secret key verified for: '$userMessage'"
                    }
                    Triple(chosen.first, chosen.second, text)
                }
                else -> {
                    val text = when (mediaType) {
                        MediaType.IMAGE -> "Encrypted image received cleanly!"
                        MediaType.VOICE_NOTE -> "Clear audio note. E2EE channel active."
                        MediaType.LOCATION -> "Got your location pin."
                        else -> "Verified signature. AES-GCM tag validated for: '$userMessage'"
                    }
                    Triple("@sarah_crypto", "Sarah Chen", text)
                }
            }

            val encPayload = EncryptionEngine.encrypt(responseText.third, chat.encryptionSeed)
            val expiresAt = if (chat.disappearingSeconds > 0) {
                System.currentTimeMillis() + (chat.disappearingSeconds * 1000L)
            } else {
                0L
            }

            val replyMsg = MessageEntity(
                messageId = UUID.randomUUID().toString(),
                chatId = chat.chatId,
                senderHandle = responseText.first,
                senderName = responseText.second,
                encryptedText = encPayload.ciphertext,
                iv = encPayload.iv,
                timestamp = System.currentTimeMillis(),
                mediaType = MediaType.NONE,
                disappearingSeconds = chat.disappearingSeconds,
                expiresAtTimestamp = expiresAt,
                status = MessageStatus.READ,
                isFromMe = false
            )

            chatDao.insertMessage(replyMsg)
            chatDao.updateLastMessage(chat.chatId, responseText.third, System.currentTimeMillis())
        }
    }

    suspend fun createDirectChat(contactHandle: String): String {
        val user = chatDao.getUserByHandle(contactHandle) ?: return ""
        val chatId = "chat_direct_${user.handle.removePrefix("@")}"
        
        val existing = chatDao.getChatById(chatId)
        if (existing == null) {
            val newChat = ChatEntity(
                chatId = chatId,
                title = user.displayName,
                chatType = ChatType.DIRECT,
                memberHandles = "@cyber_alex,${user.handle}",
                avatarColorHex = user.avatarColorHex,
                disappearingSeconds = 0,
                lastMessageText = "Direct E2EE channel created.",
                lastMessageTimestamp = System.currentTimeMillis(),
                encryptionSeed = chatId
            )
            chatDao.insertChat(newChat)
        }
        return chatId
    }

    suspend fun createGroupChat(title: String, selectedHandles: List<String>): String {
        val groupId = "group_${UUID.randomUUID().toString().take(8)}"
        val me = chatDao.getCurrentUserSync()?.handle ?: "@cyber_alex"
        val allMembers = (selectedHandles + me).distinct().joinToString(",")

        val newGroup = ChatEntity(
            chatId = groupId,
            title = title,
            chatType = ChatType.GROUP,
            memberHandles = allMembers,
            avatarColorHex = "#10B981",
            disappearingSeconds = 0,
            lastMessageText = "Group chat created.",
            lastMessageTimestamp = System.currentTimeMillis(),
            encryptionSeed = groupId
        )
        chatDao.insertChat(newGroup)

        val encMsg = EncryptionEngine.encrypt("Group '$title' created with end-to-end encryption.", groupId)
        val welcomeMsg = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            chatId = groupId,
            senderHandle = me,
            senderName = "System",
            encryptedText = encMsg.ciphertext,
            iv = encMsg.iv,
            timestamp = System.currentTimeMillis(),
            isFromMe = true
        )
        chatDao.insertMessage(welcomeMsg)

        return groupId
    }

    suspend fun setDisappearingTimer(chatId: String, seconds: Int) {
        chatDao.updateDisappearingTimer(chatId, seconds)
    }

    suspend fun deleteMessage(messageId: String) {
        chatDao.deleteMessageById(messageId)
    }

    suspend fun clearChat(chatId: String) {
        chatDao.clearMessagesForChat(chatId)
        chatDao.updateLastMessage(chatId, "Chat history cleared.", System.currentTimeMillis())
    }

    suspend fun updateProfile(username: String, displayName: String, bio: String) {
        val current = chatDao.getCurrentUserSync() ?: return
        chatDao.updateUserProfile(current.handle, displayName, bio, null)
    }

    suspend fun toggleSafetyVerified(handle: String, isVerified: Boolean) {
        chatDao.updateUserSafetyVerified(handle, isVerified)
    }

    suspend fun addCallLog(contactHandle: String, contactName: String, callType: CallType, direction: CallDirection, durationSeconds: Int) {
        val log = CallLogEntity(
            callId = UUID.randomUUID().toString(),
            contactHandle = contactHandle,
            contactName = contactName,
            callType = callType,
            direction = direction,
            durationSeconds = durationSeconds,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertCallLog(log)
    }

    suspend fun clearCallLogs() {
        chatDao.clearCallLogs()
    }
}
