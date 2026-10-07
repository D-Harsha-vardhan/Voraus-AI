package com.example.edujourneygermany.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edujourneygermany.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.edujourneygermany.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel: JourneyViewModel = viewModel()
) {
    val steps by viewModel.steps.collectAsState()
    
    // Calculate progress based on completed steps
    val completedCount = steps.count { it.status == StepStatus.Completed }
    val progress = if (steps.isNotEmpty()) completedCount.toFloat() / steps.size else 0f
    val progressPercentage = (progress * 100).toInt()
    val context = LocalContext.current

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp, top = 24.dp)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val userName = com.example.edujourneygermany.auth.UserSession.userName.ifEmpty { "Adithya" }
                        Text(
                            text = "Good morning, $userName \uD83D\uDC4B",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your Germany journey is on track.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Avatar placeholder
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Progress Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Circular Progress
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
                                CircularProgressIndicator(
                                    progress = progress,
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color(0xFF10B981),
                                    trackColor = Color(0xFFE2E8F0),
                                    strokeWidth = 4.dp
                                )
                                Text("$progressPercentage%", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Profile Completion",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    "$completedCount of ${steps.size} steps completed",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = PrimaryBlue,
                            trackColor = Color(0xFFE2E8F0)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Target Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🇩🇪", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Germany", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // Action Icons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ActionIcon("Start Journey", "🛫", PrimaryBlue) { onNavigate("goal") }
                    ActionIcon("AI Assistant", "🤖", Color(0xFF6366F1)) { onNavigate("advisor") }
                    ActionIcon("My Profile", "👤", Color(0xFF8B5CF6)) { onNavigate("profile") }
                    ActionIcon("Messages", "💬", Color(0xFFD946EF)) { }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Text(
                    text = "Your Journey Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            itemsIndexed(steps) { index, step ->
                JourneyProgressItem(
                    index = index + 1,
                    step = step,
                    onClick = { 
                        if (step.status == StepStatus.Completed || step.status == StepStatus.InProgress) {
                            step.route?.let { onNavigate(it) }
                        } else {
                            Toast.makeText(context, "Please complete previous steps first", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                if (index < steps.size - 1) {
                    Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(start = 40.dp))
                }
            }
        }
    }
}

@Composable
fun ActionIcon(title: String, emoji: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun JourneyProgressItem(index: Int, step: JourneyStepData, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Number Circle
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (step.status == StepStatus.Completed) Color(0xFF10B981) else Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = index.toString(),
                color = if (step.status == StepStatus.Completed) Color.White else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = step.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1E293B),
            modifier = Modifier.weight(1f)
        )
        
        // Status Badge
        val (bgColor, textColor, text) = when (step.status) {
            StepStatus.Completed -> Triple(Color(0xFFD1FAE5), Color(0xFF059669), "Completed")
            StepStatus.InProgress -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "In Progress")
            StepStatus.Pending -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), "Pending")
        }
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
    }
}
