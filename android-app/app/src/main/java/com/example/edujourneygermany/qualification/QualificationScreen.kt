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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Qualification", fontWeight = FontWeight.Bold) },
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
                            progress = 0.68f,
                            modifier = Modifier.size(64.dp),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = "68%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            "Partially Qualified",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "You meet 7 out of 10 requirements",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Requirements",
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
                    item { RequirementItem("Bachelor's Degree", "Met", Icons.Default.Check, Color(0xFF4CAF50), Icons.Default.CheckCircle, Color(0xFFE0E0E0)) }
                    item { Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)) }
                    item { RequirementItem("IELTS (6.5+)", "Met", Icons.Default.Check, Color(0xFF4CAF50), Icons.Default.CheckCircle, Color(0xFF4CAF50)) }
                    item { Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)) }
                    item { RequirementItem("German B1", "Missing", Icons.Default.Warning, Color(0xFFFF9800), Icons.Default.Warning, Color(0xFFFF9800)) }
                    item { Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)) }
                    item { RequirementItem("APS Certificate", "Missing", Icons.Default.Clear, Color(0xFFE53935), Icons.Default.Clear, Color(0xFFE53935)) }
                    item { Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)) }
                    item { RequirementItem("Work Experience", "Met", Icons.Default.Check, Color(0xFF4CAF50), Icons.Default.CheckCircle, Color(0xFF81C784)) }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("View Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp)) // padding for bottom nav
        }
    }
}

@Composable
fun RequirementItem(
    title: String, 
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
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(statusIcon, contentDescription = status, tint = statusColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(status, style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.Bold)
        }
    }
}
