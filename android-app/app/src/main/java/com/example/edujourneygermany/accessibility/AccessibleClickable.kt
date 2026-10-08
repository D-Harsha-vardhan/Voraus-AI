package com.example.edujourneygermany.accessibility

import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.example.edujourneygermany.data.UserProfileStore

object AccessibilityState {
    var currentlyFocusedElement by mutableStateOf<String?>(null)
}

fun Modifier.accessibleClickable(
    spokenText: String,
    onClick: () -> Unit
): Modifier = composed {
    val elementId = remember { java.util.UUID.randomUUID().toString() }

    if (UserProfileStore.isBlindModeEnabled) {
        this.clickable {
            if (AccessibilityState.currentlyFocusedElement != elementId) {
                // First tap: Set focus and speak the text
                AccessibilityState.currentlyFocusedElement = elementId
                AppSpeaker.speak(spokenText)
            } else {
                // Second tap on the same element: Trigger action and reset focus
                onClick()
                AccessibilityState.currentlyFocusedElement = null
            }
        }
    } else {
        // Normal behavior
        this.clickable { onClick() }
    }
}
