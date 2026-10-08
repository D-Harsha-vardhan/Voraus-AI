package com.example.edujourneygermany.documents

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.edujourneygermany.auth.OcrHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtractionReviewScreen(
    documentType: String,
    imageUri: Uri?,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isLoading by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }
    
    // Extracted Fields Map
    var extractedData by remember { mutableStateOf(mapOf<String, String>()) }

    LaunchedEffect(imageUri, documentType) {
        if (imageUri != null) {
            val cachedData = com.example.edujourneygermany.auth.DocumentMemory.extractedDataCache[documentType]
            if (cachedData != null) {
                extractedData = cachedData
                isLoading = false
            } else {
                val data = OcrHelper.extractData(context, imageUri, documentType)
                extractedData = data
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    val securityWarning by remember(extractedData, documentType) {
        derivedStateOf {
            val baseName = com.example.edujourneygermany.data.UserProfileStore.fullName.trim()
            
            if (documentType == "Passport") {
                val extractedName = extractedData["full_name"]?.trim() ?: ""
                if (extractedName.isNotBlank() && baseName.isNotBlank() && !extractedName.equals(baseName, ignoreCase = true)) {
                    return@derivedStateOf "Security Alert: Name on Passport ($extractedName) does not match your profile name ($baseName)."
                }
                
                val expiry = extractedData["date_of_expiry"]?.trim() ?: ""
                if (expiry.isNotBlank()) {
                    val parts = expiry.split(Regex("[^0-9]"))
                    val yearStr = parts.find { it.length == 4 }
                    if (yearStr != null && yearStr.toInt() < java.time.LocalDate.now().year) {
                        return@derivedStateOf "Security Alert: Passport appears to be expired (Year: $yearStr). Please provide a valid passport."
                    }
                }
            } else if (documentType.contains("Language") || documentType.contains("Certificate") && !documentType.contains("Degree")) {
                val extractedName = extractedData["candidate_name"]?.trim() ?: ""
                if (extractedName.isNotBlank() && baseName.isNotBlank() && !extractedName.equals(baseName, ignoreCase = true)) {
                    return@derivedStateOf "Security Alert: Candidate Name ($extractedName) does not match your profile name ($baseName)."
                }
                
                val testDate = extractedData["test_date"]?.trim() ?: ""
                if (testDate.isNotBlank()) {
                    val parts = testDate.split(Regex("[^0-9]"))
                    val yearStr = parts.find { it.length == 4 }
                    if (yearStr != null && java.time.LocalDate.now().year - yearStr.toInt() > 2) {
                        return@derivedStateOf "Warning: This certificate appears to be more than 2 years old (Year: $yearStr) and may not be accepted by universities."
                    }
                }
            } else if (documentType.contains("Degree") || documentType == "Marksheet") {
                val extractedName = extractedData["student_name"]?.trim() ?: ""
                if (extractedName.isNotBlank() && baseName.isNotBlank() && !extractedName.equals(baseName, ignoreCase = true)) {
                    return@derivedStateOf "Security Alert: Student Name ($extractedName) does not match your profile name ($baseName)."
                }
                
                val issueDate = extractedData["date_of_issue"]?.trim() ?: ""
                if (issueDate.isNotBlank()) {
                    val parts = issueDate.split(Regex("[^0-9]"))
                    val yearStr = parts.find { it.length == 4 }
                    if (yearStr != null && yearStr.toInt() > java.time.LocalDate.now().year) {
                        return@derivedStateOf "Security Alert: Issue date ($yearStr) cannot be in the future."
                    }
                }
            } else if (documentType == "Resume" || documentType == "CV" || documentType == "Experience Letter") {
                val extractedName = extractedData["employee_name"]?.trim() ?: ""
                if (extractedName.isNotBlank() && baseName.isNotBlank() && !extractedName.equals(baseName, ignoreCase = true)) {
                    return@derivedStateOf "Security Alert: Name on document ($extractedName) does not match your profile name ($baseName)."
                }
            }
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Extracted Data", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Document Preview",
                    modifier = Modifier.fillMaxWidth().height(200.dp).padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "We used AI to extract information from your $documentType. Please review and edit if necessary.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (securityWarning != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Security Alert", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                securityWarning!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Extracted Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                if (extractedData.isNotEmpty()) {
                    extractedData.forEach { (key, value) ->
                        val formattedLabel = key.split("_").joinToString(" ") { 
                            it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(java.util.Locale.getDefault()) else char.toString() } 
                        }
                        ExtractionField(
                            label = formattedLabel,
                            value = value,
                            onValueChange = { newValue ->
                                extractedData = extractedData.toMutableMap().apply { put(key, newValue) }
                            }
                        )
                    }
                } else {
                    Text("No specific fields could be extracted.", modifier = Modifier.padding(16.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        isUploading = true
                        // Save manual edits to memory so they aren't lost if the user comes back
                        com.example.edujourneygermany.auth.DocumentMemory.extractedDataCache[documentType] = extractedData
                        
                        scope.launch {
                            if (imageUri != null) {
                                OcrHelper.uploadDocumentAndData(context, imageUri, documentType, extractedData)
                            }
                            isUploading = false
                            onConfirm()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isUploading && securityWarning == null
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Confirm and Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ExtractionField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Provenance Chip
        Surface(
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "AI-extracted", 
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
