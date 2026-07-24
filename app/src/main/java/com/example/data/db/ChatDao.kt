package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // --- USERS ---
    @Query("SELECT * FROM users ORDER BY isCurrentUser DESC, displayName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE handle = :handle LIMIT 1")
    suspend fun getUserByHandle(handle: String): UserEntity?

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUserSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Query("UPDATE users SET displayName = :displayName, bio = :bio, avatarUrl = :avatarUrl WHERE handle = :handle")
    suspend fun updateUserProfile(handle: String, displayName: String, bio: String, avatarUrl: String?)

    @Query("UPDATE users SET isSafetyVerified = :isVerified WHERE handle = :handle")
    suspend fun updateUserSafetyVerified(handle: String, isVerified: Boolean)

    // --- CHATS ---
    @Query("SELECT * FROM chats ORDER BY lastMessageTimestamp DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId LIMIT 1")
    suspend fun getChatById(chatId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE chatId = :chatId LIMIT 1")
    fun getChatByIdFlow(chatId: String): Flow<ChatEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChats(chats: List<ChatEntity>)

    @Query("UPDATE chats SET disappearingSeconds = :seconds WHERE chatId = :chatId")
    suspend fun updateDisappearingTimer(chatId: String, seconds: Int)

    @Query("UPDATE chats SET lastMessageText = :lastText, lastMessageTimestamp = :timestamp WHERE chatId = :chatId")
    suspend fun updateLastMessage(chatId: String, lastText: String, timestamp: Long)

    @Query("UPDATE chats SET unreadCount = 0 WHERE chatId = :chatId")
    suspend fun clearUnreadCount(chatId: String)

    @Query("DELETE FROM chats WHERE chatId = :chatId")
    suspend fun deleteChatById(chatId: String)

    // --- MESSAGES ---
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun clearMessagesForChat(chatId: String)

    @Query("DELETE FROM messages WHERE expiresAtTimestamp > 0 AND expiresAtTimestamp <= :currentTimestamp")
    suspend fun deleteExpiredMessages(currentTimestamp: Long): Int

    // --- CALL LOGS ---
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogEntity)

    @Query("DELETE FROM call_logs WHERE callId = :callId")
    suspend fun deleteCallLogById(callId: String)

    @Query("DELETE FROM call_logs")
    suspend fun clearCallLogs()
}
