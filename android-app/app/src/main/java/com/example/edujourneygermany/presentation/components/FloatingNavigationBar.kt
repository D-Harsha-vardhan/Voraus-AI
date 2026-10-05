package com.example.edujourneygermany.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FloatingNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color(0xFF007AFF), shape = RoundedCornerShape(36.dp))
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Default.Home,
                isSelected = currentRoute == "home",
                onClick = { onNavigate("home") }
            )
            NavBarItem(
                icon = Icons.Default.Search,
                isSelected = currentRoute == "opportunities",
                onClick = { onNavigate("opportunities") }
            )
            NavBarItem(
                icon = Icons.Default.Add,
                isSelected = false,
                isCenterPlus = true,
                onClick = { onNavigate("documents") }
            )
            NavBarItem(
                icon = Icons.Default.MailOutline,
                isSelected = currentRoute == "advisor",
                onClick = { onNavigate("advisor") }
            )
            NavBarItem(
                icon = Icons.Default.PersonOutline,
                isSelected = currentRoute == "profile",
                onClick = { onNavigate("profile") }
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: ImageVector,
    isSelected: Boolean,
    isCenterPlus: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> Color.White
                    isCenterPlus -> Color(0xFF3395FF) // slightly lighter blue
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = when {
                isSelected -> Color(0xFF007AFF)
                else -> Color.White
            },
            modifier = Modifier.size(28.dp)
        )
    }
}
