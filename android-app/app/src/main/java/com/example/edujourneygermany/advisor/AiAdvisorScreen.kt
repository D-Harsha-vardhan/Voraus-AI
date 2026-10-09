package com.example.edujourneygermany.advisor

import android.Manifest
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAdvisorScreen(
    onBookConsultant: () -> Unit = {},
    onViewDetails: (String, String, String) -> Unit = { _, _, _ -> },
    sharedViewModel: com.example.edujourneygermany.presentation.SharedUniversityViewModel = viewModel(),
    viewModel: AiAdvisorViewModel = viewModel()
) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val currentOptions by viewModel.currentOptions.collectAsState()
    val isFlowComplete by viewModel.isFlowComplete.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    var showVoiceAgent by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showVoiceAgent = true
            }
        }
    )

    // Curated quick prompt starters
    val promptStarters = listOf(
        "🎓 English Master's in CS",
        "🏛️ TU Munich Deadlines",
        "📜 APS Certificate Steps",
        "💶 Blocked Account 2026/27",
        "🇩🇪 Language Requirements"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Surface(
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Column {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        "AI Advisor",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp,
                                        color = Color(0xFF0F172A)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .background(Color(0xFF10B981), CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            "Online • Voraus Llama 3.2 NIM",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.resetChat() }
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = "Restart Chat",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF1E3A8A).copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, Color(0xFF1E3A8A).copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .clickable { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                                        .padding(end = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = Color(0xFF1E3A8A),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            "Voice Agent",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E3A8A)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                        )
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF8FAFC))
            ) {
                // Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 12.dp)
                ) {
                    // Welcome Onboarding Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E3A8A).copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.School,
                                            contentDescription = null,
                                            tint = Color(0xFF1E3A8A),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "Voraus German Study & Visa Advisor",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            "TU9 Admissions • APS • Deadlines • Blocked Account",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Guten Tag! Ask me any question about applying to German public universities, tuition fees, language requirements, or visa appointments.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF475569),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    // Conversation Messages
                    items(messages.size) { index ->
                        val msg = messages[index]
                        if (msg.isUser) {
                            UserMessage(msg.text, msg.quotedQuestion)
                        } else {
                            AiSimpleMessage(
                                text = msg.text,
                                isLoading = msg.isLoading
                            )
                            if (!msg.universities.isNullOrEmpty()) {
                                LaunchedEffect(msg.universities) {
                                    sharedViewModel.updateRecommendations(msg.universities)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                TopUniversityRecommendationsCard(
                                    universities = msg.universities,
                                    onViewDetails = onViewDetails
                                )
                            }
                        }
                    }
                }

                // Interactive Option & Suggestion Chips Strip
                val activeChips = if (currentOptions.isNotEmpty()) currentOptions else if (messages.size <= 2) promptStarters else emptyList()
                if (activeChips.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activeChips) { option ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.45f)),
                                shadowElevation = 1.dp,
                                modifier = Modifier.clickable {
                                    viewModel.sendMessage(option)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        color = Color(0xFF1E3A8A),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        if (currentOptions.isNotEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.clickable {
                                        viewModel.skipFlow()
                                    }
                                ) {
                                    Text(
                                        text = "Skip setup ⚡",
                                        color = Color(0xFF64748B),
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.5.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ALWAYS-AVAILABLE Floating Minimalist Input Dock (Clears bottom navigation bar cleanly)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .padding(bottom = 96.dp) // Perfect clearance above 82dp floating bottom navigation bar
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF1E3A8A),
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            TextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = {
                                    Text(
                                        "Ask about universities, visas, APS...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp
                                    )
                                },
                                maxLines = 3,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A),
                                    cursorColor = Color(0xFF1E3A8A)
                                )
                            )
                            if (messageText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val textToSend = messageText
                                        messageText = ""
                                        viewModel.sendMessage(textToSend)
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0xFF1E3A8A), CircleShape)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0xFFF1F5F9), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.Mic,
                                        contentDescription = "Voice",
                                        tint = Color(0xFF1E3A8A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showVoiceAgent) {
            Dialog(
                onDismissRequest = { showVoiceAgent = false },
                properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
                    IconButton(
                        onClick = { showVoiceAgent = false },
                        modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).padding(top = 24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                    
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                webChromeClient = object : WebChromeClient() {
                                    override fun onPermissionRequest(request: PermissionRequest) {
                                        request.grant(request.resources)
                                    }
                                }
                                webViewClient = WebViewClient()
                                
                                val userName = com.example.edujourneygermany.data.UserProfileStore.fullName
                                val userGerman = com.example.edujourneygermany.data.UserProfileStore.germanLevel
                                val userCgpa = viewModel.userAnswers["cgpa"] ?: ""
                                val userCourse = viewModel.userAnswers["course"] ?: ""
                                val userLevel = viewModel.userAnswers["level"] ?: ""
                                
                                val htmlContent = """
                                    <!DOCTYPE html>
                                    <html>
                                    <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1">
                                    <style>
                                      body { margin: 0; padding: 0; height: 100vh; display: flex; justify-content: center; align-items: center; background-color: transparent; }
                                    </style>
                                    </head>
                                    <body>
                                      <elevenlabs-convai agent-id="${com.example.edujourneygermany.BuildConfig.ELEVENLABS_AGENT_ID}"></elevenlabs-convai>
                                      <script>
                                        const el = document.querySelector("elevenlabs-convai");
                                        el.setAttribute("dynamic-variables", JSON.stringify({
                                            name: "${userName}",
                                            cgpa: "${userCgpa}",
                                            german_score: "${userGerman}",
                                            goal: "${userLevel}",
                                            course: "${userCourse}"
                                        }));
                                      </script>
                                      <script src="https://unpkg.com/@elevenlabs/convai-widget-embed" async type="text/javascript"></script>
                                    </body>
                                    </html>
                                """.trimIndent()
                                
                                loadDataWithBaseURL("https://elevenlabs.io", htmlContent, "text/html", "UTF-8", null)
                            }
                        },
                        modifier = Modifier.fillMaxSize().padding(top = 80.dp)
                    )
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
                topStart = 18.dp,
                topEnd = 4.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A8A)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (quotedQuestion != null) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.22f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(3.dp)
                                    .background(Color(0xFF34D399))
                            )
                            Text(
                                text = quotedQuestion,
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal
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
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E3A8A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Card(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TypingIndicator()
                        Text(
                            "AI Advisor is thinking...",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                } else {
                    val lines = text.split("\n")
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.startsWith("[UNIVERSITY]")) {
                            val parts = trimmed.removePrefix("[UNIVERSITY]").split("|").map { it.trim() }
                            if (parts.size >= 3) {
                                Spacer(modifier = Modifier.height(6.dp))
                                EmbeddedUniversityCard(
                                    iconInitial = parts[0].take(1),
                                    university = parts[0],
                                    program = parts[1],
                                    location = parts[2]
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        } else if (trimmed.isNotBlank()) {
                            Text(
                                text = trimmed,
                                color = Color(0xFF0F172A),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
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

