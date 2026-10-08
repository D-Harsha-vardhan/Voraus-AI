package com.example.edujourneygermany.network

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Body

data class OpportunityDto(
    val institution: String,
    val programName: String,
    val location: String,
    val degreeType: String,
    val matchStatus: String
)

data class DocumentDto(
    val id: String,
    val type: String,
    val name: String,
    val status: String,
    val url: String?
)

data class ChatMessageDto(
    val id: String,
    val role: String, // "user" | "ai"
    val content: String
)

data class ChatRequestDto(
    val message: String
)

interface ApiService {
    @POST("opportunities/search")
    suspend fun getOpportunities(@Header("x-applicant-id") applicantId: String = "test-applicant"): List<OpportunityDto>

    @GET("documents")
    suspend fun getDocuments(@Header("x-applicant-id") applicantId: String = "test-applicant"): List<DocumentDto>

    @GET("ai/chat")
    suspend fun getChatHistory(@Header("x-applicant-id") applicantId: String = "test-applicant"): List<ChatMessageDto>

    @POST("ai/chat")
    suspend fun sendMessage(
        @Header("x-applicant-id") applicantId: String = "test-applicant",
        @Body request: ChatRequestDto
    ): ChatMessageDto

    @POST("documents/upload")
    suspend fun uploadDocument(
        @Header("x-applicant-id") applicantId: String = "test-applicant",
        @Body request: UploadDocumentRequestDto
    ): UploadDocumentResponseDto
}

data class UploadDocumentRequestDto(
    val documentType: String,
    val base64Image: String
)

data class UploadDocumentResponseDto(
    val id: String,
    val extractedData: Map<String, String>?
)
