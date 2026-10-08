package com.example.edujourneygermany.advisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParsedUniversity(
    val name: String,
    val program: String,
    val matchScore: String,
    val type: String,
    val language: String
)

data class ChatMessage(
    val text: String, 
    val isUser: Boolean, 
    val isLoading: Boolean = false, 
    val quotedQuestion: String? = null,
    val universities: List<ParsedUniversity>? = null
)

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

    private val apiKey = com.example.edujourneygermany.BuildConfig.NVIDIA_API_KEY

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
                // Prepend context to the first message sent
                val payloadMessage = if (isFirstDronaHqMessage && userAnswers.isNotEmpty()) {
                    isFirstDronaHqMessage = false
                    val contextStr = userAnswers.entries.joinToString(", ") { "${it.key}: ${it.value}" }
                    "Context: [User Profile: $contextStr]\n\nUser Question: $userText"
                } else {
                    userText
                }

                val systemPrompt = """
                    You are Educaro AI, an expert study abroad advisor for Germany. 
                    Use the context provided about the user to give accurate, personalized university and program recommendations based on real universities in Germany.
                    
                    CRITICAL: Do NOT just repeat the examples below. You MUST search your knowledge base to find the actual best-matching universities and programs for the user's specific profile (course, GPA, level, language).
                    
                    If the user asks for university recommendations or which university is best for their profile, you MUST output a special block formatted exactly like this (use exactly these tags, no markdown, no bullet points, no headers):
                    
                    [UNIVERSITY_RECOMMENDATIONS]
                    TUM | M.Sc. Computer Science | 92 | Public | English-taught
                    KIT | M.Sc. Artificial Intelligence | 88 | Public | English-taught
                    RWTH Aachen | M.Sc. Data Science | 84 | Public | English-taught
                    [/UNIVERSITY_RECOMMENDATIONS]
                    
                    Keep your text outside this block encouraging, concise, and helpful. Do not add any table headers inside the block.
                    
                    If the user asks a question entirely unrelated to studying in Germany, or if they ask highly specific questions you are unsure about, you MUST respond EXACTLY with the single word: UNKNOWN_QUERY
                """.trimIndent()
                
                val chatMessages = mutableListOf<NvidiaMessage>()
                chatMessages.add(NvidiaMessage("system", systemPrompt))
                
                _messages.value.filter { it.text != "Thinking..." }.forEach { msg ->
                    val role = if (msg.isUser) "user" else "assistant"
                    chatMessages.add(NvidiaMessage(role, msg.text))
                }
                
                if (payloadMessage != userText) {
                    chatMessages[chatMessages.lastIndex] = NvidiaMessage("user", payloadMessage)
                }

                val request = NvidiaRequest(
                    model = "meta/llama-3.2-11b-vision-instruct",
                    messages = chatMessages
                )

                // Call Nvidia API
                val response = RetrofitClient.nvidiaApi.sendMessage(
                    authHeader = "Bearer $apiKey",
                    request = request
                )

                // Parse Nvidia response (OpenAI format)
                var aiResponseText = ""
                if (response.isJsonObject) {
                    val obj = response.asJsonObject
                    if (obj.has("choices") && obj.getAsJsonArray("choices").size() > 0) {
                        val choice = obj.getAsJsonArray("choices").get(0).asJsonObject
                        if (choice.has("message") && choice.getAsJsonObject("message").has("content")) {
                            aiResponseText = choice.getAsJsonObject("message").get("content").asString
                        }
                    }
                }
                if (aiResponseText.isEmpty()) {
                    aiResponseText = response.toString()
                }

                // Extract [UNIVERSITY_RECOMMENDATIONS] block
                val uniList = mutableListOf<ParsedUniversity>()
                val regex = Regex("\\[UNIVERSITY_RECOMMENDATIONS\\](.*?)\\[/UNIVERSITY_RECOMMENDATIONS\\]", RegexOption.DOT_MATCHES_ALL)
                val match = regex.find(aiResponseText)
                var finalText = aiResponseText
                
                if (match != null) {
                    val block = match.groupValues[1].trim()
                    finalText = aiResponseText.replace(match.value, "").trim()
                    
                    block.split("\n").forEach { line ->
                        val cleanLine = line.trim().removePrefix("*").removePrefix("-").trim()
                        if (cleanLine.isNotBlank() && !cleanLine.lowercase().contains("university abbreviation")) {
                            val parts = cleanLine.split("|").map { it.trim() }
                            if (parts.size >= 5) {
                                uniList.add(ParsedUniversity(parts[0], parts[1], parts[2].replace("%", ""), parts[3], parts[4]))
                            } else if (parts.size >= 3) {
                                uniList.add(ParsedUniversity(parts[0], parts[1], parts[2].replace("%", ""), "Public", "English-taught"))
                            }
                        }
                    }
                }

                if (finalText.trim() == "UNKNOWN_QUERY") {
                    throw Exception("NVIDIA_UNKNOWN_QUERY")
                }

                // Replace loading message with actual response
                _messages.update { list ->
                    list.mapIndexed { index, chatMessage ->
                        if (index == loadingIndex) {
                            ChatMessage(
                                text = finalText, 
                                isUser = false, 
                                universities = if (uniList.isNotEmpty()) uniList else null
                            )
                        } else chatMessage
                    }
                }
            } catch (e: Exception) {
                // Nvidia failed (timeout or error) -> fallback to DronaHQ
                try {
                    val dronaHqUrl = com.example.edujourneygermany.BuildConfig.DRONAHQ_AGENT_URL
                    val dronaHqKey = com.example.edujourneygermany.BuildConfig.DRONAHQ_API_KEY
                    if (dronaHqUrl.isNotBlank()) {
                        val request = DronaHqRequest(
                            query = userText,
                            profile = userAnswers
                        )
                        val response = DronaHqClient.api.sendMessage(
                            url = dronaHqUrl,
                            apiKeyHeader = dronaHqKey,
                            request = request
                        )
                        
                        var aiResponseText = ""
                        if (response.isJsonObject) {
                            val obj = response.asJsonObject
                            if (obj.has("response") && obj.get("response").isJsonObject) {
                                val nestedResponse = obj.getAsJsonObject("response")
                                aiResponseText = nestedResponse.get("reply")?.asString ?: nestedResponse.get("answer")?.asString ?: ""
                            } else {
                                aiResponseText = obj.get("reply")?.asString ?: obj.get("answer")?.asString ?: obj.get("output")?.asString ?: ""
                            }
                        }
                        if (aiResponseText.isEmpty()) {
                            aiResponseText = response.toString()
                        }
                        
                        _messages.update { list ->
                            list.mapIndexed { index, chatMessage ->
                                if (index == loadingIndex) {
                                    ChatMessage(aiResponseText, isUser = false)
                                } else chatMessage
                            }
                        }
                    } else {
                        throw e
                    }
                } catch (dronaHqException: Exception) {
                    val errorMsg = if (e is retrofit2.HttpException && e.code() == 401) {
                        "I'm sorry, but my API key appears to be invalid or expired. Please update it in the dashboard."
                    } else if (e.message == "NVIDIA_UNKNOWN_QUERY") {
                        "I'm sorry, I couldn't find an answer to your question in my knowledge base."
                    } else {
                        "I'm sorry, I couldn't find an answer to your question."
                    }
                    _messages.update { list ->
                        list.mapIndexed { index, chatMessage ->
                            if (index == loadingIndex) {
                                ChatMessage(errorMsg, isUser = false)
                            } else chatMessage
                        }
                    }
                }
            }
        }
    }
}
