package com.example.edujourneygermany.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FloatingNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Modern Floating Navigation Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Home
                NavBarItem(
                    selectedIcon = Icons.Default.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    label = "Home",
                    isSelected = currentRoute == "home",
                    onClick = { onNavigate("home") },
                    modifier = Modifier.weight(1f)
                )

                // 2. Universities
                NavBarItem(
                    selectedIcon = Icons.Default.AccountBalance,
                    unselectedIcon = Icons.Outlined.AccountBalance,
                    label = "Universities",
                    isSelected = currentRoute == "opportunities",
                    onClick = { onNavigate("opportunities") },
                    modifier = Modifier.weight(1.15f)
                )

                // 3. Applications / Deadlines
                NavBarItem(
                    selectedIcon = Icons.AutoMirrored.Filled.Assignment,
                    unselectedIcon = Icons.AutoMirrored.Outlined.Assignment,
                    label = "Applications",
                    isSelected = currentRoute == "deadlines",
                    onClick = { onNavigate("deadlines") },
                    modifier = Modifier.weight(1.15f)
                )

                // 4. Chat
                NavBarItem(
                    selectedIcon = Icons.Default.ChatBubble,
                    unselectedIcon = Icons.Outlined.ChatBubbleOutline,
                    label = "Chat",
                    isSelected = currentRoute == "advisor",
                    onClick = { onNavigate("advisor") },
                    modifier = Modifier.weight(1f)
                )

                // 5. Profile
                NavBarItem(
                    selectedIcon = Icons.Default.Person,
                    unselectedIcon = Icons.Outlined.PersonOutline,
                    label = "Profile",
                    isSelected = currentRoute == "profile",
                    onClick = { onNavigate("profile") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NavBarItem(
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(0xFF1C64F2)
    val inactiveColor = Color(0xFF8A93A6)
    val color = if (isSelected) activeColor else inactiveColor
    val icon = if (isSelected) selectedIcon else unselectedIcon

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
