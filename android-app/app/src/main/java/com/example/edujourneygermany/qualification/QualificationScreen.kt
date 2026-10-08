package com.example.edujourneygermany.qualification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.auth.auth
import android.content.Intent
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

data class SimpleUni(val name: String, val shortName: String, val type: String = "Public", val city: String = "Germany", val applyLink: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualificationScreen(onBackClick: () -> Unit = {}, onNavigateToDocuments: () -> Unit = {}) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Finance, 1: Visa
    var searchQuery by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedUni by remember { mutableStateOf<SimpleUni?>(null) }
    var visaDetailMode by remember { mutableStateOf(false) } // Toggle for Visa details view
    var financeDetailMode by remember { mutableStateOf(false) } // Toggle for Finance details view
    var selectedCourse by remember { mutableStateOf("") }
    var selectedIntake by remember { mutableStateOf("") }
    var isBlockedAccountVerified by remember { mutableStateOf(false) }
    var isPassportVerified by remember { mutableStateOf(false) }
    var isAdmissionVerified by remember { mutableStateOf(false) }
    var isHealthVerified by remember { mutableStateOf(false) }
    var isApsVerified by remember { mutableStateOf(false) }
    var isCvVerified by remember { mutableStateOf(false) }
    var isTranscriptsVerified by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val bucket = io.github.jan.supabase.SupabaseClient.let { com.example.edujourneygermany.data.Supabase.client.storage["user_documents"] }
            val email = com.example.edujourneygermany.data.Supabase.client.auth.currentUserOrNull()?.email ?: ""
            val files = bucket.list()
            val allNames = files.map { it.name.lowercase() }.filter { it.startsWith(email.lowercase()) }
            isBlockedAccountVerified = allNames.any { it.contains("_blocked") }
            isPassportVerified = allNames.any { it.contains("_passport") }
            isAdmissionVerified = allNames.any { it.contains("_admission") }
            isHealthVerified = allNames.any { it.contains("_health") }
            isApsVerified = allNames.any { it.contains("_aps") }
            isCvVerified = allNames.any { it.contains("_cv") || it.contains("_resume") }
            isTranscriptsVerified = allNames.any { it.contains("_transcripts") || it.contains("_degree") }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Load universities dynamically
    val allUnis = remember {
        val list = mutableListOf<SimpleUni>()
        try {
            val reader = BufferedReader(InputStreamReader(context.assets.open("germany_csv/24_university_profiles.csv")))
            reader.readLine() // skip header
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line.isNullOrBlank()) continue
                val parts = line!!.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).map { it.trim('\"', ' ') }
                if (parts.size >= 5) {
                    val fullName = parts[1] // university is at index 1
                    val cityVal = parts[2] // city is at index 2
                    val type = parts[4] // type is at index 4 (TU9, Public, etc)
                    val applyLink = if (parts.size > 8) parts[8] else ""
                    
                    var shortName = fullName.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("").take(4)
                    if (fullName.contains("(TUM)")) shortName = "TUM"
                    if (fullName.contains("RWTH")) shortName = "RWTH"
                    if (fullName.contains("KIT")) shortName = "KIT"
                    
                    list.add(SimpleUni(fullName, shortName, type, cityVal, applyLink))
                }
            }
            reader.close()
            if (list.isEmpty()) {
                list.addAll(listOf(
                    SimpleUni("Technical University of Munich (TUM)", "TUM"),
                    SimpleUni("RWTH Aachen University", "RWTH"),
                    SimpleUni("Karlsruhe Institute of Technology (KIT)", "KIT")
                ))
            }
        } catch (e: Exception) {
            list.addAll(listOf(
                SimpleUni("Technical University of Munich (TUM)", "TUM"),
                SimpleUni("RWTH Aachen University", "RWTH"),
                SimpleUni("Karlsruhe Institute of Technology (KIT)", "KIT")
            ))
        }
        list
    }

    val popularUnis = allUnis.take(4)
    val filteredUnis = allUnis.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    val titleText = if (financeDetailMode) "${selectedUni?.shortName ?: "TUM"} – Finance Details" else "Finance & Visa Agent"
                    Text(titleText, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (financeDetailMode) {
                            financeDetailMode = false
                        } else if (visaDetailMode) {
                            visaDetailMode = false
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 100.dp)
        ) {
            
            if (visaDetailMode && selectedTab == 1) {
                VisaDetailView(
                    isBlockedAccountVerified = isBlockedAccountVerified,
                    isPassportVerified = isPassportVerified,
                    isAdmissionVerified = isAdmissionVerified,
                    isHealthVerified = isHealthVerified,
                    isApsVerified = isApsVerified,
                    isCvVerified = isCvVerified,
                    isTranscriptsVerified = isTranscriptsVerified,
                    onBack = { visaDetailMode = false },
                    onNavigateToDocuments = onNavigateToDocuments
                )
                return@Column
            }

            if (financeDetailMode && selectedTab == 0 && selectedUni != null) {
                FinanceDetailView(uni = selectedUni!!, course = selectedCourse, intake = selectedIntake, onBack = { financeDetailMode = false })
                return@Column
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Segmented Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp)
            ) {
                SegmentButton(
                    text = "Finance",
                    isSelected = selectedTab == 0,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = 0 }
                SegmentButton(
                    text = "Visa",
                    isSelected = selectedTab == 1,
                    modifier = Modifier.weight(1f)
                ) { selectedTab = 1 }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedTab == 0) {
                FinanceContent(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    selectedCourse = selectedCourse,
                    onCourseChange = { selectedCourse = it },
                    selectedIntake = selectedIntake,
                    onIntakeChange = { selectedIntake = it },
                    filteredUnis = filteredUnis,
                    popularUnis = popularUnis,
                    selectedUni = selectedUni,
                    onUniSelected = { 
                        searchQuery = it.name
                        selectedUni = it
                        expanded = false
                    },
                    onClearSearch = {
                        searchQuery = ""
                        selectedUni = null
                        expanded = false
                    },
                    onCostCalculatorClick = {
                        if (popularUnis.isNotEmpty()) {
                            searchQuery = popularUnis.first().name
                            selectedUni = popularUnis.first()
                            expanded = false
                        }
                    },
                    onVisaRequirementsClick = {
                        selectedTab = 1
                    },
                    onSaveAndViewDetails = {
                        // Auto-select first popular uni if none chosen
                        if (selectedUni == null && popularUnis.isNotEmpty()) {
                            selectedUni = popularUnis.first()
                            searchQuery = popularUnis.first().name
                        }
                        if (selectedUni != null) {
                            financeDetailMode = true
                        }
                    }
                )
            } else {
                VisaPlannerContent(isBlockedAccountVerified = isBlockedAccountVerified, onViewDetailsClick = { visaDetailMode = true })
            }
        }
    }
}

@Composable
fun VisaPlannerContent(isBlockedAccountVerified: Boolean = false, onViewDetailsClick: () -> Unit) {
    // Visa Hero Banner
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD1FAE5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.School, contentDescription = "Visa", tint = Color(0xFF059669), modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Visa & Insurance Planner", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Your personalized checklist for a smooth visa process.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF475569))
        }
    }
    
    Spacer(modifier = Modifier.height(24.dp))

    // Current Visa Category
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onViewDetailsClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Color(0xFFF0F9FF), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Current Visa Category", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
                    Text("Student Visa (Type D)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("(for enrolment)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                }
            }
            Box(
                modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Edit", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    
    // Planner Items
    PlannerItem(
        icon = Icons.Default.HealthAndSafety, 
        iconColor = Color(0xFF059669), 
        iconBg = Color(0xFFD1FAE5),
        title = "Health Insurance", 
        subtitle = "Travel + German health insurance\n(for enrolment)",
        statusText = "Recommended",
        statusColor = Color(0xFF059669),
        statusBg = Color(0xFFD1FAE5),
        onClick = onViewDetailsClick
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    PlannerItem(
        icon = Icons.Default.AccountBalance, 
        iconColor = Color(0xFF0284C7), 
        iconBg = Color(0xFFF0F9FF),
        title = "Blocked Account", 
        subtitle = "€11,208 (1 year) • Deutsche Bank/ Fintiba",
        statusText = if (isBlockedAccountVerified) "Verified" else "Required",
        statusColor = if (isBlockedAccountVerified) Color(0xFF059669) else Color(0xFF059669),
        statusBg = Color(0xFFD1FAE5),
        onClick = onViewDetailsClick
    )
    
    Spacer(modifier = Modifier.height(12.dp))
    
    PlannerItem(
        icon = Icons.Default.Event, 
        iconColor = Color(0xFF059669), 
        iconBg = Color(0xFFD1FAE5),
        title = "Visa Appointment", 
        subtitle = "Book at German Embassy/Consulate",
        statusText = "View",
        statusColor = MaterialTheme.colorScheme.primary,
        statusBg = Color(0xFFDBEAFE),
        onClick = onViewDetailsClick
    )

    Spacer(modifier = Modifier.height(12.dp))
    
    PlannerItem(
        icon = Icons.Default.GppGood, 
        iconColor = Color(0xFF059669), 
        iconBg = Color(0xFFD1FAE5),
        title = "Visa Status", 
        subtitle = "Track your application progress",
        statusText = "Not Started",
        statusColor = Color(0xFF64748B),
        statusBg = Color(0xFFF1F5F9),
        onClick = onViewDetailsClick
    )

    Spacer(modifier = Modifier.height(24.dp))
    
    // Info Box
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row {
            Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Requirements may vary based on your university, course and personal situation. Always refer to the official German Embassy website.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF475569),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Visit Official Website", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun PlannerItem(
    icon: ImageVector, iconColor: Color, iconBg: Color,
    title: String, subtitle: String,
    statusText: String, statusColor: Color, statusBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(iconBg, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B), lineHeight = 16.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(statusBg, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun VisaDetailView(
    isBlockedAccountVerified: Boolean = false,
    isPassportVerified: Boolean = false,
    isAdmissionVerified: Boolean = false,
    isHealthVerified: Boolean = false,
    isApsVerified: Boolean = false,
    isCvVerified: Boolean = false,
    isTranscriptsVerified: Boolean = false,
    onBack: () -> Unit,
    onNavigateToDocuments: () -> Unit = {}
) {
    // Top Bar handled by scaffold, but we have our own nested view here.
    // Assuming this is inside the Visa Details screen
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp)).padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(20.dp)).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text("Overview", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.weight(1f).padding(vertical = 8.dp).clickable { onNavigateToDocuments() }, contentAlignment = Alignment.Center) {
                Text("Documents", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            }
            Box(modifier = Modifier.weight(1f).padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text("Timeline", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        // Detail Cards
        VisaDetailCard(
            title = "Health Insurance",
            subtitle = "Travel medical insurance (for visa) and\nGerman health insurance (after enrolment).",
            status = "Required",
            icon = Icons.Default.HealthAndSafety,
            buttonText = "View Recommendations"
        )
        Spacer(modifier = Modifier.height(16.dp))
        VisaDetailCard(
            title = "Blocked Account",
            subtitle = "Amount: € 11,208 (≈ ₹ 9,48,000)\nDuration: 12 months",
            status = if (isBlockedAccountVerified) "Verified" else "Required",
            icon = Icons.Default.AccountBalance,
            buttonText = if (isBlockedAccountVerified) "View Document" else "Find Providers",
            iconBg = Color(0xFFE0F2FE),
            iconTint = Color(0xFF0284C7)
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Checklist Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).background(Color(0xFFE0E7FF), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Checklist, contentDescription = null, tint = Color(0xFF4F46E5), modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Visa Documents Checklist", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    }
                    Box(modifier = Modifier.background(Color(0xFFD1FAE5), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                        val completedCount = listOf(isPassportVerified, isAdmissionVerified, isBlockedAccountVerified, isHealthVerified, isApsVerified, isCvVerified, isTranscriptsVerified).count { it }
                        Text("${completedCount}/7 completed", color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ChecklistItem("Valid Passport", isChecked = isPassportVerified)
                ChecklistItem("Admission Letter", isChecked = isAdmissionVerified)
                ChecklistItem("Proof of Funds (Blocked Account)", isChecked = isBlockedAccountVerified)
                ChecklistItem("Health Insurance", isChecked = isHealthVerified)
                ChecklistItem("APS Certificate (if required)", isChecked = isApsVerified)
                ChecklistItem("CV & Motivation Letter", isChecked = isCvVerified)
                ChecklistItem("Academic Transcripts", isChecked = isTranscriptsVerified)
                
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)).padding(vertical = 12.dp).clickable {  }, contentAlignment = Alignment.Center) {
                    Text("View Full Checklist", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun ChecklistItem(text: String, isChecked: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(
            if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isChecked) Color(0xFF10B981) else Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = if (isChecked) Color(0xFF059669) else Color(0xFF475569))
    }
}

@Composable
fun VisaDetailCard(title: String, subtitle: String, status: String, icon: ImageVector, buttonText: String, iconBg: Color = Color(0xFFD1FAE5), iconTint: Color = Color(0xFF059669)) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row {
                    Box(modifier = Modifier.size(32.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B), lineHeight = 18.sp)
                    }
                }
                Box(modifier = Modifier.background(Color(0xFFD1FAE5), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp).height(18.dp), contentAlignment = Alignment.Center) {
                    Text(status, color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)).padding(vertical = 12.dp).clickable {  }, contentAlignment = Alignment.Center) {
                Text(buttonText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceContent(
    searchQuery: String, onSearchQueryChange: (String) -> Unit,
    expanded: Boolean, onExpandedChange: (Boolean) -> Unit,
    selectedCourse: String, onCourseChange: (String) -> Unit,
    selectedIntake: String, onIntakeChange: (String) -> Unit,
    filteredUnis: List<SimpleUni>, popularUnis: List<SimpleUni>,
    selectedUni: SimpleUni?, onUniSelected: (SimpleUni) -> Unit,
    onClearSearch: () -> Unit,
    onCostCalculatorClick: () -> Unit,
    onVisaRequirementsClick: () -> Unit,
    onSaveAndViewDetails: () -> Unit
) {
    // Search Bar with Dropdown
    Text("Search University or College", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
    Spacer(modifier = Modifier.height(12.dp))
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                onSearchQueryChange(it)
                onExpandedChange(true)
                if (it.isEmpty()) onClearSearch()
            },
            placeholder = { Text("e.g. TUM, RWTH Aachen, KIT...", color = Color(0xFF94A3B8)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF64748B))
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            modifier = Modifier.fillMaxWidth().menuAnchor(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded && filteredUnis.isNotEmpty(),
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
        ) {
            filteredUnis.forEach { uni ->
                DropdownMenuItem(
                    text = { Text(uni.name) },
                    onClick = { onUniSelected(uni) }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (selectedUni == null) {
        // Initial State
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Popular Universities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
            Text("View all", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(popularUnis.size) { index ->
                val uni = popularUnis[index]
                UniversityCard(name = uni.name, shortName = uni.shortName, onClick = {
                    onUniSelected(uni)
                })
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Quick Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ToolCard(
                modifier = Modifier.weight(1f),
                title = "Cost Calculator",
                subtitle = "Get estimated expenses in € and ₹",
                icon = Icons.Default.Calculate,
                iconColor = Color(0xFF10B981),
                iconBg = Color(0xFFD1FAE5),
                onClick = onCostCalculatorClick
            )
            ToolCard(
                modifier = Modifier.weight(1f),
                title = "Visa Requirements",
                subtitle = "Check documents & process",
                icon = Icons.Default.VerifiedUser,
                iconColor = Color(0xFF3B82F6),
                iconBg = Color(0xFFDBEAFE),
                onClick = onVisaRequirementsClick
            )
        }
    } else {
        // Selected University State
        var expandedCourse by remember { mutableStateOf(false) }
        val courseOptions = listOf("M.Sc. Computer Science", "M.Sc. Data Science", "MBA", "M.Sc. Mechanical Engineering")

        var expandedIntake by remember { mutableStateOf(false) }
        val intakeOptions = listOf("Winter Semester 2025", "Summer Semester 2025", "Winter Semester 2026")

        var isAnalyzing by remember(selectedUni, selectedCourse, selectedIntake) { mutableStateOf(true) }

        LaunchedEffect(selectedUni, selectedCourse, selectedIntake) {
            if (selectedCourse.isNotEmpty() && selectedIntake.isNotEmpty()) {
                isAnalyzing = true
                kotlinx.coroutines.delay(1500)
                isAnalyzing = false
            }
        }

        Text("Course & Intake", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(12.dp))
        
        ExposedDropdownMenuBox(
            expanded = expandedCourse,
            onExpandedChange = { expandedCourse = it }
        ) {
            OutlinedTextField(
                value = selectedCourse,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select Course", color = Color(0xFF94A3B8)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourse) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            ExposedDropdownMenu(
                expanded = expandedCourse,
                onDismissRequest = { expandedCourse = false }
            ) {
                courseOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onCourseChange(option)
                            expandedCourse = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        ExposedDropdownMenuBox(
            expanded = expandedIntake,
            onExpandedChange = { expandedIntake = it }
        ) {
            OutlinedTextField(
                value = selectedIntake,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select Intake", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(20.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedIntake) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            ExposedDropdownMenu(
                expanded = expandedIntake,
                onDismissRequest = { expandedIntake = false }
            ) {
                intakeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onIntakeChange(option)
                            expandedIntake = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectedCourse.isEmpty() || selectedIntake.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Please select a Course and Intake to estimate costs.", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B), textAlign = TextAlign.Center)
            }
        } else if (isAnalyzing) {
            Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analyzing tuition & living costs...", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF64748B))
                }
            }
        } else {
            Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(selectedUni.shortName, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(selectedUni.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.background(Color(0xFFD1FAE5), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("Top Choice", color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$selectedCourse • $selectedIntake", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                    }
                }

                var selectedCurrency by remember { mutableStateOf("EUR") }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Estimated Total Cost (1st Year)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f).alpha(if (selectedCurrency == "EUR") 1f else 0.5f).background(Color(0xFFF0F9FF), RoundedCornerShape(8.dp)).border(if(selectedCurrency == "EUR") 2.dp else 1.dp, Color(0xFFBAE6FD), RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)).clickable { selectedCurrency = "EUR" }.padding(12.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val isPrivate = selectedUni.type.contains("Private", ignoreCase = true)
                            Text(if(isPrivate) "€ 26,750" else "€ 14,750", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                            Text("in Germany", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0284C7))
                        }
                    }
                    Box(modifier = Modifier.weight(1f).alpha(if (selectedCurrency == "INR") 1f else 0.5f).background(Color(0xFFECFDF5), RoundedCornerShape(8.dp)).border(if(selectedCurrency == "INR") 2.dp else 1.dp, Color(0xFFA7F3D0), RoundedCornerShape(8.dp)).clip(RoundedCornerShape(8.dp)).clickable { selectedCurrency = "INR" }.padding(12.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val isPrivate = selectedUni.type.contains("Private", ignoreCase = true)
                            Text(if(isPrivate) "₹ 23,95,000" else "₹ 13,21,000", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                            Text("(approx.) in India", style = MaterialTheme.typography.labelSmall, color = Color(0xFF047857))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                CostBreakdownList(uni = selectedUni, selectedCurrency = selectedCurrency)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSaveAndViewDetails,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save & View Details", fontWeight = FontWeight.Bold)
        }
    }
    }
}

@Composable
fun CostBreakdownList(uni: SimpleUni, selectedCurrency: String) {
    val isPrivate = uni.type.contains("Private", ignoreCase = true)
    val tuitionEur = if (isPrivate) "€ 12,000" else "€ 0"
    val tuitionInr = if (isPrivate) "₹ 10,74,000" else "₹ 0"
    val tuitionLabel = if (isPrivate) "(Private University)" else "(Public University)"
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cost Breakdown", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        CostItem(Icons.Default.School, "Tuition Fees", tuitionLabel, if (selectedCurrency == "EUR") tuitionEur else tuitionInr)
        CostItem(Icons.Default.CalendarToday, "Semester Contribution", "", if (selectedCurrency == "EUR") "€ 150" else "₹ 13,400")
        CostItem(Icons.Default.Home, "Living Expenses", "(Room, Food, Transport)", if (selectedCurrency == "EUR") "€ 11,400" else "₹ 10,19,000")
        CostItem(Icons.Default.LocalHospital, "Health Insurance", "", if (selectedCurrency == "EUR") "€ 1,200" else "₹ 1,07,400")
        CostItem(Icons.Default.Flight, "Visa & Travel Costs", "", if (selectedCurrency == "EUR") "€ 800" else "₹ 71,600")
        CostItem(Icons.Default.Settings, "Initial Setup", "(Phone, Bank, etc.)", if (selectedCurrency == "EUR") "€ 1,200" else "₹ 1,07,400")
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Exchange Rate", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
            }
            Text("1 € = ₹ 89.5 (as of 21 Apr 2025)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun CostItem(icon: ImageVector, title: String, subtitle: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(32.dp).background(Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color(0xFF1E293B))
            if (subtitle.isNotEmpty()) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color(0xFF64748B))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
        }
    }
}

@Composable
fun SegmentButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF64748B),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun UniversityCard(name: String, shortName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(shortName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF475569),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ToolCard(modifier: Modifier = Modifier, title: String, subtitle: String, icon: ImageVector, iconColor: Color, iconBg: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B), lineHeight = 16.sp)
        }
    }
}
@Composable
fun FinanceDetailView(uni: SimpleUni, course: String, intake: String, onBack: () -> Unit) {
    val isPrivate = uni.type.contains("Private", ignoreCase = true)
    val context = LocalContext.current
    var selectedCurrency by remember { mutableStateOf("EUR") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // No verticalScroll here - parent Column in Scaffold already handles scrolling
    ) {
        // University Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(uni.shortName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(uni.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${uni.city}, Germany", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                        }
                    }
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color(0xFF64748B))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(uni.type, "Top 50 (QS)", "English-taught").forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(tag, fontSize = 11.sp, color = Color(0xFF475569), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Program Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Program Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(12.dp))
                DetailRow("Course", course)
                DetailRow("Duration", "2 years")
                val tuitionTxt = if (isPrivate) "€ 12,000 (Private University)" else "€ 0 (Public University)"
                DetailRow("Tuition Fee", tuitionTxt)
                DetailRow("Semester Contribution", "€ 150 / semester")
                DetailRow("Intake", intake)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Fee information is based on the official university website and last verified on 21 Apr 2025.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (uni.applyLink.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uni.applyLink))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View source", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Estimated Expenses
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estimated Expenses (1st Year)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    // EUR / INR pill toggle
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(3.dp)
                    ) {
                        listOf("EUR", "INR").forEach { currency ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selectedCurrency == currency) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedCurrency = currency }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    currency,
                                    color = if (selectedCurrency == currency) Color.White else Color(0xFF64748B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Cost rows
                val items = if (selectedCurrency == "EUR") listOf(
                    Triple(Icons.Default.School, "Tuition Fees", if (isPrivate) "€ 12,000" else "€ 0"),
                    Triple(Icons.Default.CalendarToday, "Semester Contribution", "€ 150"),
                    Triple(Icons.Default.Home, "Living Expenses", "€ 11,400"),
                    Triple(Icons.Default.HealthAndSafety, "Health Insurance", "€ 1,200"),
                    Triple(Icons.Default.Flight, "Visa & Travel", "€ 800"),
                    Triple(Icons.Default.Settings, "Initial Setup", "€ 1,200")
                ) else listOf(
                    Triple(Icons.Default.School, "Tuition Fees", if (isPrivate) "₹ 10,74,000" else "₹ 0"),
                    Triple(Icons.Default.CalendarToday, "Semester Contribution", "₹ 13,400"),
                    Triple(Icons.Default.Home, "Living Expenses", "₹ 10,19,000"),
                    Triple(Icons.Default.HealthAndSafety, "Health Insurance", "₹ 1,07,400"),
                    Triple(Icons.Default.Flight, "Visa & Travel", "₹ 71,600"),
                    Triple(Icons.Default.Settings, "Initial Setup", "₹ 1,07,400")
                )
                items.forEach { (icon, label, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), color = Color(0xFF1E293B))
                        Text(amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text(
                        if (selectedCurrency == "EUR") { if (isPrivate) "€ 26,750" else "€ 14,750" } else { if (isPrivate) "₹ 23,95,000" else "₹ 13,21,000" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Apply Now button
        Button(
            onClick = {
                val url = uni.applyLink.ifEmpty { "https://www.uni-assist.de/en/" }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1FAB71))
        ) {
            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apply Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B), modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B), textAlign = TextAlign.End, modifier = Modifier.weight(1.2f))
    }
}

