package com.example.edujourneygermany.presentation

import androidx.lifecycle.ViewModel
import com.example.edujourneygermany.advisor.ParsedUniversity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedUniversityViewModel : ViewModel() {
    private val _recommendedUniversities = MutableStateFlow<List<ParsedUniversity>>(emptyList())
    val recommendedUniversities: StateFlow<List<ParsedUniversity>> = _recommendedUniversities.asStateFlow()

    fun updateRecommendations(universities: List<ParsedUniversity>) {
        _recommendedUniversities.value = universities
    }
}
