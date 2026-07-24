package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.example.calling.CallManager
import com.example.data.db.CipherChatDatabase
import com.example.data.repository.ChatRepository
import com.example.ui.CipherChatApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = CipherChatDatabase.getDatabase(applicationContext)
        val repository = ChatRepository(db.chatDao())
        val callManager = CallManager(repository)

        setContent {
            CipherChatApp(
                repository = repository,
                callManager = callManager
            )
        }
    }
}
