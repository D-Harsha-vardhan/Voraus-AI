package com.example.edujourneygermany.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
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
fun NotificationsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                NotificationItem(
                    title = "Your degree document has been processed",
                    time = "2 hours ago",
                    icon = Icons.Default.Description,
                    iconTint = Color(0xFF1E88E5) // Blue
                )
            }
            item { Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)) }
            
            item {
                NotificationItem(
                    title = "Profile is 92% complete",
                    time = "3 hours ago",
                    icon = Icons.Default.Person,
                    iconTint = Color(0xFF1E88E5) // Blue
                )
            }
            item { Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)) }
            
            item {
                NotificationItem(
                    title = "APS Certificate is missing",
                    time = "5 hours ago",
                    icon = Icons.Default.Error,
                    iconTint = Color(0xFFE53935) // Red
                )
            }
            item { Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)) }
            
            item {
                NotificationItem(
                    title = "New programs matching your profile are available",
                    time = "1 day ago",
                    icon = Icons.Default.ListAlt,
                    iconTint = Color(0xFF4CAF50) // Green
                )
            }
            item { Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)) }
            
            item {
                NotificationItem(
                    title = "Consultant requested additional information",
                    time = "1 day ago",
                    icon = Icons.Default.Person,
                    iconTint = Color(0xFFFF9800) // Orange
                )
            }
            item { Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)) }
            
            item {
                NotificationItem(
                    title = "Your IELTS certificate is verified",
                    time = "2 days ago",
                    icon = Icons.Default.FactCheck,
                    iconTint = Color(0xFF4CAF50) // Green
                )
            }
            item { Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)) }
        }
    }
}

@Composable
fun NotificationItem(
    title: String,
    time: String,
    icon: ImageVector,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(time, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = "Details",
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}
