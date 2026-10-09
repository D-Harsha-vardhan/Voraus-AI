package com.example.edujourneygermany.auth

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.edujourneygermany.R
import com.example.edujourneygermany.data.Supabase
import com.example.edujourneygermany.theme.PrimaryBlue
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashScreen(onGetStarted: () -> Unit, onLogin: () -> Unit, onAlreadyLoggedIn: () -> Unit = {}) {
    var animationState by remember { mutableStateOf(0) }
    
    var isAuthenticated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Supabase.client.auth.sessionStatus.collectLatest { status ->
            if (status is SessionStatus.Authenticated) {
                isAuthenticated = true
            }
        }
    }

    // Show logo early then slide it up, then show content
    LaunchedEffect(Unit) {
        delay(800)
        animationState = 1        // Fade in logo centered
        delay(1200)
        animationState = 2        // Slide logo up
        delay(500)
        
        if (isAuthenticated) {
            onAlreadyLoggedIn()
        } else {
            animationState = 3    // Show text and Get Started button
        }
    }


    // Animations
    val logoAlpha by animateFloatAsState(
        targetValue = if (animationState >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "logoAlpha"
    )
    
    // Y-offset for logo: 200dp down when centered, 0dp when at top
    val logoOffset by animateDpAsState(
        targetValue = if (animationState >= 2) 0.dp else 250.dp,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "logoOffset"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (animationState >= 3) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "contentAlpha"
    )
    
    val contentOffset by animateDpAsState(
        targetValue = if (animationState >= 3) 0.dp else 50.dp,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "contentOffset"
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        // Video Background
        AndroidView(
            factory = { context ->
                VideoView(context).apply {
                    setVideoURI(Uri.parse("android.resource://${context.packageName}/${R.raw.splash_bg_video}"))
                    setOnPreparedListener { mp ->
                        mp.isLooping = true          // Play once — full video
                        mp.setVolume(0f, 0f)          // Silent

                        // Center Crop Logic
                        val videoWidth = mp.videoWidth.toFloat()
                        val videoHeight = mp.videoHeight.toFloat()

                        if (videoWidth > 0 && videoHeight > 0 && screenWidth > 0 && screenHeight > 0) {
                            val videoRatio = videoWidth / videoHeight
                            val screenRatio = screenWidth / screenHeight

                            val scale = if (videoRatio > screenRatio) {
                                videoRatio / screenRatio
                            } else {
                                screenRatio / videoRatio
                            }
                            this.scaleX = scale
                            this.scaleY = scale
                        }

                        start()
                    }
                    // When video ends → reveal Get Started button
                    
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Dark Overlay Gradient (Brighter)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent, // Completely clear at the top so the video isn't dull
                            Color.Black.copy(alpha = 0.8f) // Dark at the bottom for text readability
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Logo Section (Animated)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = logoOffset)
                    .graphicsLayer(alpha = logoAlpha),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = "Logo",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.educaro_brand), // "Educaro" or "Voraus AI"
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.educaro_tagline),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Bottom Content Section (Animated)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = contentOffset)
                    .graphicsLayer(alpha = contentAlpha)
            ) {
                Text(
                    text = stringResource(R.string.your_dreams_our_ai),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 40.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.from_your_goals),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Get Started Button
                Button(
                    onClick = { if (animationState >= 3) onLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = stringResource(R.string.get_started), 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp, 
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


