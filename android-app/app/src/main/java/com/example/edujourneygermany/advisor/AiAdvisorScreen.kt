package com.example.edujourneygermany.advisor

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.ui.unit.sp

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAdvisorScreen(
    onBookConsultant: () -> Unit = {},
    onViewDetails: (String, String, String) -> Unit = { _, _, _ -> },
    sharedViewModel: com.example.edujourneygermany.presentation.SharedUniversityViewModel = viewModel(),
    viewModel: AiAdvisorViewModel = viewModel()
) {
    var messageText by remember { mutableStateOf("") }
    var showOptionsSheet by remember { mutableStateOf(false) }
    val messages by viewModel.messages.collectAsState()
    val currentOptions by viewModel.currentOptions.collectAsState()
    val isFlowComplete by viewModel.isFlowComplete.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Scaffold(
        topBar = {
            val context = LocalContext.current
            TopAppBar(
                title = { Text("AI Advisor", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://elevenlabs.io/app/talk-to?agent_id=agent_0301m4ebw1qee439epz1b3fh56k9&branch_id=agtbrch_3301m4ebw2v0emmvgjpj3dv68pse"))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Agent", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Voice Agent")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 100.dp) // Nav bar padding
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(messages.size) { index ->
                    val msg = messages[index]
                    if (msg.isUser) {
                        UserMessage(msg.text, msg.quotedQuestion)
                    } else {
                        // Show "Choose" button only on the last message if flow is not complete
                        val isLastAndFlowActive = !isFlowComplete && index == messages.lastIndex && !msg.isLoading
                        AiSimpleMessage(
                            text = msg.text, 
                            isLoading = msg.isLoading,
                            onOptionsClick = if (isLastAndFlowActive) { { showOptionsSheet = true } } else null
                        )
                        if (!msg.universities.isNullOrEmpty()) {
                            LaunchedEffect(msg.universities) {
                                sharedViewModel.updateRecommendations(msg.universities)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TopUniversityRecommendationsCard(
                                universities = msg.universities,
                                onViewDetails = onViewDetails
                            )
                        }
                    }
                }
            }

            if (isFlowComplete) {
                // Input field area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .border(1.dp, Color.LightGray.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                            .padding(horizontal = 8.dp, vertical = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Ask me anything...", color = Color.Gray) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            )
                        )
                        IconButton(onClick = { 
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(messageText)
                                messageText = ""
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFF1976D2))
                        }
                    }
                }
            }
        }
        
        if (showOptionsSheet && currentOptions.isNotEmpty()) {
            ModalBottomSheet(
                onDismissRequest = { showOptionsSheet = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, top = 8.dp)
                ) {
                    Text(
                        text = "Choose",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    currentOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.sendMessage(option)
                                    showOptionsSheet = false
                                }
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            RadioButton(
                                selected = false,
                                onClick = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserMessage(text: String, quotedQuestion: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 4.dp
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (quotedQuestion != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            // Green accent bar
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(4.dp)
                                    .background(Color(0xFF10B981))
                            )
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "Educaro AI",
                                    color = Color(0xFF10B981),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = quotedQuestion,
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = text,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun AiSimpleMessage(text: String, isLoading: Boolean = false, onOptionsClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 4.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (isLoading) {
                    TypingIndicator()
                } else {
                    val lines = text.split("\n")
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.startsWith("[UNIVERSITY]")) {
                            val parts = trimmed.removePrefix("[UNIVERSITY]").split("|").map { it.trim() }
                            if (parts.size >= 3) {
                                Spacer(modifier = Modifier.height(8.dp))
                                EmbeddedUniversityCard(
                                    iconInitial = parts[0].take(1),
                                    university = parts[0],
                                    program = parts[1],
                                    location = parts[2]
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        } else {
                            if (trimmed.isNotBlank()) {
                                Text(
                                    text = trimmed,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                    
                    if (onOptionsClick != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOptionsClick() }
                                .padding(top = 12.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.FormatListBulleted, 
                                contentDescription = "Choose",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Choose",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 150L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0.0f at 0 using LinearOutSlowInEasing
                        1.0f at 300 using LinearOutSlowInEasing
                        0.0f at 600 using LinearOutSlowInEasing
                        0.0f at 1200 using LinearOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = (-8).dp * animatable.value)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
fun AiComplexMessage(onBookConsultant: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 4.dp,
                bottomEnd = 20.dp
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Based on your profile, here are some suitable programs in Germany:",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Embedded Card 1
                EmbeddedUniversityCard(
                    iconInitial = "T",
                    university = "Technical University of Munich",
                    program = "M.Sc. Computer Science",
                    location = "Munich • Germany"
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Embedded Card 2
                EmbeddedUniversityCard(
                    iconInitial = "R",
                    university = "RWTH Aachen University",
                    program = "M.Sc. Artificial Intelligence",
                    location = "Aachen • Germany"
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "These recommendations are based on your B.Tech in Computer Science and IELTS 7.5 (English met). You're missing a German B1 certificate and an APS certificate for most programs.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { /* TODO */ }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)) {
                        Text("Generate CV", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(onClick = { /* TODO */ }, shape = RoundedCornerShape(16.dp), modifier = Modifier.weight(1f)) {
                        Text("Upload Docs", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onBookConsultant,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Book a Call with Educaro Consultant", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TopUniversityRecommendationsCard(
    universities: List<ParsedUniversity>,
    onViewDetails: (String, String, String) -> Unit = { _, _, _ -> }
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp)
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            // Header
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.School, 
                    contentDescription = "Recommendations",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Top University Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )
            }
            
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            
            // List of universities
            universities.forEachIndexed { index, uni ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo box
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0D47A1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uni.name.take(3).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(uni.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
                        Text(uni.program, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp)) {
                                Text(uni.type, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF1976D2))
                            }
                            Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp)) {
                                Text(uni.language, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF1976D2))
                            }
                        }
                    }
                    
                    // Match score and Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                                drawCircle(color = Color(0xFFE8F5E9))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${uni.matchScore}%", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 16.sp)
                            }
                        }
                        Text("Match", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { onViewDetails(uni.name, uni.program, uni.matchScore) },
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                            border = BorderStroke(1.dp, Color(0xFF1976D2).copy(alpha = 0.5f))
                        ) {
                            Text("View Details", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1976D2))
                        }
                    }
                }
                if (index < universities.lastIndex) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
            
            // Footer
            Surface(color = Color(0xFFF5F9FF)) {
                Row(modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Star, contentDescription = "Sparkle", tint = Color(0xFF64B5F6), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "These recommendations are based on your degree, GPA, target intake and current German level. You can save any program to start the application process.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1976D2).copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun EmbeddedUniversityCard(iconInitial: String, university: String, program: String, location: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0D47A1)),
            contentAlignment = Alignment.Center
        ) {
            Text(iconInitial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(university, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF0D47A1))
            Text(program, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(location, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}
