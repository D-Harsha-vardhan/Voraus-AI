package com.example.edujourneygermany.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edujourneygermany.R
import com.example.edujourneygermany.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isStudentSelected by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Header Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Logo",
                    modifier = Modifier.size(32.dp),
                    tint = PrimaryBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.educaro_brand),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.educaro_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Welcome Text
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.welcome_back),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.sign_in_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Segmented Control (Student / Consultant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isStudentSelected) PrimaryBlue else Color.Transparent)
                        .clickable { isStudentSelected = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.student_tab),
                        color = if (isStudentSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (!isStudentSelected) PrimaryBlue else Color.Transparent)
                        .clickable { isStudentSelected = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.consultant_tab),
                        color = if (!isStudentSelected) Color.White else Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text(stringResource(R.string.email_address), color = Color.Gray) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = Color.Gray)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = PrimaryBlue
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text(stringResource(R.string.password), color = Color.Gray) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = Color.Gray
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = PrimaryBlue
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Forgot Password
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = stringResource(R.string.forgot_password),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { /* Handle forgot password */ }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Login Button
            Button(
                onClick = onLoginSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text(stringResource(R.string.sign_in_button), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // OR Divider
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                Text(
                    text = stringResource(R.string.or_continue_with),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                Divider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Social Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text("G", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.continue_with_google), color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { /* TODO */ },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Text("🍎", fontSize = 18.sp) // Fallback for Apple icon
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.continue_with_apple), color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))

            // Register Link
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = Color.Gray)) {
                        append(stringResource(R.string.dont_have_account))
                    }
                    withStyle(style = SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Bold)) {
                        append(stringResource(R.string.sign_up))
                    }
                },
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .clickable { /* Navigate to register */ }
            )
        }
    }
}
