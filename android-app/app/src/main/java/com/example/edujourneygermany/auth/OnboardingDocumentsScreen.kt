package com.example.edujourneygermany.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.util.Base64
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.edujourneygermany.network.RetrofitClient
import com.example.edujourneygermany.network.UploadDocumentRequestDto
import com.example.edujourneygermany.data.UserProfileStore
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingDocumentsScreen(onNext: () -> Unit, onBack: () -> Unit) {
    var passportUri by remember { mutableStateOf<Uri?>(null) }
    var degreeUri by remember { mutableStateOf<Uri?>(null) }
    var languageUri by remember { mutableStateOf<Uri?>(null) }
    var resumeUri by remember { mutableStateOf<Uri?>(null) }
    var otherUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isUploading by remember { mutableStateOf(false) }

    fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    val passportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> passportUri = uri }
    val degreeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> degreeUri = uri }
    val languageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> languageUri = uri }
    val resumeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> resumeUri = uri }
    val otherLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> otherUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                "3. Documents Upload",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Upload your required documents",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = 3f / 3f,
                    modifier = Modifier.weight(1f).height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("3/3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            DocumentUploadCard(
                title = "Passport",
                isUploaded = passportUri != null,
                icon = Icons.Default.Description,
                onClick = { passportLauncher.launch("*/*") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DocumentUploadCard(
                title = "Degree Certificate",
                isUploaded = degreeUri != null,
                icon = Icons.Default.Description,
                onClick = { degreeLauncher.launch("*/*") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DocumentUploadCard(
                title = "Language Certificate",
                isUploaded = languageUri != null,
                icon = Icons.Default.Description,
                isError = languageUri == null, // Show red if not uploaded? The image shows "Not Uploaded" in red. Let's pass a special state
                statusText = if (languageUri != null) "Uploaded" else "Not Uploaded",
                onClick = { languageLauncher.launch("*/*") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DocumentUploadCard(
                title = "Resume (CV)",
                isUploaded = resumeUri != null,
                icon = Icons.Default.Description,
                onClick = { resumeLauncher.launch("*/*") }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DocumentUploadCard(
                title = "Other Documents",
                isUploaded = otherUri != null,
                icon = Icons.Default.Description,
                statusText = if (otherUri != null) "Uploaded" else "Optional",
                isNeutral = otherUri == null,
                onClick = { otherLauncher.launch("*/*") }
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    if (isUploading) return@Button
                    isUploading = true
                    coroutineScope.launch {
                        try {
                            // Upload Passport
                            if (passportUri != null) {
                                val base64 = uriToBase64(passportUri!!)
                                if (base64 != null) {
                                    val res = RetrofitClient.api.uploadDocument(
                                        request = UploadDocumentRequestDto("passport", base64)
                                    )
                                    if (res.extractedData != null) {
                                        val mapData = res.extractedData
                                        mapData["fullName"]?.let { UserProfileStore.fullName = it }
                                        mapData["dob"]?.let { UserProfileStore.dob = it }
                                        mapData["passportNumber"]?.let { UserProfileStore.passportNumber = it }
                                        mapData["nationality"]?.let { UserProfileStore.nationality = it }
                                    }
                                }
                            }
                            
                            // Upload Degree
                            if (degreeUri != null) {
                                val base64 = uriToBase64(degreeUri!!)
                                if (base64 != null) {
                                    val res = RetrofitClient.api.uploadDocument(
                                        request = UploadDocumentRequestDto("degree", base64)
                                    )
                                    if (res.extractedData != null) {
                                        val mapData = res.extractedData
                                        mapData["degree"]?.let { UserProfileStore.degree = it }
                                        mapData["institution"]?.let { UserProfileStore.university = it }
                                        mapData["graduationYear"]?.let { UserProfileStore.graduationYear = it }
                                    }
                                }
                            }

                            // Upload Language
                            if (languageUri != null) {
                                val base64 = uriToBase64(languageUri!!)
                                if (base64 != null) {
                                    val res = RetrofitClient.api.uploadDocument(
                                        request = UploadDocumentRequestDto("language", base64)
                                    )
                                    if (res.extractedData != null) {
                                        val mapData = res.extractedData
                                        mapData["proficiency"]?.let { UserProfileStore.englishLevel = it }
                                    }
                                }
                            }
                            
                            onNext()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            onNext() // proceed anyway on error
                        } finally {
                            isUploading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extracting Profile Data...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                } else {
                    Text("Complete Onboarding \u2192", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun DocumentUploadCard(
    title: String,
    isUploaded: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusText: String = if (isUploaded) "Uploaded" else "Not Uploaded",
    isError: Boolean = false,
    isNeutral: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = if (isError) Color(0xFFFFF0F0) else if (isNeutral) Color(0xFFF5F7FA) else Color(0xFFF0F5FF)
    val iconColor = if (isError) Color(0xFFFF5252) else if (isNeutral) Color(0xFF9E9E9E) else MaterialTheme.colorScheme.primary
    
    val statusBgColor = if (isUploaded) Color(0xFFE8F5E9) else if (isError) Color(0xFFFFF0F0) else Color(0xFFF5F7FA)
    val statusTextColor = if (isUploaded) Color(0xFF4CAF50) else if (isError) Color(0xFFFF5252) else Color(0xFF9E9E9E)
    val statusIcon = if (isUploaded) Icons.Default.CheckCircle else if (isError) Icons.Default.Cancel else null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            
            // Status pill
            Row(
                modifier = Modifier
                    .background(statusBgColor, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (statusIcon != null) {
                    Icon(statusIcon, contentDescription = null, tint = statusTextColor, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(statusText, style = MaterialTheme.typography.labelSmall, color = statusTextColor, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFB0BEC5), modifier = Modifier.size(20.dp))
        }
    }
}
