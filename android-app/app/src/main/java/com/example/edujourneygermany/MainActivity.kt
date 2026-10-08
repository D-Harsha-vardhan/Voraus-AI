package com.example.edujourneygermany

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.edujourneygermany.navigation.AppNavigation
import com.example.edujourneygermany.theme.EduJourneyGermanyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("edu_journey_prefs", android.content.Context.MODE_PRIVATE)
        com.example.edujourneygermany.data.UserProfileStore.hasSeenAccessibilityScreen = prefs.getBoolean("has_seen_accessibility", false)
        com.example.edujourneygermany.data.UserProfileStore.isBlindModeEnabled = prefs.getBoolean("blind_mode_enabled", false)
        
        setContent {
            EduJourneyGermanyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
