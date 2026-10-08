package com.example.edujourneygermany.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.edujourneygermany.accessibility.AppSpeaker
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
import com.example.edujourneygermany.data.Supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.put


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingAboutYouScreen(onNext: () -> Unit, onBack: () -> Unit) {
    LaunchedEffect(Unit) {
        if (UserProfileStore.isBlindModeEnabled) {
            AppSpeaker.speak("Tell us about yourself. Please enter your full name, date of birth, gender, and nationality. Then tap Next.")
        }
    }
    // Bind to the global store instead of local state
    var fullName by remember { mutableStateOf(UserProfileStore.fullName) }
    var dob by remember { mutableStateOf(UserProfileStore.dob) }
    var gender by remember { mutableStateOf(UserProfileStore.gender) }
    var nationality by remember { mutableStateOf(UserProfileStore.nationality) }

    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Male", "Female", "Other", "Prefer not to say")

    var nationalityExpanded by remember { mutableStateOf(false) }
    val nationalityOptions = listOf("Indian", "American", "British", "Canadian", "Australian", "Other")

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
        ) {
            Text(
                "2. About You",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tell us more about yourself",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = 2f / 4f,
                    modifier = Modifier.weight(1f).height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("2/4", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Personal Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            com.example.edujourneygermany.accessibility.AccessibleOutlinedTextField(
                label = "Full Name",
                value = fullName,
                onValueChange = { fullName = it },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            com.example.edujourneygermany.accessibility.AccessibleOutlinedTextField(
                label = "Date of Birth",
                value = dob,
                onValueChange = { dob = it },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = { },
                    label = { Text("Gender") },
                    leadingIcon = { Icon(Icons.Default.Male, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genderOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                gender = selectionOption
                                genderExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ExposedDropdownMenuBox(
                expanded = nationalityExpanded,
                onExpandedChange = { nationalityExpanded = !nationalityExpanded }
            ) {
                OutlinedTextField(
                    value = nationality,
                    onValueChange = { },
                    label = { Text("Nationality") },
                    leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = nationalityExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
                ExposedDropdownMenu(
                    expanded = nationalityExpanded,
                    onDismissRequest = { nationalityExpanded = false }
                ) {
                    nationalityOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                nationality = selectionOption
                                nationalityExpanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            val scope = rememberCoroutineScope()

            Button(
                onClick = {
                    UserProfileStore.fullName = fullName
                    UserProfileStore.dob = dob
                    UserProfileStore.gender = gender
                    UserProfileStore.nationality = nationality
                    
                    // Save to Supabase
                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            val uid = Supabase.client.auth.currentUserOrNull()?.id
                            if (uid != null) {
                                val updateJson = kotlinx.serialization.json.buildJsonObject {
                                    put("full_name", fullName)
                                }
                                Supabase.client.postgrest["profiles"].update(updateJson) {
                                    filter {
                                        eq("id", uid)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    
                    onNext()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = fullName.isNotBlank() && dob.isNotBlank() && gender.isNotBlank() && nationality.isNotBlank()
            ) {
                Text("Next \u2192", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
