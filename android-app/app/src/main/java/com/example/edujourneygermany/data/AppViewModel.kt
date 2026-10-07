package com.example.edujourneygermany.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edujourneygermany.network.RetrofitClient
import com.example.edujourneygermany.network.OpportunityDto
import com.example.edujourneygermany.network.DocumentDto
import com.example.edujourneygermany.network.ChatMessageDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {
    private val api = RetrofitClient.api

    private val _opportunities = MutableStateFlow<List<OpportunityDto>>(emptyList())
    val opportunities: StateFlow<List<OpportunityDto>> = _opportunities

    private val _documents = MutableStateFlow<List<DocumentDto>>(emptyList())
    val documents: StateFlow<List<DocumentDto>> = _documents

    private val _chatHistory = MutableStateFlow<List<ChatMessageDto>>(emptyList())
    val chatHistory: StateFlow<List<ChatMessageDto>> = _chatHistory
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch from endpoints
                _opportunities.value = api.getOpportunities()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                _documents.value = api.getDocuments()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                _chatHistory.value = api.getChatHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }
}
