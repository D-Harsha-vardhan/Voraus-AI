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

object OcrHelper {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractData(
        context: Context,
        uri: Uri,
        documentType: String
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, String>()
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext updates
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
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
            systemMessage.put("content", "You are a data extraction API. You MUST return ONLY a valid JSON object. Do not add any conversational text, preamble, markdown formatting, or bold text. Your output must start exactly with { and end with }.")
            messages.put(systemMessage)

            val imageObj = org.json.JSONObject()
            val imageUrlObj = org.json.JSONObject()
            imageUrlObj.put("url", dataUrl)
            imageObj.put("image_url", imageUrlObj)
            imageObj.put("type", "image_url")

            val targetKeys = when (documentType) {
                "Passport" -> "full_name, passport_number, location"
                "Degree" -> "degree, university, graduation_year"
                "EnglishLanguage" -> "english_level"
                "GermanLanguage" -> "german_level"
                "Resume" -> "phone_number, role, company, location"
                else -> "full_name, passport_number, phone_number, location, degree, university, graduation_year, role, company, english_level, german_level"
            }

            val textObj = org.json.JSONObject()
            textObj.put("type", "text")
            textObj.put("text", "Transcribe all text from this image and structure it. Return ONLY a valid JSON object. IMPORTANT: You must use the following EXACT keys if the information is present: $targetKeys. Do NOT return keys if the information is not found. Do NOT include any explanations, safety warnings, or markdown blocks.")

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
                        var count = 0
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = extractedJson.get(key).toString()
                            if (!value.equals("Not Found", ignoreCase = true) && !value.equals("null", ignoreCase = true) && value.isNotBlank()) {
                                updates[key] = value
                                count++
                            }
                        }
                        if (count == 0) updates["raw_response"] = responseText
                    } else {
                        Log.e("OcrHelper", "No JSON found in response: $responseText")
                        updates["raw_response"] = responseText
                    }
                } catch (e: Exception) {
                    Log.e("OcrHelper", "Failed to parse JSON from VLM response: $responseText", e)
                    updates["raw_response"] = responseText
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
                        "role", "company", "english_level", "german_level"
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
