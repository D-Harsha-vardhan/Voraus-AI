package com.example.edujourneygermany.qualification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Warning
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
fun QualificationScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val visaRequirements = androidx.compose.runtime.remember {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val reader = java.io.BufferedReader(java.io.InputStreamReader(context.assets.open("germany_csv/42_visa_documents_checklist.csv")))
            reader.readLine() // skip header
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line.isNullOrBlank()) continue
                // CSV columns: order,document,app_group,required,what_to_prepare,tip
                // Since some columns have quotes (like what_to_prepare), we need a basic regex split
                val parts = line!!.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).map { it.trim('\"', ' ') }
                if (parts.size >= 5) {
                    list.add(Pair(parts[1], parts[4]))
                }
            }
            reader.close()
        } catch (e: Exception) {}
        list
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance/Visa Advisor", fontWeight = FontWeight.Bold) },
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
                .padding(bottom = 100.dp) // Nav bar padding
        ) {
            // Overall Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)), // Light yellow
                border = BorderStroke(1.dp, Color(0xFFF0E5C8))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = 0.20f,
                            modifier = Modifier.size(64.dp),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = "20%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            "Visa Readiness",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "You have prepared 2 out of ${visaRequirements.size} documents",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Document Checklist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    if (visaRequirements.isEmpty()) {
                        item {
                            Text("No requirements found.", modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(visaRequirements.size) { index ->
                            val req = visaRequirements[index]
                            // For mock purposes, mark the first two as Met, others as Pending
                            val isMet = index < 2
                            val status = if (isMet) "Prepared" else "Pending"
                            val statusIcon = if (isMet) Icons.Default.Check else Icons.Default.Warning
                            val statusColor = if (isMet) Color(0xFF4CAF50) else Color(0xFFFF9800)
                            val leftIcon = if (isMet) Icons.Default.CheckCircle else Icons.Default.Warning

                            RequirementItem(
                                title = req.first,
                                subtitle = req.second,
                                status = status,
                                statusIcon = statusIcon,
                                statusColor = statusColor,
                                leftIcon = leftIcon,
                                leftIconColor = statusColor
                            )
                            if (index < visaRequirements.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("Generate Document Checklist", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp)) // padding for bottom nav
        }
    }
}

@Composable
fun RequirementItem(
    title: String, 
    subtitle: String,
    status: String, 
    statusIcon: ImageVector, 
    statusColor: Color, 
    leftIcon: ImageVector, 
    leftIconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(leftIcon, contentDescription = null, tint = leftIconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(statusIcon, contentDescription = status, tint = statusColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(status, style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.Bold)
        }
    }
}
