package com.example.edujourneygermany.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.jan.supabase.storage.storage

object LocalDocumentManager {
    private const val PREFS_NAME = "LocalDocumentPrefs"

    // Save a document to local internal storage and record its path in SharedPreferences
    suspend fun saveDocumentLocally(context: Context, docType: String, uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val docsDir = File(context.filesDir, "cached_documents")
                    if (!docsDir.exists()) docsDir.mkdirs()

                    val extension = "jpg" // Assume jpg for simplicity, or could extract from type
                    val localFile = File(docsDir, "${docType.replace(" ", "_")}.$extension")
                    
                    val outputStream = FileOutputStream(localFile)
                    inputStream.copyTo(outputStream)
                    
                    inputStream.close()
                    outputStream.close()

                    // Save path to SharedPreferences
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putString(docType, localFile.absolutePath).apply()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Save a document directly from bytes (e.g. downloaded from Supabase)
    suspend fun saveDocumentBytesLocally(context: Context, docType: String, bytes: ByteArray) {
        withContext(Dispatchers.IO) {
            try {
                val docsDir = File(context.filesDir, "cached_documents")
                if (!docsDir.exists()) docsDir.mkdirs()

                val localFile = File(docsDir, "${docType.replace(" ", "_")}.jpg")
                localFile.writeBytes(bytes)

                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putString(docType, localFile.absolutePath).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Get the local file path if it exists
    fun getLocalDocumentPath(context: Context, docType: String): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val path = prefs.getString(docType, null) ?: return null
        
        val file = File(path)
        if (file.exists()) {
            return path
        } else {
            // File was deleted somehow, clean up prefs
            prefs.edit().remove(docType).apply()
            return null
        }
    }
    
    // Clear all cached documents (e.g. on logout)
    fun clearCache(context: Context) {
        val docsDir = File(context.filesDir, "cached_documents")
        if (docsDir.exists()) {
            docsDir.deleteRecursively()
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
    
    // Sync all documents from Supabase to local cache
    suspend fun syncAllDocumentsFromSupabase(context: Context, email: String) {
        withContext(Dispatchers.IO) {
            try {
                val bucket = com.example.edujourneygermany.data.Supabase.client.storage["user_documents"]
                val files = bucket.list()
                val userFiles = files.filter { it.name.lowercase().startsWith(email.lowercase()) }
                
                val docTypes = mapOf(
                    "_cv" to "CV / Resume",
                    "_resume" to "CV / Resume",
                    "_degree" to "Degree Certificate",
                    "_german" to "German Certificate",
                    "_ielts" to "IELTS Certificate",
                    "_passport" to "Passport",
                    "_experience" to "Experience Letter",
                    "_marksheet" to "Marksheet"
                )
                
                userFiles.forEach { file ->
                    val lowerName = file.name.lowercase()
                    // Find matching doctype
                    val docTypeEntry = docTypes.entries.find { lowerName.contains(it.key) }
                    if (docTypeEntry != null) {
                        val docType = docTypeEntry.value
                        val bytes = bucket.downloadAuthenticated(file.name)
                        saveDocumentBytesLocally(context, docType, bytes)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    // Save CV Intro locally to persist it without Supabase schema changes
    fun saveCvIntroLocally(context: Context, intro: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("cvIntro", intro).apply()
    }
    
    fun getCvIntroLocally(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("cvIntro", "") ?: ""
    }
}
