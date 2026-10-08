package com.example.edujourneygermany.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.edujourneygermany.data.Supabase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object DocumentMemory {
    val extractedDataCache = mutableMapOf<String, Map<String, String>>()
}

object OcrHelper {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractData(
        context: Context,
        uri: Uri,
        documentType: String
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, String>()
        
        val expectedKeysList = when (documentType) {
            "Passport" -> listOf("full_name", "passport_number", "location", "date_of_expiry", "date_of_birth", "nationality")
            "Degree", "Degree Certificate", "Marksheet" -> listOf("degree", "university", "graduation_year", "student_name", "date_of_issue")
            "EnglishLanguage", "IELTS Certificate" -> listOf("english_level", "candidate_name", "test_date", "certificate_number")
            "GermanLanguage", "German Certificate" -> listOf("german_level", "candidate_name", "test_date", "certificate_number")
            "Resume", "CV", "Experience Letter" -> listOf("phone_number", "role", "company", "location", "employee_name", "employment_start_date", "employment_end_date", "professional_summary")
            else -> listOf("full_name", "passport_number", "phone_number", "location", "degree", "university", "graduation_year", "role", "company", "date_of_expiry", "candidate_name", "test_date", "student_name")
        }
        
        // Pre-fill updates with empty strings so fields ALWAYS appear in the UI, even if image load fails
        expectedKeysList.forEach { updates[it] = "" }
        val targetKeys = expectedKeysList.joinToString(", ")

        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext updates
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (originalBitmap == null) {
                updates["api_error"] = "Could not decode image. Please ensure you uploaded a valid image file (JPEG, PNG), not a PDF."
                return@withContext updates
            }
            
            // Downscale to max 2048 dimension
            val maxDim = 2048
            val scale = Math.min(maxDim.toFloat() / originalBitmap.width, maxDim.toFloat() / originalBitmap.height)
            val scaledBitmap = if (scale < 1) {
                android.graphics.Bitmap.createScaledBitmap(originalBitmap, (originalBitmap.width * scale).toInt(), (originalBitmap.height * scale).toInt(), true)
            } else {
                originalBitmap
            }
            
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            
            val base64String = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            val dataUrl = "data:image/jpeg;base64,$base64String"

            val payload = org.json.JSONObject()
            val messages = org.json.JSONArray()
            val message = org.json.JSONObject()
            val content = org.json.JSONArray()

            val systemMessage = org.json.JSONObject()
            systemMessage.put("role", "system")
            systemMessage.put("content", "You are a data extraction API. You MUST return ONLY a valid JSON object. Do not add any conversational text, preamble, markdown formatting, or bold text. Your output must start exactly with { and end with }. IMPORTANT: YOU MUST TRANSLATE ALL EXTRACTED TEXT AND VALUES INTO THE GERMAN LANGUAGE BEFORE RETURNING THE JSON.")
            messages.put(systemMessage)

            val imageObj = org.json.JSONObject()
            val imageUrlObj = org.json.JSONObject()
            imageUrlObj.put("url", dataUrl)
            imageObj.put("image_url", imageUrlObj)
            imageObj.put("type", "image_url")

            val textObj = org.json.JSONObject()
            textObj.put("type", "text")
            val contextHint = when {
                documentType == "Passport" -> " Note: For 'full_name', combine the Given Name and Surname in the exact format 'Given Name Surname' (e.g. 'Kartikey Sharma'). Do NOT reverse them. Do NOT duplicate names from the MRZ. Also accurately extract 'date_of_expiry', 'date_of_birth', and 'nationality' for security verification."
                documentType.contains("Language") || documentType.contains("Certificate") && !documentType.contains("Degree") -> " Note: For language certificates, extract the overall 'CEFR Level' (e.g., B1, B2). Also extract 'candidate_name', 'test_date', and 'certificate_number'. If a field like certificate_number is not visible on the document, return an empty string."
                documentType.contains("Degree") || documentType == "Marksheet" -> " Note: Extract 'student_name' and 'date_of_issue' for security verification. If a field is not present, return an empty string."
                documentType == "Resume" || documentType == "CV" || documentType == "Experience Letter" -> " Note: Extract 'employee_name', 'employment_start_date', and 'employment_end_date' if available. You MUST extract the introductory summary or objective statement as 'professional_summary' and translate it to German."
                else -> ""
            }
            
            textObj.put("text", "Extract the following specific fields from the image: $targetKeys.$contextHint Return ONLY a valid JSON object containing EXACTLY these keys. Translate all extracted values into GERMAN. If a value cannot be found, set its value to an empty string. Do not add any conversational text or markdown blocks.")


            content.put(imageObj)
            content.put(textObj)
            message.put("content", content)
            message.put("role", "user")
            messages.put(message)

            payload.put("messages", messages)
            payload.put("model", "meta/llama-3.2-11b-vision-instruct")
            payload.put("max_tokens", 2048)
            payload.put("temperature", 0.0)

            val url = java.net.URL("https://integrate.api.nvidia.com/v1/chat/completions")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer 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")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.connectTimeout = 30000 // 30 seconds
            connection.readTimeout = 60000 // 60 seconds
            connection.doOutput = true

            java.io.OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            Log.d("OcrHelper", "Sent payload to NVIDIA API, waiting for response...")
            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("OcrHelper", "Received 200 OK: $response")
                val responseJson = org.json.JSONObject(response)
                var responseText = responseJson.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                
                try {
                    val startIndex = responseText.indexOf('{')
                    val endIndex = responseText.lastIndexOf('}')
                    if (startIndex != -1 && endIndex != -1 && endIndex >= startIndex) {
                        val jsonStr = responseText.substring(startIndex, endIndex + 1)
                        val extractedJson = org.json.JSONObject(jsonStr)
                        val keys = extractedJson.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = extractedJson.get(key).toString()
                            if (!value.equals("Not Found", ignoreCase = true) && !value.equals("null", ignoreCase = true) && value.isNotBlank()) {
                                updates[key] = value
                            }
                        }
                    } else {
                        Log.e("OcrHelper", "No JSON found in response: $responseText")
                    }
                } catch (e: Exception) {
                    Log.e("OcrHelper", "Failed to parse JSON from VLM response: $responseText", e)
                }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error stream"
                Log.e("OcrHelper", "NVIDIA API Error (${connection.responseCode}): $error")
                updates["api_error"] = "Error ${connection.responseCode}: $error"
            }

        } catch (e: Exception) {
            Log.e("OcrHelper", "Exception during NVIDIA API call", e)
            e.printStackTrace()
        }
        return@withContext updates
    }

    suspend fun uploadDocumentAndData(
        context: Context,
        uri: Uri,
        documentType: String,
        updates: Map<String, String>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Upload image to Supabase Storage
            val bytes = context.contentResolver.openInputStream(uri)?.readBytes()
            if (bytes != null) {
                val filename = "${UserSession.userEmail}_${documentType}_${UUID.randomUUID()}.jpg"
                Supabase.client.storage["user_documents"].upload(filename, bytes) {
                    upsert = false
                }
            }

            // Update the profiles table with final confirmed data
            if (updates.isNotEmpty()) {
                val userId = Supabase.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val allowedColumns = setOf(
                        "full_name", "passport_number", "phone_number", 
                        "location", "degree", "university", "graduation_year", 
                        "role", "company", "english_level", "german_level",
                        "date_of_birth", "nationality", "professional_summary"
                    )
                    val validUpdates = updates.filterKeys { it in allowedColumns }
                    
                    if (validUpdates.isNotEmpty()) {
                        val updateJson = buildJsonObject {
                            validUpdates.forEach { (key, value) ->
                                put(key, value)
                            }
                        }
                        Supabase.client.postgrest["profiles"].update(updateJson) {
                            filter {
                                eq("id", userId)
                            }
                        }
                    }
                }
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
