package com.example.edujourneygermany.profile

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edujourneygermany.data.UserProfileStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApsGeneratorScreen(onBack: () -> Unit) {
    var aadharNumber by remember { mutableStateOf("") }
    var paymentReference by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf("") }
    var highSchoolBoard by remember { mutableStateOf("") }
    var highSchoolPercentage by remember { mutableStateOf("") }
    var bachelorsCgpa by remember { mutableStateOf("") }
    var showPreview by remember { mutableStateOf(false) }

    if (showPreview) {
        ApsPreview(
            aadharNumber = aadharNumber,
            paymentReference = paymentReference,
            paymentDate = paymentDate,
            highSchoolBoard = highSchoolBoard,
            highSchoolPercentage = highSchoolPercentage,
            bachelorsCgpa = bachelorsCgpa,
            onBack = { showPreview = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("APS Certificate Generator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Auto-filled from your Profile. Please fill in the missing details for your APS India Application.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = UserProfileStore.fullName, onValueChange = { UserProfileStore.fullName = it }, isError = UserProfileStore.fullName.isBlank(), label = { Text("Full Name (as on Passport)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = UserProfileStore.dob, onValueChange = { UserProfileStore.dob = it }, isError = UserProfileStore.dob.isBlank(), label = { Text("Date of Birth (DD.MM.YYYY)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = UserProfileStore.passportNumber, onValueChange = { UserProfileStore.passportNumber = it }, isError = UserProfileStore.passportNumber.isBlank(), label = { Text("Passport Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = aadharNumber, onValueChange = { aadharNumber = it }, isError = aadharNumber.isBlank(), label = { Text("Aadhar Number (12 digits)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = UserProfileStore.email, onValueChange = { UserProfileStore.email = it }, isError = UserProfileStore.email.isBlank(), label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = UserProfileStore.phone, onValueChange = { UserProfileStore.phone = it }, isError = UserProfileStore.phone.isBlank(), label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            Text("Educational Qualifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = highSchoolBoard, onValueChange = { highSchoolBoard = it }, isError = highSchoolBoard.isBlank(), label = { Text("12th Grade Board (e.g., CBSE)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = highSchoolPercentage, onValueChange = { highSchoolPercentage = it }, isError = highSchoolPercentage.isBlank(), label = { Text("12th Grade Percentage") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = UserProfileStore.degree, onValueChange = { UserProfileStore.degree = it }, isError = UserProfileStore.degree.isBlank(), label = { Text("Bachelor's Degree Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = UserProfileStore.university, onValueChange = { UserProfileStore.university = it }, isError = UserProfileStore.university.isBlank(), label = { Text("University Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = bachelorsCgpa, onValueChange = { bachelorsCgpa = it }, isError = bachelorsCgpa.isBlank(), label = { Text("Bachelor's CGPA / Percentage") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))

            Text("Payment Details (CCavenue / Bank Transfer)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = paymentReference, onValueChange = { paymentReference = it }, isError = paymentReference.isBlank(), label = { Text("Transaction Reference Number") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = paymentDate, onValueChange = { paymentDate = it }, isError = paymentDate.isBlank(), label = { Text("Transaction Date (DD.MM.YYYY)") }, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { showPreview = true },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Generate APS Application Form", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApsPreview(
    aadharNumber: String,
    paymentReference: String,
    paymentDate: String,
    highSchoolBoard: String,
    highSchoolPercentage: String,
    bachelorsCgpa: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("APS Form Preview", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
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
            // Preview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("APS INDIA - APPLICATION FORM", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("1. Personal Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ApsPreviewItem("Full Name", UserProfileStore.fullName)
                    ApsPreviewItem("Date of Birth", UserProfileStore.dob)
                    ApsPreviewItem("Passport Number", UserProfileStore.passportNumber)
                    ApsPreviewItem("Aadhar Number", aadharNumber)
                    ApsPreviewItem("Email ID", UserProfileStore.email)
                    ApsPreviewItem("Mobile Number", UserProfileStore.phone)

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("2. Educational Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ApsPreviewItem("12th Grade Board", highSchoolBoard)
                    ApsPreviewItem("12th Grade %", highSchoolPercentage)
                    ApsPreviewItem("Bachelor Degree", UserProfileStore.degree)
                    ApsPreviewItem("University", UserProfileStore.university)
                    ApsPreviewItem("CGPA / %", bachelorsCgpa)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("3. Payment Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ApsPreviewItem("Reference Number", paymentReference)
                    ApsPreviewItem("Transaction Date", paymentDate)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Signature: ___________________________", modifier = Modifier.align(Alignment.End))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    exportApsToPdf(
                        context, 
                        aadharNumber, 
                        paymentReference, 
                        paymentDate, 
                        highSchoolBoard, 
                        highSchoolPercentage, 
                        bachelorsCgpa
                    ) 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
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
fun ApsPreviewItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f), color = Color.DarkGray)
        Text(value.takeIf { it.isNotBlank() } ?: "-", modifier = Modifier.weight(0.6f), color = Color.Black)
    }
}

fun exportApsToPdf(
    context: Context,
    aadharNumber: String,
    paymentReference: String,
    paymentDate: String,
    highSchoolBoard: String,
    highSchoolPercentage: String,
    bachelorsCgpa: String
) {
    val fullName = UserProfileStore.fullName.takeIf { it.isNotBlank() } ?: "-"
    val dob = UserProfileStore.dob.takeIf { it.isNotBlank() } ?: "-"
    val passport = UserProfileStore.passportNumber.takeIf { it.isNotBlank() } ?: "-"
    val aadhar = aadharNumber.takeIf { it.isNotBlank() } ?: "-"
    val email = UserProfileStore.email.takeIf { it.isNotBlank() } ?: "-"
    val phone = UserProfileStore.phone.takeIf { it.isNotBlank() } ?: "-"
    
    val board12 = highSchoolBoard.takeIf { it.isNotBlank() } ?: "-"
    val perc12 = highSchoolPercentage.takeIf { it.isNotBlank() } ?: "-"
    val degree = UserProfileStore.degree.takeIf { it.isNotBlank() } ?: "-"
    val uni = UserProfileStore.university.takeIf { it.isNotBlank() } ?: "-"
    val cgpa = bachelorsCgpa.takeIf { it.isNotBlank() } ?: "-"
    
    val ref = paymentReference.takeIf { it.isNotBlank() } ?: "-"
    val pDate = paymentDate.takeIf { it.isNotBlank() } ?: "-"

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="utf-8">
        <style>
            body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; margin: 0; padding: 40px; color: #333; line-height: 1.6;}
            h1 { text-align: center; color: #0f172a; border-bottom: 2px solid #e2e8f0; padding-bottom: 10px; margin-bottom: 30px;}
            h2 { color: #1e3a8a; border-bottom: 1px solid #cbd5e1; padding-bottom: 5px; margin-top: 30px; font-size: 18px;}
            table { width: 100%; border-collapse: collapse; margin-top: 10px; }
            td { padding: 8px 0; vertical-align: top; }
            .label { font-weight: bold; width: 40%; color: #475569; }
            .value { width: 60%; color: #0f172a; }
            .signature-box { margin-top: 60px; text-align: right; }
        </style>
        </head>
        <body>
            <h1>APS INDIA - APPLICATION FORM</h1>
            
            <h2>1. Personal Details</h2>
            <table>
                <tr><td class="label">Full Name</td><td class="value">$fullName</td></tr>
                <tr><td class="label">Date of Birth</td><td class="value">$dob</td></tr>
                <tr><td class="label">Passport Number</td><td class="value">$passport</td></tr>
                <tr><td class="label">Aadhar Number</td><td class="value">$aadhar</td></tr>
                <tr><td class="label">Email ID</td><td class="value">$email</td></tr>
                <tr><td class="label">Mobile Number</td><td class="value">$phone</td></tr>
            </table>

            <h2>2. Educational Details</h2>
            <table>
                <tr><td class="label">12th Grade Board</td><td class="value">$board12</td></tr>
                <tr><td class="label">12th Grade %</td><td class="value">$perc12</td></tr>
                <tr><td class="label">Bachelor Degree</td><td class="value">$degree</td></tr>
                <tr><td class="label">University</td><td class="value">$uni</td></tr>
                <tr><td class="label">CGPA / %</td><td class="value">$cgpa</td></tr>
            </table>

            <h2>3. Payment Details</h2>
            <table>
                <tr><td class="label">Reference Number</td><td class="value">$ref</td></tr>
                <tr><td class="label">Transaction Date</td><td class="value">$pDate</td></tr>
            </table>
            
            <div class="signature-box">
                Signature: ___________________________<br>
                <span style="font-size: 12px; color: #64748b; margin-right: 50px;">(Applicant)</span>
            </div>
        </body>
        </html>
    """.trimIndent()

    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "APS_Form_${fullName.replace(" ", "_")}"
            val printAdapter = view.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
}
