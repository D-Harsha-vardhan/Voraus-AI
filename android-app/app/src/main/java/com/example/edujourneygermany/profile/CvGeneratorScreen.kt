package com.example.edujourneygermany.profile

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edujourneygermany.data.UserProfileStore
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.edujourneygermany.auth.OcrHelper
import com.example.edujourneygermany.data.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import java.io.File
import androidx.core.content.FileProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CvGeneratorScreen() {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit CV Information" else "German CV (Lebenslauf)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        if (isEditing) {
            CvEditForm(
                paddingValues = paddingValues,
                onGenerate = { isEditing = false }
            )
        } else {
            CvPreview(
                paddingValues = paddingValues,
                onEdit = { isEditing = true },
                onDownload = { exportCvToPdf(context) }
            )
        }
    }
}

@Composable
fun CvEditForm(paddingValues: PaddingValues, onGenerate: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExtracting by remember { mutableStateOf(false) }
    var extractionError by remember { mutableStateOf<String?>(null) }

    val autoFillFromSupabase = {
        isExtracting = true
        extractionError = null
        scope.launch {
            try {
                val bucket = Supabase.client.storage["user_documents"]
                val email = Supabase.client.auth.currentUserOrNull()?.email ?: ""
                val searchKeys = listOf("_cv", "_resume")
                val files = bucket.list()
                
                val docFiles = files.filter { file ->
                    val lowerName = file.name.lowercase()
                    searchKeys.any { key -> lowerName.contains(key) } && lowerName.startsWith(email.lowercase())
                }
                
                if (docFiles.isNotEmpty()) {
                    val latestFile = docFiles.maxByOrNull { it.createdAt?.toString() ?: "" } ?: docFiles.first()
                    val bytes = bucket.downloadAuthenticated(latestFile.name)
                    
                    val tempFile = withContext(Dispatchers.IO) {
                        val file = File.createTempFile("cv_download", ".jpg", context.cacheDir)
                        file.writeBytes(bytes)
                        file
                    }
                    
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
                    val data = OcrHelper.extractData(context, uri, "Resume")
                    
                    data.forEach { (key, value) ->
                        if (value.isNotBlank()) {
                            when (key) {
                                "employee_name", "full_name" -> UserProfileStore.fullName = value
                                "phone_number" -> UserProfileStore.phone = value
                                "location" -> UserProfileStore.location = value
                                "role" -> UserProfileStore.role = value
                                "company" -> UserProfileStore.company = value
                                "professional_summary" -> {
                                    UserProfileStore.cvIntro = value
                                    com.example.edujourneygermany.data.LocalDocumentManager.saveCvIntroLocally(context, value)
                                }
                                "employment_start_date", "employment_end_date" -> {
                                    if (UserProfileStore.experienceDuration.isBlank()) {
                                        UserProfileStore.experienceDuration = value
                                    }
                                }
                            }
                        }
                    }
                } else {
                    extractionError = "No CV found in your uploaded documents."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                extractionError = "Failed to fetch or extract CV: ${e.message}"
            } finally {
                isExtracting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable { if (!isExtracting) autoFillFromSupabase() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                if (isExtracting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Fetching & Extracting...", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                } else {
                    Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Auto-fill from existing CV", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        if (extractionError != null) {
            Text(extractionError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(4.dp))

        Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = UserProfileStore.fullName, onValueChange = { UserProfileStore.fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = UserProfileStore.dob, onValueChange = { UserProfileStore.dob = it }, label = { Text("Date of Birth (e.g. 15.08.1995)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = UserProfileStore.nationality, onValueChange = { UserProfileStore.nationality = it }, label = { Text("Nationality") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = UserProfileStore.location, onValueChange = { UserProfileStore.location = it }, label = { Text("Address/Location") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = UserProfileStore.email, onValueChange = { UserProfileStore.email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = UserProfileStore.phone, onValueChange = { UserProfileStore.phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))
        Text("Professional Summary (Kurzprofil)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = UserProfileStore.cvIntro, 
            onValueChange = { 
                UserProfileStore.cvIntro = it
                com.example.edujourneygermany.data.LocalDocumentManager.saveCvIntroLocally(context, it)
            }, 
            label = { Text("Short Intro (in German)") }, 
            modifier = Modifier.fillMaxWidth().height(100.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Work Experience", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = UserProfileStore.role, onValueChange = { UserProfileStore.role = it }, label = { Text("Job Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = UserProfileStore.company, onValueChange = { UserProfileStore.company = it }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = UserProfileStore.experienceDuration, onValueChange = { UserProfileStore.experienceDuration = it }, label = { Text("Duration (e.g. 01/2021 - Heute)") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))
        Text("Education", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = UserProfileStore.degree, onValueChange = { UserProfileStore.degree = it }, label = { Text("Degree") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = UserProfileStore.university, onValueChange = { UserProfileStore.university = it }, label = { Text("University") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = UserProfileStore.graduationYear, onValueChange = { UserProfileStore.graduationYear = it }, label = { Text("Graduation Year / Duration") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))
        Text("Languages", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = UserProfileStore.englishLevel, onValueChange = { UserProfileStore.englishLevel = it }, label = { Text("English Level (e.g. C1 / Fließend)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = UserProfileStore.germanLevel, onValueChange = { UserProfileStore.germanLevel = it }, label = { Text("German Level (e.g. B2 / Gute Kenntnisse)") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onGenerate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Generate German CV", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CvPreview(paddingValues: PaddingValues, onEdit: () -> Unit, onDownload: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Real-time CV Preview (A4 Aspect Ratio approx)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Sidebar (Left Column)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.35f)
                        .background(Color(0xFF1E293B))
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    CvSidebarHeader("Persönliche Daten")
                    CvSidebarItem("Geburtsdatum", UserProfileStore.dob.takeIf { it.isNotBlank() } ?: "01.01.1995")
                    CvSidebarItem("Nationalität", UserProfileStore.nationality.takeIf { it.isNotBlank() } ?: "Deutsch")
                    CvSidebarItem("Adresse", UserProfileStore.location.takeIf { it.isNotBlank() } ?: "Berlin, Deutschland")
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    CvSidebarHeader("Kontakt")
                    CvSidebarItem("E-Mail", UserProfileStore.email.takeIf { it.isNotBlank() } ?: "max@beispiel.de")
                    CvSidebarItem("Telefon", UserProfileStore.phone.takeIf { it.isNotBlank() } ?: "+49 123 456789")

                    Spacer(modifier = Modifier.height(24.dp))
                    CvSidebarHeader("Sprachen")
                    CvSidebarItem("Englisch", UserProfileStore.englishLevel.takeIf { it.isNotBlank() } ?: "Fließend (C1)")
                    CvSidebarItem("Deutsch", UserProfileStore.germanLevel.takeIf { it.isNotBlank() } ?: "Gute Kenntnisse (B2)")
                }

                // Main Content (Right Column)
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.65f)
                        .background(Color.White)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val fullName = UserProfileStore.fullName.takeIf { it.isNotBlank() } ?: "Max Mustermann"
                    Text(fullName.uppercase(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A), letterSpacing = 1.sp)
                    val mainRole = UserProfileStore.role.takeIf { it.isNotBlank() } ?: "Softwareentwickler"
                    Text(mainRole, style = MaterialTheme.typography.titleMedium, color = Color(0xFF64748B))

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (UserProfileStore.cvIntro.isNotBlank()) {
                        CvMainHeader("Kurzprofil")
                        Text(UserProfileStore.cvIntro, fontSize = 13.sp, color = Color(0xFF334155), lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    CvMainHeader("Berufserfahrung")
                    val expDur = UserProfileStore.experienceDuration.takeIf { it.isNotBlank() } ?: "01/2021 - Heute"
                    val comp = UserProfileStore.company.takeIf { it.isNotBlank() } ?: "Beispielfirma GmbH"
                    CvTimelineItem(date = expDur, title = mainRole, subtitle = comp)

                    Spacer(modifier = Modifier.height(24.dp))
                    CvMainHeader("Bildungsweg")
                    val gradYear = UserProfileStore.graduationYear.takeIf { it.isNotBlank() } ?: "2020"
                    val degree = UserProfileStore.degree.takeIf { it.isNotBlank() } ?: "M.Sc. Informatik"
                    val uni = UserProfileStore.university.takeIf { it.isNotBlank() } ?: "TU Berlin"
                    CvTimelineItem(date = gradYear, title = degree, subtitle = uni)

                    Spacer(modifier = Modifier.weight(1f))
                    val loc = UserProfileStore.location.takeIf { it.isNotBlank() } ?: "Berlin"
                    Text("$loc, den ${SimpleDateFormat("dd.MM.yyyy").format(Date())}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("___________________________", fontSize = 12.sp, color = Color.Gray)
                    Text("(Unterschrift)", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Data", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onDownload,
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download PDF", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CvSidebarHeader(title: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)) {
        Text(title.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1), letterSpacing = 1.sp)
        HorizontalDivider(color = Color(0xFF475569), thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun CvSidebarItem(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label.uppercase(), fontSize = 10.sp, color = Color(0xFF94A3B8))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFFF8FAFC))
    }
}

@Composable
fun CvMainHeader(title: String) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(title.uppercase(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), letterSpacing = 1.sp)
        HorizontalDivider(color = Color(0xFF0F172A), thickness = 2.dp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun CvTimelineItem(date: String, title: String, subtitle: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(date, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.weight(0.35f))
        Column(modifier = Modifier.weight(0.65f).padding(start = 8.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            Text(subtitle, fontSize = 13.sp, color = Color(0xFF475569))
        }
    }
}

fun exportCvToPdf(context: Context) {
    val fullName = UserProfileStore.fullName.takeIf { it.isNotBlank() } ?: "Max Mustermann"
    val dob = UserProfileStore.dob.takeIf { it.isNotBlank() } ?: "01.01.1995"
    val nationality = UserProfileStore.nationality.takeIf { it.isNotBlank() } ?: "Deutsch"
    val location = UserProfileStore.location.takeIf { it.isNotBlank() } ?: "Berlin, Deutschland"
    val email = UserProfileStore.email.takeIf { it.isNotBlank() } ?: "max@beispiel.de"
    val phone = UserProfileStore.phone.takeIf { it.isNotBlank() } ?: "+49 123 456789"
    val role = UserProfileStore.role.takeIf { it.isNotBlank() } ?: "Softwareentwickler"
    val company = UserProfileStore.company.takeIf { it.isNotBlank() } ?: "Beispielfirma GmbH"
    val experienceDuration = UserProfileStore.experienceDuration.takeIf { it.isNotBlank() } ?: "01/2021 - Heute"
    val degree = UserProfileStore.degree.takeIf { it.isNotBlank() } ?: "M.Sc. Informatik"
    val university = UserProfileStore.university.takeIf { it.isNotBlank() } ?: "TU Berlin"
    val graduationYear = UserProfileStore.graduationYear.takeIf { it.isNotBlank() } ?: "2020"
    val englishLevel = UserProfileStore.englishLevel.takeIf { it.isNotBlank() } ?: "Fließend (C1)"
    val germanLevel = UserProfileStore.germanLevel.takeIf { it.isNotBlank() } ?: "Gute Kenntnisse (B2)"
    val intro = UserProfileStore.cvIntro
    
    val introHtml = if (intro.isNotBlank()) """
        <div class="section">
            <h2>Kurzprofil</h2>
            <div class="summary">$intro</div>
        </div>
    """.trimIndent() else ""

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="utf-8">
        <style>
            body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 0; padding: 0; color: #333; display: flex; height: 100vh; }
            .sidebar { width: 32%; background-color: #1e293b; color: #f8fafc; padding: 40px 25px; box-sizing: border-box; }
            .main-content { width: 68%; background-color: #ffffff; padding: 40px 50px; box-sizing: border-box; }
            .sidebar h2 { color: #cbd5e1; border-bottom: 1px solid #475569; padding-bottom: 5px; font-size: 15px; text-transform: uppercase; letter-spacing: 1px; margin-top: 30px; margin-bottom: 15px;}
            .main-content h2 { color: #0f172a; border-bottom: 2px solid #0f172a; padding-bottom: 5px; font-size: 18px; text-transform: uppercase; letter-spacing: 1px; margin-top: 30px; margin-bottom: 20px;}
            .name { font-size: 36px; font-weight: bold; margin-bottom: 5px; color: #0f172a; text-transform: uppercase; letter-spacing: 2px;}
            .job-title { font-size: 18px; color: #64748b; margin-bottom: 40px; }
            .sidebar-item { margin-bottom: 15px; }
            .sidebar-label { font-size: 12px; color: #94a3b8; text-transform: uppercase; margin-bottom: 3px;}
            .sidebar-value { font-size: 14px; font-weight: 500; }
            .timeline-item { margin-bottom: 20px; display: flex; }
            .timeline-date { width: 140px; font-size: 14px; color: #64748b; font-weight: bold; flex-shrink: 0; padding-top: 2px;}
            .timeline-content { padding-left: 20px; border-left: 2px solid #e2e8f0; }
            .timeline-title { font-weight: bold; font-size: 16px; color: #0f172a; }
            .timeline-subtitle { font-size: 15px; color: #475569; margin-top: 4px; }
            .summary { font-size: 15px; line-height: 1.6; color: #334155; }
            .footer { margin-top: 60px; font-size: 14px; color: #64748b; }
        </style>
        </head>
        <body>
            <div class="sidebar">
                <h2 style="margin-top:0;">Persönliche Daten</h2>
                <div class="sidebar-item"><div class="sidebar-label">Geburtsdatum</div><div class="sidebar-value">$dob</div></div>
                <div class="sidebar-item"><div class="sidebar-label">Nationalität</div><div class="sidebar-value">$nationality</div></div>
                <div class="sidebar-item"><div class="sidebar-label">Adresse</div><div class="sidebar-value">$location</div></div>
                
                <h2>Kontakt</h2>
                <div class="sidebar-item"><div class="sidebar-label">E-Mail</div><div class="sidebar-value">$email</div></div>
                <div class="sidebar-item"><div class="sidebar-label">Telefon</div><div class="sidebar-value">$phone</div></div>
                
                <h2>Sprachen</h2>
                <div class="sidebar-item"><div class="sidebar-label">Englisch</div><div class="sidebar-value">$englishLevel</div></div>
                <div class="sidebar-item"><div class="sidebar-label">Deutsch</div><div class="sidebar-value">$germanLevel</div></div>
            </div>
            <div class="main-content">
                <div class="name">$fullName</div>
                <div class="job-title">$role</div>
                
                $introHtml
                
                <div class="section">
                    <h2>Berufserfahrung</h2>
                    <div class="timeline-item">
                        <div class="timeline-date">$experienceDuration</div>
                        <div class="timeline-content">
                            <div class="timeline-title">$role</div>
                            <div class="timeline-subtitle">$company</div>
                        </div>
                    </div>
                </div>

                <div class="section">
                    <h2>Bildungsweg</h2>
                    <div class="timeline-item">
                        <div class="timeline-date">$graduationYear</div>
                        <div class="timeline-content">
                            <div class="timeline-title">$degree</div>
                            <div class="timeline-subtitle">$university</div>
                        </div>
                    </div>
                </div>

                <div class="footer">
                    $location, den ${SimpleDateFormat("dd.MM.yyyy").format(Date())}<br><br><br>
                    ___________________________<br>
                    <span style="font-size: 10px;">(Unterschrift)</span>
                </div>
            </div>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "Lebenslauf_${fullName.replace(" ", "_")}"
            val printAdapter = view.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
}
