package com.example.edujourneygermany.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.edujourneygermany.data.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(onNavigateToExtraction: (String, String) -> Unit) {
    val selectedDocType = remember { mutableStateOf<String?>(null) }
    var viewingImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingImage by remember { mutableStateOf(false) }
    
    // Dynamic statuses
    val docStatuses = remember { mutableStateMapOf<String, String>(
        "CV / Resume" to "Loading...",
        "Degree Certificate" to "Loading...",
        "Marksheet" to "Loading...",
        "Passport" to "Loading...",
        "IELTS Certificate" to "Loading...",
        "German Certificate" to "Loading..."
    ) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        try {
            val bucket = Supabase.client.storage["user_documents"]
            val email = Supabase.client.auth.currentUserOrNull()?.email ?: com.example.edujourneygermany.auth.UserSession.userEmail
            val allNames = if (email.isNotBlank()) {
                val files = bucket.list()
                files.map { it.name.lowercase() }.filter { it.startsWith(email.lowercase()) }
            } else emptyList()
            
            docStatuses.keys.toList().forEach { docType ->
                val searchKeys = when(docType) {
                    "CV / Resume" -> listOf("_cv", "_resume")
                    "Degree Certificate" -> listOf("_degree")
                    "German Certificate" -> listOf("_german")
                    "IELTS Certificate" -> listOf("_ielts")
                    else -> listOf("_${docType.lowercase()}")
                }
                
                val exists = allNames.any { name -> searchKeys.any { key -> name.contains(key) } }
                docStatuses[docType] = if (exists) "Verified" else "Not Uploaded"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback
            docStatuses.keys.toList().forEach { docStatuses[it] = "Not Uploaded" }
        }
    }
    
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && selectedDocType.value != null) {
            onNavigateToExtraction(selectedDocType.value!!, uri.toString())
        }
        selectedDocType.value = null
    }

    val handleDocAction: (String, String) -> Unit = { docType, status ->
        if (status == "Missing" || status == "Not Uploaded") {
            selectedDocType.value = docType
            launcher.launch("*/*")
        } else {
            // View document
            scope.launch {
                isLoadingImage = true
                try {
                    val bucket = Supabase.client.storage["user_documents"]
                    val email = Supabase.client.auth.currentUserOrNull()?.email ?: com.example.edujourneygermany.auth.UserSession.userEmail
                    
                    val searchKeys = when(docType) {
                        "CV / Resume" -> listOf("_cv", "_resume")
                        "Degree Certificate" -> listOf("_degree")
                        "German Certificate" -> listOf("_german")
                        "IELTS Certificate" -> listOf("_ielts")
                        else -> listOf("_${docType.lowercase()}")
                    }
                    
                    val files = bucket.list()
                    val docFiles = files.filter { file ->
                        val lowerName = file.name.lowercase()
                        searchKeys.any { key -> lowerName.contains(key) } && lowerName.startsWith(email.lowercase())
                    }
                    
                    android.util.Log.d("DocumentsScreen", "Found ${docFiles.size} files for $docType")
                    if (docFiles.isNotEmpty()) {
                        val latestFile = docFiles.maxByOrNull { it.createdAt?.toString() ?: "" } ?: docFiles.first()
                        viewingImageUrl = bucket.publicUrl(latestFile.name)
                    } else {
                        // Fallback if not found but marked as verified (mock data)
                        // In a real app, we'd show a "Not Found" message
                        viewingImageUrl = "not_found" 
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    viewingImageUrl = "error"
                } finally {
                    isLoadingImage = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documents", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f).padding(bottom = 100.dp) // Nav bar padding
                ) {
                    item { DocumentItem("CV / Resume", docStatuses["CV / Resume"] ?: "Not Uploaded") { handleDocAction("CV / Resume", docStatuses["CV / Resume"] ?: "Not Uploaded") } }
                    item { DocumentItem("Degree Certificate", docStatuses["Degree Certificate"] ?: "Not Uploaded") { handleDocAction("Degree Certificate", docStatuses["Degree Certificate"] ?: "Not Uploaded") } }
                    item { DocumentItem("Marksheet", docStatuses["Marksheet"] ?: "Not Uploaded") { handleDocAction("Marksheet", docStatuses["Marksheet"] ?: "Not Uploaded") } }
                    item { DocumentItem("Passport", docStatuses["Passport"] ?: "Not Uploaded") { handleDocAction("Passport", docStatuses["Passport"] ?: "Not Uploaded") } }
                    item { DocumentItem("IELTS Certificate", docStatuses["IELTS Certificate"] ?: "Not Uploaded") { handleDocAction("IELTS Certificate", docStatuses["IELTS Certificate"] ?: "Not Uploaded") } }
                    item { DocumentItem("German Certificate", docStatuses["German Certificate"] ?: "Not Uploaded") { handleDocAction("German Certificate", docStatuses["German Certificate"] ?: "Not Uploaded") } }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { selectedDocType.value = "Other Documents"; launcher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Upload Document", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            if (isLoadingImage) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
    // Image Viewer Dialog
    if (viewingImageUrl != null) {
        Dialog(
            onDismissRequest = { viewingImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                if (viewingImageUrl == "not_found" || viewingImageUrl == "error") {
                    Text(
                        text = "Document image not found in storage.",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    AsyncImage(
                        model = viewingImageUrl,
                        contentDescription = "Document Image",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                IconButton(
                    onClick = { viewingImageUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun DocumentItem(title: String, status: String, onActionClick: () -> Unit = {}) {
    val statusColor = when (status) {
        "Verified" -> Color(0xFF4CAF50)
        "Processing", "Needs Review" -> Color(0xFFFF9800)
        "Missing" -> Color(0xFFE53935)
        else -> MaterialTheme.colorScheme.primary
    }

    val icon = when (status) {
        "Verified" -> Icons.Default.CheckCircle
        "Processing", "Needs Review" -> Icons.Default.Warning
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = statusColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = status, tint = statusColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(status, style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.Medium)
                }
            }
            OutlinedButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(if (status == "Missing" || status == "Not Uploaded") "Upload" else "View", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
