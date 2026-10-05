package com.example.edujourneygermany.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    // State variables for form fields (mocked with initial data)
    var name by remember { mutableStateOf(if (com.example.edujourneygermany.auth.UserSession.userName.isNotEmpty()) com.example.edujourneygermany.auth.UserSession.userName else "Applicant") }
    var email by remember { mutableStateOf(if (com.example.edujourneygermany.auth.UserSession.userEmail.isNotEmpty()) com.example.edujourneygermany.auth.UserSession.userEmail else "applicant@example.com") }
    
    var dob by remember { mutableStateOf("15 May 1998") }
    var nationality by remember { mutableStateOf("Indian") }
    var passport by remember { mutableStateOf("Z1234567") }
    
    var phone by remember { mutableStateOf("+91 98765 43210") }
    var location by remember { mutableStateOf("Mumbai, India") }
    
    var degree by remember { mutableStateOf("B.Tech in Computer Science") }
    var university by remember { mutableStateOf("ABC University") }
    var gradYear by remember { mutableStateOf("2020") }
    
    var role by remember { mutableStateOf("Software Engineer") }
    var company by remember { mutableStateOf("Tech Solutions Inc.") }
    var expDuration by remember { mutableStateOf("2 Years") }
    
    var englishLang by remember { mutableStateOf("IELTS 7.5 (C1)") }
    var germanLang by remember { mutableStateOf("A1") }
    
    var linkedin by remember { mutableStateOf("linkedin.com/in/rahulsharma") }
    var github by remember { mutableStateOf("github.com/rahulsharma") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Basic Info
                Text("Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                EditTextField(label = "Full Name", value = name, onValueChange = { name = it })
                EditTextField(label = "Email Address", value = email, onValueChange = { email = it })
                
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                
                // Personal Info
                Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                EditTextField(label = "Date of Birth", value = dob, onValueChange = { dob = it })
                EditTextField(label = "Nationality", value = nationality, onValueChange = { nationality = it })
                EditTextField(label = "Passport Number", value = passport, onValueChange = { passport = it })
                
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                
                // Contact Details
                Text("Contact Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                EditTextField(label = "Phone Number", value = phone, onValueChange = { phone = it })
                EditTextField(label = "Location", value = location, onValueChange = { location = it })
                
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                
                // Education
                Text("Education", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                EditTextField(label = "Degree", value = degree, onValueChange = { degree = it })
                EditTextField(label = "University", value = university, onValueChange = { university = it })
                EditTextField(label = "Graduation Year", value = gradYear, onValueChange = { gradYear = it })
                
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                
                // Experience
                Text("Experience", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                EditTextField(label = "Role", value = role, onValueChange = { role = it })
                EditTextField(label = "Company", value = company, onValueChange = { company = it })
                EditTextField(label = "Duration", value = expDuration, onValueChange = { expDuration = it })
                
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                
                // Languages
                Text("Languages", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                EditTextField(label = "English (e.g. IELTS 7.5)", value = englishLang, onValueChange = { englishLang = it })
                EditTextField(label = "German (e.g. A1)", value = germanLang, onValueChange = { germanLang = it })
                
                Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                
                // Social Links
                Text("Social Links (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))
                EditTextField(label = "LinkedIn", value = linkedin, onValueChange = { linkedin = it })
                EditTextField(label = "GitHub", value = github, onValueChange = { github = it })
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Save Button Section (Pinned to bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onSaveSuccess,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun EditTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
            ),
            singleLine = true
        )
    }
}
