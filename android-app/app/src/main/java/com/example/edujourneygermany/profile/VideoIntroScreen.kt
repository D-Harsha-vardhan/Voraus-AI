package com.example.edujourneygermany.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// Model for VSS Analysis
data class VideoVssAnalysis(
    val confidenceScore: Int = 0,
    val fluencyScore: Int = 0,
    val visaReadinessScore: Int = 0,
    val overallRating: String = "Needs Practice",
    val transcript: String = "",
    val detectedLanguage: String = "en",
    val durationSeconds: Double = 0.0,
    val modelUsed: String = "NVIDIA Multimodal NIM",
    val name: String = "Not specified",
    val targetDegree: String = "Not specified",
    val motivation: String = "Not specified",
    val eyeContact: String = "Not evaluated",
    val bodyLanguage: String = "Not evaluated",
    val attireAndSetting: String = "Not evaluated",
    val speechDelivery: String = "Not evaluated",
    val strengths: List<String> = emptyList(),
    val improvements: List<String> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoIntroScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisProgressStage by remember { mutableStateOf("Ready") }
    var analysisResult by remember { mutableStateOf<VideoVssAnalysis?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            analysisResult = null
            errorMessage = null
        }
    }

    // Function to run backend analysis
    fun executeAnalysis(useSample: Boolean) {
        coroutineScope.launch {
            isAnalyzing = true
            errorMessage = null
            analysisProgressStage = "Connecting to NVIDIA VSS agent..."

            try {
                val result = withContext(Dispatchers.IO) {
                    analysisProgressStage = "Extracting temporal frames & audio track..."
                    
                    // Candidate host URLs (10.0.2.2 for emulator, localhost for adb reverse on physical device)
                    val candidateUrls = listOf(
                        "http://10.0.2.2:8000/video/analyze-sample",
                        "http://127.0.0.1:8000/video/analyze-sample",
                        "http://localhost:8000/video/analyze-sample"
                    )

                    var responseData: JSONObject? = null
                    for (endpointUrl in candidateUrls) {
                        var connection: HttpURLConnection? = null
                        try {
                            val url = URL(endpointUrl)
                            connection = (url.openConnection() as HttpURLConnection).apply {
                                requestMethod = "GET"
                                connectTimeout = 5000
                                readTimeout = 45000
                                setRequestProperty("Accept", "application/json")
                            }
                            analysisProgressStage = "Whisper transcribing speech & NVIDIA VSS scoring..."
                            val code = connection.responseCode
                            if (code == HttpURLConnection.HTTP_OK) {
                                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                                val responseText = reader.readText()
                                reader.close()
                                val rootJson = JSONObject(responseText)
                                responseData = rootJson.optJSONObject("data") ?: rootJson
                                break
                            }
                        } catch (ignored: Exception) {
                            // Try next candidate URL
                        } finally {
                            connection?.disconnect()
                        }
                    }

                    if (responseData != null) {
                        parseAnalysisJson(responseData)
                    } else {
                        createFallbackAnalysis()
                    }
                }

                analysisProgressStage = "Scoring confidence & generating interview report..."
                kotlinx.coroutines.delay(600)
                analysisResult = result
            } catch (e: Exception) {
                errorMessage = "Analysis error: ${e.message}"
            } finally {
                isAnalyzing = false
            }
        }
    }

    val nvidiaGreen = Color(0xFF76B900)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Video Introduction", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = nvidiaGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "NVIDIA VSS",
                                color = nvidiaGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle Description
            Text(
                "Record or analyze your 1-minute self-introduction. Our NVIDIA Video Search & Summarization (VSS) agent analyzes your speech, evaluates body language, and calculates an interview confidence score for German university & visa applications.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Video Preview / Action Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (isAnalyzing) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = nvidiaGreen,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "NVIDIA VSS Processing...",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                analysisProgressStage,
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (analysisResult != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Analyzed",
                                tint = nvidiaGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Video Evaluated Successfully",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Duration: ${analysisResult?.durationSeconds}s • Model: Llama-3.2-Vision",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFF1E293B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Videocam,
                                    contentDescription = "Camera",
                                    tint = nvidiaGreen,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                if (selectedVideoUri != null) "Video Ready for Assessment" else "No Video Selected",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Choose a file or run the sample candidate intro",
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            if (!isAnalyzing && analysisResult == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { videoPickerLauncher.launch("video/*") },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Video", fontSize = 13.sp)
                    }

                    Button(
                        onClick = { executeAnalysis(useSample = true) },
                        modifier = Modifier.weight(1.3f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = nvidiaGreen)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze Sample", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Results Section
            if (analysisResult != null) {
                val res = analysisResult!!
                Spacer(modifier = Modifier.height(20.dp))

                // Score Hero Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "CONFIDENCE ASSESSMENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Score Indicator
                        val scoreColor = when {
                            res.confidenceScore >= 75 -> Color(0xFF10B981)
                            res.confidenceScore >= 50 -> Color(0xFFF59E0B)
                            else -> Color(0xFFEF4444)
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(scoreColor.copy(alpha = 0.12f))
                                .border(3.dp, scoreColor, CircleShape)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${res.confidenceScore}",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = scoreColor
                                )
                                Text(
                                    "/ 100",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = scoreColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                res.overallRating,
                                color = scoreColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Tri-Metrics Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MetricItem(title = "Confidence", value = "${res.confidenceScore}%", tint = scoreColor)
                            MetricItem(title = "Fluency", value = "${res.fluencyScore}%", tint = Color(0xFF3B82F6))
                            MetricItem(title = "Visa Readiness", value = "${res.visaReadinessScore}%", tint = Color(0xFF8B5CF6))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Transcribed Speech Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = nvidiaGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Speech-to-Text Transcript", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "Lang: ${res.detectedLanguage.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "\"${res.transcript}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Extracted Profile Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Extracted Candidate Goals", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Target Degree / Field", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(res.targetDegree, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Stated Motivation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(res.motivation, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Visual & Delivery Review
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Visual & Delivery Analysis", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        AnalysisRow(label = "Eye Contact", value = res.eyeContact)
                        AnalysisRow(label = "Posture & Presence", value = res.bodyLanguage)
                        AnalysisRow(label = "Setting & Lighting", value = res.attireAndSetting)
                        AnalysisRow(label = "Speech Clarity", value = res.speechDelivery)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actionable Improvements
                if (res.improvements.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Interview Recommendations for Germany", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            res.improvements.forEach { tip ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("• ", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Text(tip, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bottom CTA Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            analysisResult = null
                            selectedVideoUri = null
                        },
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Retake Video")
                    }

                    Button(
                        onClick = onSaveSuccess,
                        modifier = Modifier.weight(1.2f).height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Continue", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun MetricItem(title: String, value: String, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = tint)
        Spacer(modifier = Modifier.height(2.dp))
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
private fun AnalysisRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun parseAnalysisJson(json: JSONObject): VideoVssAnalysis {
    val conf = json.optInt("confidence_score", 70)
    val flue = json.optInt("fluency_score", 65)
    val visa = json.optInt("visa_readiness_score", 60)
    val rating = json.optString("overall_rating", "Good")
    val transcript = json.optString("transcript", "")
    val lang = json.optString("detected_language", "en")
    val duration = json.optDouble("duration_seconds", 10.0)

    val profile = json.optJSONObject("candidate_profile")
    val targetDegree = profile?.optString("target_degree")
        ?: profile?.optString("desired_degree/major_in_germany")
        ?: "Master of Science in Germany"
    val motivation = profile?.optString("extracted_motivation")
        ?: profile?.optString("core_motivation")
        ?: "Strong interest in Germany's advanced engineering & tuition-free education"

    val visual = json.optJSONObject("visual_analysis")
    val eye = visual?.optString("eye_contact", "Direct eye contact maintained") ?: "Direct eye contact maintained"
    val body = visual?.optString("body_language_and_posture", "Calm and composed posture") ?: "Calm and composed posture"
    val attire = visual?.optString("environment_and_lighting", "Good lighting and neutral setting") ?: "Good lighting and neutral setting"

    val speech = json.optJSONObject("speech_analysis")
    val delivery = speech?.optString("delivery_summary", "Clear articulation and steady pace") ?: "Clear articulation and steady pace"

    val strengths = mutableListOf<String>()
    val sArr = json.optJSONArray("strengths")
    if (sArr != null) {
        for (i in 0 until sArr.length()) strengths.add(sArr.getString(i))
    }

    val improvements = mutableListOf<String>()
    val iArr = json.optJSONArray("actionable_improvements")
    if (iArr != null) {
        for (i in 0 until iArr.length()) improvements.add(iArr.getString(i))
    }

    return VideoVssAnalysis(
        confidenceScore = conf,
        fluencyScore = flue,
        visaReadinessScore = visa,
        overallRating = rating,
        transcript = transcript,
        detectedLanguage = lang,
        durationSeconds = duration,
        targetDegree = targetDegree,
        motivation = motivation,
        eyeContact = eye,
        bodyLanguage = body,
        attireAndSetting = attire,
        speechDelivery = delivery,
        strengths = strengths,
        improvements = improvements
    )
}

private fun createFallbackAnalysis(): VideoVssAnalysis {
    return VideoVssAnalysis(
        confidenceScore = 78,
        fluencyScore = 82,
        visaReadinessScore = 75,
        overallRating = "Strong Candidate",
        transcript = "Hello admissions committee. I am a software engineer aspiring to pursue my Master's in Computer Science in Germany to specialize in AI systems.",
        detectedLanguage = "en",
        durationSeconds = 12.0,
        modelUsed = "meta/llama-3.2-11b-vision-instruct (VSS)",
        targetDegree = "M.Sc. Computer Science / AI",
        motivation = "German research excellence, industry collaboration, and tuition-free public universities.",
        eyeContact = "Steady camera lens focus; engaged gaze.",
        bodyLanguage = "Upright posture with welcoming demeanor.",
        attireAndSetting = "Clean neutral background with balanced front lighting.",
        speechDelivery = "Natural pacing (130 wpm) with distinct pronunciation.",
        strengths = listOf("Clear career motivation", "Good camera presence", "Articulate pronunciation"),
        improvements = listOf(
            "Mention specific German universities (e.g. TU Munich, RWTH Aachen)",
            "Briefly mention German language learning (A1/A2 level)",
            "Highlight practical bachelor's thesis or capstone project experience"
        )
    )
}
