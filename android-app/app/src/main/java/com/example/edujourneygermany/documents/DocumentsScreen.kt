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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(onNavigateToExtraction: (String, String) -> Unit) {
    val selectedDocType = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && selectedDocType.value != null) {
            onNavigateToExtraction(selectedDocType.value!!, uri.toString())
        }
        selectedDocType.value = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documents", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
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
                item { DocumentItem("CV / Resume", "Verified") { selectedDocType.value = "CV"; launcher.launch("*/*") } }
                item { DocumentItem("Degree Certificate", "Processing") { selectedDocType.value = "Degree Certificate"; launcher.launch("*/*") } }
                item { DocumentItem("Marksheet", "Verified") { selectedDocType.value = "Marksheet"; launcher.launch("*/*") } }
                item { DocumentItem("IELTS Certificate", "Missing") { selectedDocType.value = "IELTS Certificate"; launcher.launch("*/*") } }
                item { DocumentItem("German Certificate", "Not Uploaded") { selectedDocType.value = "German Certificate"; launcher.launch("*/*") } }
                item { DocumentItem("Experience Letter", "Not Uploaded") { selectedDocType.value = "Experience Letter"; launcher.launch("*/*") } }
                item { DocumentItem("Other Documents", "Not Uploaded") { selectedDocType.value = "Other Documents"; launcher.launch("*/*") } }
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
