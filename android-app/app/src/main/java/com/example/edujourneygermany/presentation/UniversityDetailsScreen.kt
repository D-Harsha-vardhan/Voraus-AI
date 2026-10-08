package com.example.edujourneygermany.presentation

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversityDetailsScreen(
    uniName: String,
    programName: String,
    matchScore: String,
    onBack: () -> Unit,
    viewModel: UniversityDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val decodedUni = Uri.decode(uniName)
    val decodedProgram = Uri.decode(programName)
    val decodedScore = Uri.decode(matchScore).ifBlank { "90" }

    LaunchedEffect(decodedUni, decodedProgram) {
        viewModel.loadDetails(context, decodedUni, decodedProgram)
    }

    val uniProfile by viewModel.universityProfile.collectAsState()
    val programDetails by viewModel.programDetails.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO Bookmark */ }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header Image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    if (uniProfile?.imageUrl?.isNotBlank() == true) {
                        AsyncImage(
                            model = uniProfile!!.imageUrl,
                            contentDescription = "University Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color(0xFF64B5F6), Color(0xFF1976D2))))
                        )
                    }
                    
                    // Card Overlapping Bottom
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = 120.dp)
                            .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .offset(y = (-32).dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF0D47A1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = decodedUni.take(3).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = uniProfile?.name ?: decodedUni,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D47A1),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = programDetails?.duration?.let { "$decodedProgram" } ?: decodedProgram,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp)) {
                                        Text("Germany", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF1976D2))
                                    }
                                    Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp)) {
                                        Text(uniProfile?.publicOrPrivate ?: "Public", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF1976D2))
                                    }
                                    Surface(color = Color(0xFFE3F2FD), shape = RoundedCornerShape(12.dp)) {
                                        Text(programDetails?.language ?: "English-taught", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF1976D2))
                                    }
                                }
                                Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(12.dp)) {
                                    Text("$decodedScore% Match", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(140.dp)) }

            // Quick Info Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoItem(icon = Icons.Default.Person, title = "Tuition", value = programDetails?.tuition?.let { if(it == "0") "€0/yr" else it } ?: "€0 - €2,000/yr")
                    InfoItem(icon = Icons.Default.Schedule, title = "Duration", value = programDetails?.duration ?: "2 years")
                    InfoItem(icon = Icons.Default.DateRange, title = "Intake", value = "WS 2026")
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // About Section
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "About the University",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uniProfile?.about ?: "A leading university in Germany known for its strong research, innovation, and industry connections. It offers excellent opportunities in computer science, AI and engineering.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Apply Button
            item {
                Button(
                    onClick = { /* TODO Apply */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("Apply Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.OpenInNew, contentDescription = "External Link", modifier = Modifier.size(18.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Application Tracker Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF81C784).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = "Calendar", tint = Color(0xFF4CAF50), modifier = Modifier.padding(8.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Application & Deadline Tracker", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text("Track your application status and important dates.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = Color(0xFF90CAF9), modifier = Modifier.size(12.dp)) {}
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Application Not Started", style = MaterialTheme.typography.labelMedium, color = Color(0xFF1976D2))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Deadline", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text("15 Nov 2025", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF1976D2),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Overview", fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Requirements", fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Gallery", fontWeight = FontWeight.Bold) })
                }
            }

            // Tab Content
            item {
                Box(modifier = Modifier.padding(16.dp)) {
                    when (selectedTab) {
                        0 -> Text("Overview details from CSV: ${programDetails?.deadlineNotes ?: ""}", color = Color.Gray)
                        1 -> Text("Requirements: ${programDetails?.requirements ?: ""}\nIELTS: ${programDetails?.ielts ?: ""}", color = Color.Gray)
                        2 -> Text("Gallery coming soon.", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = title, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}
