package com.weijia.chatbuddy.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weijia.chatbuddy.models.ChatMessageDao
import com.weijia.chatbuddy.models.ChatMessageEntity
import com.weijia.chatbuddy.models.ChatRequest
import com.weijia.chatbuddy.models.GrokService
import com.weijia.chatbuddy.models.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


data class ChatMessage(val text: String, val isUser: Boolean)

class ChatViewModel( private val chatMessageDao: ChatMessageDao) : ViewModel() {

    private val _chatState = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatState: StateFlow<List<ChatMessage>> = _chatState

    private var lastRequestTime = 0L
    private val minRequestInterval = 1000L // 1 second

    init {
        // Load persisted messages on initialization
        viewModelScope.launch {
            chatMessageDao.getAllMessages().collect { entities ->
                _chatState.value = entities.map { ChatMessage(it.text, it.isUser) }
            }
        }
    }

    private val api = GrokService.api

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRequestTime < minRequestInterval) {
            _chatState.value += ChatMessage("Please wait a moment before sending another message", false)
            return
        }

        lastRequestTime = currentTime

        // Save user message to Room
        viewModelScope.launch {
            chatMessageDao.insertMessage(
                ChatMessageEntity(text = userMessage, isUser = true, timestamp = currentTime)
            )
        }

        viewModelScope.launch {
            try {
                val request = ChatRequest(messages = listOf(Message("user", userMessage)))
                val response = api.getChatResponse(request)
                val botMessage = response.choices.firstOrNull()?.message?.content ?: "No response from Grok"
                // Save bot message to Room
                chatMessageDao.insertMessage(
                    ChatMessageEntity(text = botMessage, isUser = false, timestamp = System.currentTimeMillis())
                )
                _chatState.value += ChatMessage(botMessage, false)
            } catch (e: Exception) {
                val errorMessage = "Error: ${e.message}"
                chatMessageDao.insertMessage(
                    ChatMessageEntity(text = errorMessage, isUser = false, timestamp = System.currentTimeMillis())
                )
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatMessageDao.clearMessages()
        }
    }
}