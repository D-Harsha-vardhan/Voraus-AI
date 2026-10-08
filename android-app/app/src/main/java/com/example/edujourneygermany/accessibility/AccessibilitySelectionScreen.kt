package com.example.edujourneygermany.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edujourneygermany.data.UserProfileStore
import kotlinx.coroutines.delay

@Composable
fun AccessibilitySelectionScreen(
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        AppSpeaker.init(context)
        delay(500)
        AppSpeaker.speak("Welcome to Edu Journey Germany. Are you visually impaired? Tap anywhere on the top half of the screen for Yes, or tap anywhere on the bottom half of the screen for No.")
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF4CAF50))
                .clickable {
                    UserProfileStore.isBlindModeEnabled = true
                    UserProfileStore.hasSeenAccessibilityScreen = true
                    context.getSharedPreferences("edu_journey_prefs", android.content.Context.MODE_PRIVATE).edit().apply {
                        putBoolean("has_seen_accessibility", true)
                        putBoolean("blind_mode_enabled", true)
                        apply()
                    }
                    AppSpeaker.speak("Blind Mode activated. Proceeding to app.")
                    onNavigateNext()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "YES\n(Tap anywhere in top half)", 
                color = Color.White, 
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFF44336))
                .clickable {
                    UserProfileStore.isBlindModeEnabled = false
                    UserProfileStore.hasSeenAccessibilityScreen = true
                    context.getSharedPreferences("edu_journey_prefs", android.content.Context.MODE_PRIVATE).edit().apply {
                        putBoolean("has_seen_accessibility", true)
                        putBoolean("blind_mode_enabled", false)
                        apply()
                    }
                    AppSpeaker.stop()
                    onNavigateNext()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "NO\n(Tap anywhere in bottom half)", 
                color = Color.White, 
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
