package com.example.edujourneygermany.advisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean, val isLoading: Boolean = false, val quotedQuestion: String? = null)

data class FlowQuestion(
    val key: String,
    val question: String,
    val options: List<String>
)

class AiAdvisorViewModel : ViewModel() {

    private val flowQuestions = listOf(
        FlowQuestion("level", "Hello, Start your journey to Study in Germany", listOf("Bachelors", "Masters")),
        FlowQuestion("mode", "Preferred Mode of Study", listOf("English", "German")),
        FlowQuestion("course", "Which course are you interested in?", listOf("IT / Computer Science", "Business / Management", "Medical / Healthcare", "Engineering")),
        FlowQuestion("cgpa", "To provide personalized study plan, please share the CGPA of highest qualification", listOf("Below 6.5", "6.5-7.5", "7.5-8.5", "8.5+")),
        FlowQuestion("city", "Please select your nearest city", listOf("Bangalore", "Hyderabad", "Chennai", "Mumbai", "Delhi", "Other"))
    )

    private val afterFlowMessage = "Got you! Germany study plan ready for you. Ask FREE UNLIMITED queries."

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _currentOptions = MutableStateFlow<List<String>>(emptyList())
    val currentOptions: StateFlow<List<String>> = _currentOptions.asStateFlow()

    private val _isFlowComplete = MutableStateFlow(false)
    val isFlowComplete: StateFlow<Boolean> = _isFlowComplete.asStateFlow()

    private var currentFlowIndex = 0
    private val userAnswers = mutableMapOf<String, String>()
    private var isFirstDronaHqMessage = true

    private val apiKey = "187e7a22-6051-484d-a4d3-749c41d13c90" // Note: In production, store this securely

    init {
        showNextFlowQuestion()
    }

    private fun showNextFlowQuestion() {
        viewModelScope.launch {
            // Add loading message
            val loadingIndex = _messages.value.size
            _messages.update { it + ChatMessage("Thinking...", isUser = false, isLoading = true) }
            
            kotlinx.coroutines.delay(1000) // Simulate typing delay

            if (currentFlowIndex < flowQuestions.size) {
                val q = flowQuestions[currentFlowIndex]
                _messages.update { list ->
                    list.mapIndexed { index, chatMessage ->
                        if (index == loadingIndex) {
                            ChatMessage(q.question, isUser = false)
                        } else chatMessage
                    }
                }
                _currentOptions.value = q.options
            } else {
                _isFlowComplete.value = true
                _currentOptions.value = emptyList()
                _messages.update { list ->
                    list.mapIndexed { index, chatMessage ->
                        if (index == loadingIndex) {
                            ChatMessage(afterFlowMessage, isUser = false)
                        } else chatMessage
                    }
                }
            }
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        if (!_isFlowComplete.value) {
            // We are still in the flow
            val q = flowQuestions[currentFlowIndex]
            _messages.update { it + ChatMessage(userText, isUser = true, quotedQuestion = q.question) }
            
            userAnswers[q.key] = userText
            currentFlowIndex++
            _currentOptions.value = emptyList() // clear options briefly
            showNextFlowQuestion()
            return
        }

        // Add user message for normal chat
        _messages.update { it + ChatMessage(userText, isUser = true) }

        // Add loading message
        val loadingIndex = _messages.value.size
        _messages.update { it + ChatMessage("Thinking...", isUser = false, isLoading = true) }

        viewModelScope.launch {
            try {
                // Prepend context to the first message sent to DronaHQ
                val payloadMessage = if (isFirstDronaHqMessage && userAnswers.isNotEmpty()) {
                    isFirstDronaHqMessage = false
                    val contextStr = userAnswers.entries.joinToString(", ") { "${it.key}: ${it.value}" }
                    "Context: [User Profile: $contextStr]\n\nUser Question: $userText"
                } else {
                    userText
                }

                // Call DronaHQ webhook
                val response = RetrofitClient.dronaHqApi.sendMessage(
                    authHeader = "Bearer $apiKey",
                    apiKeyHeader = apiKey,
                    request = WebhookRequest(message = payloadMessage)
                )

                // Parse response
                val aiResponseText = if (response.isJsonObject) {
                    val obj = response.asJsonObject
                    if (obj.has("response") && obj.get("response").isJsonObject && obj.getAsJsonObject("response").has("reply")) {
                        obj.getAsJsonObject("response").get("reply").asString
                    } else if (obj.has("message")) {
                        obj.get("message").asString
                    } else if (obj.has("text")) {
                        obj.get("text").asString
                    } else {
                        response.toString()
                    }
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
            } catch (e: retrofit2.HttpException) {
                // Replace loading message with specific HTTP error
                val errorMsg = if (e.code() == 401) {
                    "I'm sorry, but my API key appears to be invalid or expired. Please update it in the dashboard."
                } else {
                    "Sorry, I couldn't reach the server right now. Error: HTTP ${e.code()}"
                }
                _messages.update { list ->
                    list.mapIndexed { index, chatMessage ->
                        if (index == loadingIndex) {
                            ChatMessage(errorMsg, isUser = false)
                        } else chatMessage
                    }
                }
            } catch (e: Exception) {
                // Replace loading message with general error
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
