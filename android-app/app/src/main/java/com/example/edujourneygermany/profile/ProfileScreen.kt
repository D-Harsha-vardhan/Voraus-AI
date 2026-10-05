package com.example.edujourneygermany.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToDocuments: () -> Unit,
    onNavigateToQualification: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 100.dp) // Nav bar padding
        ) {
            // Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val userName = com.example.edujourneygermany.auth.UserSession.userName
                    val userEmail = com.example.edujourneygermany.auth.UserSession.userEmail
                    Text(if (userName.isNotEmpty()) userName else "Applicant", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(if (userEmail.isNotEmpty()) userEmail else "Not provided", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onNavigateToEditProfile,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Edit Profile")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sections
            ProfileSection(title = "Personal Information", status = "Verified", statusColor = Color(0xFF4CAF50)) {
                ProfileInfoRow("Date of Birth", "15 May 1998", "Verified by Passport")
                ProfileInfoRow("Nationality", "Indian", "Verified by Passport")
                ProfileInfoRow("Passport Number", "Z1234567", "AI-extracted from Passport")
            }
            
            ProfileSection(title = "Contact Details", status = "Applicant-provided", statusColor = Color(0xFF2196F3)) {
                ProfileInfoRow("Phone", "+91 98765 43210", "Applicant-provided")
                ProfileInfoRow("Location", "Mumbai, India", "Applicant-provided")
            }
            
            ProfileSection(title = "Education", status = "Verified", statusColor = Color(0xFF4CAF50)) {
                ProfileInfoRow("Degree", "B.Tech in Computer Science", "Verified by APS")
                ProfileInfoRow("University", "ABC University", "Verified by APS")
                ProfileInfoRow("Graduation", "2020", "Applicant-provided")
            }
            
            ProfileSection(title = "Experience", status = "Needs Review", statusColor = Color(0xFFFF9800)) {
                ProfileInfoRow("Role", "Software Engineer", "Applicant-provided")
                ProfileInfoRow("Company", "Tech Solutions Inc.", "Applicant-provided")
                ProfileInfoRow("Duration", "2 Years", "AI-extracted from CV")
            }
            
            ProfileSection(title = "Skills", status = "AI-generated", statusColor = Color(0xFF9C27B0)) {
                ProfileInfoRow("Top Skills", "Java, Kotlin, Spring Boot", "AI-extracted from CV")
                ProfileInfoRow("Frameworks", "Jetpack Compose", "Applicant-provided")
            }
            
            ProfileSection(title = "Languages", status = "Verified", statusColor = Color(0xFF4CAF50)) {
                ProfileInfoRow("English", "IELTS 7.5 (C1)", "Verified by IELTS Certificate")
                ProfileInfoRow("German", "A1", "Applicant-provided")
            }
            
            ProfileSection(title = "Motivation & Goals", status = "AI-generated", statusColor = Color(0xFF9C27B0)) {
                ProfileInfoRow("Goal", "Study Master's in Germany", "Applicant-provided")
                ProfileInfoRow("Summary", "Highly motivated to pursue research in AI.", "AI-extracted from Video")
            }
            
            ProfileSection(title = "Social Links", status = "Optional", statusColor = Color(0xFF9E9E9E)) {
                ProfileInfoRow("LinkedIn", "linkedin.com/in/applicant", "Applicant-provided")
                ProfileInfoRow("GitHub", "github.com/applicant", "Applicant-provided")
            }
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    status: String,
    statusColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (status) {
                            "Verified" -> Icons.Default.CheckCircle
                            "Needs Review" -> Icons.Default.Warning
                            else -> Icons.Default.Info
                        }
                        Icon(icon, contentDescription = status, tint = statusColor, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(status, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String, provenance: String = "Applicant-provided") {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
        Text(provenance, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
    }
}
