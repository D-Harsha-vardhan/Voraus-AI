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
import com.example.edujourneygermany.data.UserProfileStore
import com.example.edujourneygermany.data.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToDocuments: () -> Unit,
    onNavigateToQualification: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val user = Supabase.client.auth.currentUserOrNull()
            val uid = user?.id
            if (user != null) {
                user.email?.let { UserProfileStore.email = it }
            }
            if (uid != null) {
                val profileResult = Supabase.client.postgrest["profiles"]
                    .select { filter { eq("id", uid) } }
                    .decodeSingle<JsonObject>()
                
                profileResult["full_name"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.fullName = it }
                profileResult["passport_number"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.passportNumber = it }
                profileResult["phone_number"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.phone = it }
                profileResult["location"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.location = it }
                profileResult["degree"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.degree = it }
                profileResult["university"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.university = it }
                profileResult["graduation_year"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.graduationYear = it }
                profileResult["role"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.role = it }
                profileResult["company"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.company = it }
                profileResult["english_level"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.englishLevel = it }
                profileResult["german_level"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.germanLevel = it }
                profileResult["date_of_birth"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.dob = it }
                profileResult["nationality"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.nationality = it }
                profileResult["professional_summary"]?.jsonPrimitive?.contentOrNull?.let { UserProfileStore.cvIntro = it }
                
                val localIntro = com.example.edujourneygermany.data.LocalDocumentManager.getCvIntroLocally(context)
                if (localIntro.isNotBlank() && UserProfileStore.cvIntro.isBlank()) {
                    UserProfileStore.cvIntro = localIntro
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

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
                    Text(
                        text = if (UserProfileStore.fullName.isNotEmpty()) UserProfileStore.fullName else "Your Name", 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold
                    )
                    Text(if (UserProfileStore.email.isNotEmpty()) UserProfileStore.email else "user@example.com", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
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
                ProfileInfoRow("Date of Birth", UserProfileStore.dob.ifEmpty { "Not provided" })
                ProfileInfoRow("Nationality", UserProfileStore.nationality.ifEmpty { "Not provided" })
                ProfileInfoRow("Passport Number", UserProfileStore.passportNumber.ifEmpty { "Not provided" })
            }
            
            ProfileSection(title = "Contact Details", status = "Verified", statusColor = Color(0xFF4CAF50)) {
                ProfileInfoRow("Phone", UserProfileStore.phone.ifEmpty { "Not provided" })
                ProfileInfoRow("Location", UserProfileStore.location.ifEmpty { "Not provided" })
            }
            
            ProfileSection(title = "Education", status = "Verified", statusColor = Color(0xFF4CAF50)) {
                ProfileInfoRow("Degree", UserProfileStore.degree.ifEmpty { "Not provided" })
                ProfileInfoRow("University", UserProfileStore.university.ifEmpty { "Not provided" })
                ProfileInfoRow("Graduation", UserProfileStore.graduationYear.ifEmpty { "Not provided" })
            }
            
            ProfileSection(title = "Experience", status = "Needs Review", statusColor = Color(0xFFFF9800)) {
                ProfileInfoRow("Role", UserProfileStore.role.ifEmpty { "Not provided" })
                ProfileInfoRow("Company", UserProfileStore.company.ifEmpty { "Not provided" })
                ProfileInfoRow("Duration", UserProfileStore.experienceDuration.ifEmpty { "Not provided" })
            }
            
            ProfileSection(title = "Languages", status = "Verified", statusColor = Color(0xFF4CAF50)) {
                ProfileInfoRow("English", UserProfileStore.englishLevel.ifEmpty { "Not provided" })
                ProfileInfoRow("German", UserProfileStore.germanLevel.ifEmpty { "Not provided" })
            }
            
            ProfileSection(title = "Social Links", status = "Optional", statusColor = Color(0xFF9E9E9E)) {
                ProfileInfoRow("LinkedIn", UserProfileStore.linkedIn.ifEmpty { "Not provided" })
                ProfileInfoRow("GitHub", UserProfileStore.github.ifEmpty { "Not provided" })
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    scope.launch {
                        try {
                            Supabase.client.auth.signOut()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        onLogout()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(80.dp))
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
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
