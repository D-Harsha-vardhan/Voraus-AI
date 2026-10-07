package com.example.edujourneygermany.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class StepStatus {
    Completed, InProgress, Pending
}

data class JourneyStepData(
    val id: String,
    val title: String,
    val status: StepStatus,
    val route: String? = null
)

class JourneyViewModel : ViewModel() {

    // Ideally, this list is fetched from backend/Supabase based on profileCompletion status
    // For now, it is held in memory but can easily be populated via an API call.
    private val _steps = MutableStateFlow(
        listOf(
            JourneyStepData("1", "Goal", StepStatus.InProgress, "goal"),
            JourneyStepData("2", "About You", StepStatus.Pending, "profile"),
            JourneyStepData("3", "Documents Upload", StepStatus.Pending, "documents"),
            JourneyStepData("4", "Video Upload", StepStatus.Pending, "video_intro"),
            JourneyStepData("5", "Merge Profile", StepStatus.Pending, "merge_profile"),
            JourneyStepData("6", "Verification", StepStatus.Pending, "verification"),
            JourneyStepData("7", "Generate CV", StepStatus.Pending, "cv"),
            JourneyStepData("8", "Financial Readiness", StepStatus.Pending, "financial_readiness"),
            JourneyStepData("9", "Eligibility Assessment", StepStatus.Pending, "qualification"),
            JourneyStepData("10", "Program Matching", StepStatus.Pending, "opportunities"),
            JourneyStepData("11", "Recommendation", StepStatus.Pending, "recommendation")
        )
    )
    
    val steps: StateFlow<List<JourneyStepData>> = _steps.asStateFlow()

    fun completeStep(stepId: String) {
        _steps.update { currentList ->
            var foundCurrent = false
            currentList.map { step ->
                if (step.id == stepId) {
                    foundCurrent = true
                    step.copy(status = StepStatus.Completed)
                } else if (foundCurrent && step.status == StepStatus.Pending) {
                    foundCurrent = false
                    step.copy(status = StepStatus.InProgress)
                } else {
                    step
                }
            }
        }
    }
}
