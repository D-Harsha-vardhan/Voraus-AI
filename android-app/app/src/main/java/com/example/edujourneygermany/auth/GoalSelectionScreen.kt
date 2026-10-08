package com.example.edujourneygermany.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import com.example.edujourneygermany.data.UserProfileStore
import com.example.edujourneygermany.accessibility.AppSpeaker
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edujourneygermany.R
import com.example.edujourneygermany.theme.PrimaryBlue

data class GoalData(
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector,
    val iconColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSelectionScreen(onNext: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) {
        if (UserProfileStore.isBlindModeEnabled) {
            AppSpeaker.speak("What is your primary goal in Germany? Options are Study, Work, or language learning.")
        }
    }
    var selectedGoal by remember { mutableStateOf<Int?>(0) }

    val goals = listOf(
        GoalData(R.string.goal_study, R.string.goal_study_desc, Icons.Default.School, PrimaryBlue),
        GoalData(R.string.goal_vocational, R.string.goal_vocational_desc, Icons.Default.BusinessCenter, Color(0xFF10B981)),
        GoalData(R.string.goal_employment, R.string.goal_employment_desc, Icons.Default.Work, Color(0xFF8B5CF6))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.step_1_goal),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.what_is_main_goal),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            goals.forEachIndexed { index, goal ->
                GoalOptionCard(
                    title = stringResource(goal.titleRes),
                    description = stringResource(goal.descRes),
                    icon = goal.icon,
                    isSelected = selectedGoal == index,
                    onClick = { selectedGoal = index },
                    iconColor = goal.iconColor
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                enabled = selectedGoal != null
            ) {
                Text(stringResource(R.string.next_btn), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun GoalOptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    iconColor: Color
) {
    val borderColor = if (isSelected) PrimaryBlue else Color(0xFFE2E8F0)
    val bgColor = if (isSelected) PrimaryBlue.copy(alpha = 0.05f) else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(2.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    title, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.Gray
                )
            }
        }
    }
}
