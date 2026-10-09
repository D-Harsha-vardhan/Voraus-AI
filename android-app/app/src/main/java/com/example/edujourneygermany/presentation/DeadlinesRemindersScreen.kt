package com.example.edujourneygermany.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DeadlineItem(
    val id: String,
    val title: String,
    val dateText: String,
    val highlightYear: String? = null,
    val subtitle: String,
    val badgeText: String,
    val badgeBgColor: Color,
    val badgeTextColor: Color,
    val category: String, // "Applications", "Documents", "Visa"
    val isUrgent: Boolean = false,
    val targetRoute: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlinesRemindersScreen(
    onBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Applications", "Documents", "Visa")

    val allDeadlines = remember {
        listOf(
            DeadlineItem(
                id = "tum_deadline",
                title = "TUM Application Deadline",
                dateText = "15 Nov ",
                highlightYear = "2025",
                subtitle = "Winter Semester 2025",
                badgeText = "In 38 days",
                badgeBgColor = Color(0xFFFEE2E2),
                badgeTextColor = Color(0xFFDC2626),
                category = "Applications",
                isUrgent = true,
                targetRoute = "opportunities"
            ),
            DeadlineItem(
                id = "aps_cert",
                title = "APS Certificate",
                dateText = "30 Nov 2025",
                subtitle = "Required for most German universities",
                badgeText = "In 53 days",
                badgeBgColor = Color(0xFFEFF6FF),
                badgeTextColor = Color(0xFF2563EB),
                category = "Documents",
                targetRoute = "documents"
            ),
            DeadlineItem(
                id = "health_insurance",
                title = "Health Insurance Confirmation",
                dateText = "10 Dec 2025",
                subtitle = "For visa submission",
                badgeText = "In 63 days",
                badgeBgColor = Color(0xFFECFDF5),
                badgeTextColor = Color(0xFF059669),
                category = "Visa",
                targetRoute = "qualification"
            ),
            DeadlineItem(
                id = "visa_appointment",
                title = "Visa Appointment",
                dateText = "Jan 2026",
                subtitle = "Book at German Consulate",
                badgeText = "In 3 months",
                badgeBgColor = Color(0xFFEFF6FF),
                badgeTextColor = Color(0xFF2563EB),
                category = "Visa",
                targetRoute = "qualification"
            ),
            DeadlineItem(
                id = "blocked_account",
                title = "Blocked Account Validity",
                dateText = "Dec 2026",
                subtitle = "Ensure sufficient balance",
                badgeText = "In 1 year",
                badgeBgColor = Color(0xFFF5F3FF),
                badgeTextColor = Color(0xFF7C3AED),
                category = "Visa",
                targetRoute = "qualification"
            )
        )
    }

    val filteredDeadlines = remember(selectedFilter) {
        if (selectedFilter == "All") allDeadlines
        else allDeadlines.filter { it.category.equals(selectedFilter, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Deadlines & Reminders",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color(0xFF0F172A)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Calendar Header Card ("Stay on track!")
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF3FF)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFD6E4FF)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Calendar",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Stay on track!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "We'll remind you about important dates and upcoming tasks.",
                                fontSize = 12.5.sp,
                                color = Color(0xFF475569),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // 2. Filter Pills Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.forEach { filterName ->
                        val isSelected = selectedFilter == filterName
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedFilter = filterName },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF0052CC) else Color(0xFFEFF3F8)
                        ) {
                            Text(
                                text = filterName,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569)
                            )
                        }
                    }
                }
            }

            // 3. Timeline Items with Connected Line
            itemsIndexed(filteredDeadlines) { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Timeline Column (Node + Vertical Line)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(32.dp)
                    ) {
                        // Node Circle
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(
                                    width = 2.dp,
                                    color = if (item.isUrgent) Color(0xFFEF4444) else Color(0xFF2563EB),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.isUrgent) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2563EB))
                                )
                            }
                        }

                        // Connecting vertical line (for all except last)
                        if (index < filteredDeadlines.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(90.dp)
                                    .background(Color(0xFFE2E8F0))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Right Deadline Card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                item.targetRoute?.let { onNavigateToRoute(it) }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isUrgent) Color(0xFFFFF7F7) else Color.White
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (item.isUrgent) Color(0xFFFEE2E2) else Color(0xFFF1F5F9)
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                
                                // Formatted date text
                                if (item.highlightYear != null) {
                                    Text(
                                        text = buildAnnotatedString {
                                            append(item.dateText)
                                            withStyle(SpanStyle(color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)) {
                                                append(item.highlightYear)
                                            }
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )
                                } else {
                                    Text(
                                        text = item.dateText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF1E293B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = item.subtitle,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            // Right Badge Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = item.badgeBgColor
                            ) {
                                Text(
                                    text = item.badgeText,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = item.badgeTextColor
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 4. Pending Tasks Alert Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRoute("documents") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBFDBFE)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDBEAFE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Pending Tasks",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "You have 2 pending tasks",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Upload your APS certificate and confirm your health insurance.",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
