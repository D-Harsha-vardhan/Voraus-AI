package com.example.edujourneygermany.auth

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edujourneygermany.data.UserProfileStore
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingExtendedProfileScreen(onNext: () -> Unit, onBack: () -> Unit) {
    var passportNumber by remember { mutableStateOf(UserProfileStore.passportNumber) }
    var phone by remember { mutableStateOf(UserProfileStore.phone) }
    var location by remember { mutableStateOf(UserProfileStore.location) }
    
    var degree by remember { mutableStateOf(UserProfileStore.degree) }
    var university by remember { mutableStateOf(UserProfileStore.university) }
    var graduationYear by remember { mutableStateOf(UserProfileStore.graduationYear) }
    
    var role by remember { mutableStateOf(UserProfileStore.role) }
    var company by remember { mutableStateOf(UserProfileStore.company) }
    var experienceDuration by remember { mutableStateOf(UserProfileStore.experienceDuration) }
    
    var englishLevel by remember { mutableStateOf(UserProfileStore.englishLevel) }
    var germanLevel by remember { mutableStateOf(UserProfileStore.germanLevel) }
    
    var linkedIn by remember { mutableStateOf(UserProfileStore.linkedIn) }
    var github by remember { mutableStateOf(UserProfileStore.github) }

    LaunchedEffect(Unit) {
        val userId = com.example.edujourneygermany.data.Supabase.client.auth.currentUserOrNull()?.id
        if (userId != null) {
            try {
                val result = com.example.edujourneygermany.data.Supabase.client.postgrest["profiles"]
                    .select { filter { eq("id", userId) } }
                    
                val jsonString = result.data
                val jsonArray = kotlinx.serialization.json.Json.parseToJsonElement(jsonString) as? kotlinx.serialization.json.JsonArray
                val firstObj = jsonArray?.firstOrNull() as? kotlinx.serialization.json.JsonObject
                
                if (firstObj != null) {
                    firstObj["passport_number"]?.let { if (it !is kotlinx.serialization.json.JsonNull) passportNumber = it.toString().removeSurrounding("\"") }
                    firstObj["degree"]?.let { if (it !is kotlinx.serialization.json.JsonNull) degree = it.toString().removeSurrounding("\"") }
                    firstObj["university"]?.let { if (it !is kotlinx.serialization.json.JsonNull) university = it.toString().removeSurrounding("\"") }
                    firstObj["graduation_year"]?.let { if (it !is kotlinx.serialization.json.JsonNull) graduationYear = it.toString().removeSurrounding("\"") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Complete Profile Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Please provide accurate details for your journey.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = 3f / 4f,
                    modifier = Modifier.weight(1f).height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("3/4", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Contact & ID", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = passportNumber, onValueChange = { passportNumber = it }, label = { Text("Passport Number") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location (City, Country)") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Education", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = degree, onValueChange = { degree = it }, label = { Text("Degree") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = university, onValueChange = { university = it }, label = { Text("University") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = graduationYear, onValueChange = { graduationYear = it }, label = { Text("Graduation Year") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Experience", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = company, onValueChange = { company = it }, label = { Text("Company") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = experienceDuration, onValueChange = { experienceDuration = it }, label = { Text("Duration (e.g. 2 Years)") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Languages", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = englishLevel, onValueChange = { englishLevel = it }, label = { Text("English Level (e.g. IELTS 7.5)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = germanLevel, onValueChange = { germanLevel = it }, label = { Text("German Level (e.g. A1)") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Social Links (Optional)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = linkedIn, onValueChange = { linkedIn = it }, label = { Text("LinkedIn Profile URL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = github, onValueChange = { github = it }, label = { Text("GitHub Profile URL") }, modifier = Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    UserProfileStore.passportNumber = passportNumber
                    UserProfileStore.phone = phone
                    UserProfileStore.location = location
                    UserProfileStore.degree = degree
                    UserProfileStore.university = university
                    UserProfileStore.graduationYear = graduationYear
                    UserProfileStore.role = role
                    UserProfileStore.company = company
                    UserProfileStore.experienceDuration = experienceDuration
                    UserProfileStore.englishLevel = englishLevel
                    UserProfileStore.germanLevel = germanLevel
                    UserProfileStore.linkedIn = linkedIn
                    UserProfileStore.github = github
                    onNext()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next \u2192", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
