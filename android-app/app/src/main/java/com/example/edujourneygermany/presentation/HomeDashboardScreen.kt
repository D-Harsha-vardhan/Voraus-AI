package com.example.edujourneygermany.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edujourneygermany.data.UserProfileStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    onNavigate: (String) -> Unit
) {
    // Professional Executive Theme Tokens
    val primaryRoyalBlue = Color(0xFF1D4ED8)
    val accentSapphire = Color(0xFF2563EB)
    val pillBg = Color(0xFFEFF6FF)
    val cardBorder = Color(0xFFE2E8F0)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    // Warm Amber Priority Alert Colors
    val amberWarning = Color(0xFFD97706)
    val amberHeading = Color(0xFF92400E)
    val amberBg = Color(0xFFFFFBEB)
    val amberBorder = Color(0xFFFDE68A)

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(bottom = 135.dp) // Generous scroll padding for the 82dp professional floating nav bar
        ) {
            // 1. Executive Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Avatar Squircle
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(pillBg)
                            .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = primaryRoyalBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "WELCOME BACK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (UserProfileStore.fullName.isNotEmpty()) "Hello, ${UserProfileStore.fullName}!" else "Hello, Kartikey Sharma!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = textPrimary
                        )
                    }
                }

                // Notifications Button with Unread Badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, cardBorder, CircleShape)
                        .clickable { onNavigate("notifications") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(22.dp)
                    )
                    // Unread badge dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                }
            }

            // 2. Executive Hero Progress Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(pillBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Progress",
                                    tint = primaryRoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Profile Completion",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = pillBg,
                            border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                        ) {
                            Text(
                                text = "40% Done",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryRoyalBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { 0.4f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = accentSapphire,
                        trackColor = Color(0xFFF1F5F9)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step 2 of 5 Milestones Ready",
                            fontSize = 12.5.sp,
                            color = textSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        // Pill Action Button
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = primaryRoyalBlue,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onNavigate("profile") }
                        ) {
                            Text(
                                text = "Complete Profile →",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Your Goal Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(pillBg)
                            .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Goal",
                            tint = primaryRoyalBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "TARGET PROGRAM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Master's in Germany",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "🇩🇪 Winter Intake • English Taught",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentSapphire
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Action Required Alert Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = amberBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, amberBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alert",
                                    tint = amberWarning,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "PRIORITY ACTION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Upload APS Certificate",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = amberHeading
                                )
                            }
                        }

                        // Upload Pill Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = amberWarning,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigate("documents") }
                        ) {
                            Text(
                                text = "Upload ↗",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Mandatory verification required by German universities and the visa consulate.",
                        fontSize = 12.5.sp,
                        color = Color(0xFF78350F),
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Quick Actions Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Actions",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Text(
                    text = "Explore All",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryRoyalBlue,
                    modifier = Modifier.clickable { onNavigate("opportunities") }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Professional 3x3 Grid using the SAME ICONS
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Person,
                        title = "Profile",
                        onClick = { onNavigate("profile") }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Folder,
                        title = "Documents",
                        onClick = { onNavigate("documents") }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Checklist,
                        title = "Finance / Visa",
                        onClick = { onNavigate("qualification") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.School,
                        title = "Universities",
                        onClick = { onNavigate("opportunities") }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ChatBubble,
                        title = "AI Advisor",
                        onClick = { onNavigate("advisor") }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Description,
                        title = "CV Generator",
                        onClick = { onNavigate("cv") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Map,
                        title = "Berlin Map",
                        onClick = { onNavigate("heatmap") }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        title = "Deadlines",
                        onClick = { onNavigate("deadlines") }
                    )
                    QuickActionCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.VerifiedUser,
                        title = "Verification",
                        onClick = { onNavigate("verification") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 6. Your Journey Roadmap Section
            Text(
                text = "Your Path to Germany",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    JourneyStep(
                        title = "Upload Documents",
                        subtitle = "CV, Degree, IELTS uploaded",
                        isCompleted = true,
                        isLast = false
                    )
                    JourneyStep(
                        title = "Profile Verification",
                        subtitle = "Advisor agent reviewing credentials",
                        isCompleted = false,
                        isActive = true,
                        isLast = false,
                        onClick = { onNavigate("verification") }
                    )
                    JourneyStep(
                        title = "Video Introduction",
                        subtitle = "Record your 2-minute intro",
                        isCompleted = false,
                        isActive = false,
                        isLast = false,
                        onClick = { onNavigate("video_intro") }
                    )
                    JourneyStep(
                        title = "APS Setup",
                        subtitle = "Apply for official APS certificate",
                        isCompleted = false,
                        isLast = false,
                        onClick = { onNavigate("documents") }
                    )
                    JourneyStep(
                        title = "University Applications",
                        subtitle = "Submit to matched public universities",
                        isCompleted = false,
                        isLast = true,
                        onClick = { onNavigate("opportunities") }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val primaryRoyalBlue = Color(0xFF1D4ED8)
    val pillBg = Color(0xFFEFF6FF)
    val cardBorder = Color(0xFFE2E8F0)

    Card(
        onClick = onClick,
        modifier = modifier.height(108.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(pillBg)
                    .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = primaryRoyalBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun JourneyStep(
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    isActive: Boolean = false,
    isLast: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val primaryColor = Color(0xFF1D4ED8)
    val successColor = Color(0xFF10B981)
    val inactiveColor = Color(0xFFE2E8F0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp)
    ) {
        // Timeline graphic
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> successColor
                            isActive -> primaryColor
                            else -> Color(0xFFF1F5F9)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFCBD5E1))
                    )
                }
            }

            if (!isLast) {
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                ) {
                    drawLine(
                        color = if (isCompleted) successColor.copy(alpha = 0.5f) else inactiveColor,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Content
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive || isCompleted) Color(0xFF0F172A) else Color(0xFF64748B)
                )
                if (isActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFDBEAFE))
                    ) {
                        Text(
                            text = "In Review",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}
