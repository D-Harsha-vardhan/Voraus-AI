package com.example.edujourneygermany.advisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean, val isLoading: Boolean = false)

class AiAdvisorViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage("Hi! I'm your Educaro AI Advisor. How can I help you with your journey to Germany today?", isUser = false)
    ))
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val apiKey = "sk_KFTF1wvMEfshej4N9PdT5Saw62s2Dt80" // Note: In production, store this securely

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        // Add user message
        _messages.update { it + ChatMessage(userText, isUser = true) }
        
        // Add loading message
        val loadingIndex = _messages.value.size
        _messages.update { it + ChatMessage("Thinking...", isUser = false, isLoading = true) }

        viewModelScope.launch {
            try {
                // Call DronaHQ webhook
                val response = RetrofitClient.dronaHqApi.sendMessage(
                    authHeader = "Bearer $apiKey",
                    request = WebhookRequest(message = userText)
                )

                // Parse response - assuming the webhook returns a JSON object with a "message" or "response" field,
                // or just convert the whole thing to a string representation if it's unknown.
                val aiResponseText = if (response.isJsonObject) {
                    val obj = response.asJsonObject
                    if (obj.has("message")) obj.get("message").asString
                    else if (obj.has("response")) obj.get("response").asString
                    else if (obj.has("text")) obj.get("text").asString
                    else response.toString()
                } else if (response.isJsonPrimitive) {
                    response.asString
                } else {
                    response.toString()
                }

                // Replace loading message with actual response
                _messages.update { list ->
                    list.mapIndexed { index, chatMessage ->
                        if (index == loadingIndex) {
                            ChatMessage(aiResponseText, isUser = false)
                        } else chatMessage
                    }
                }
            } catch (e: Exception) {
                // Replace loading message with error
                _messages.update { list ->
                    list.mapIndexed { index, chatMessage ->
                        if (index == loadingIndex) {
                            ChatMessage("Sorry, I couldn't reach the server right now. Error: ${e.localizedMessage}", isUser = false)
                        } else chatMessage
                    }
                }
            }
        }
    }
}
