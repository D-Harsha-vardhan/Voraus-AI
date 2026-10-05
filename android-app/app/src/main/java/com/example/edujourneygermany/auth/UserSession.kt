package com.example.edujourneygermany.auth

object UserSession {
    var userEmail: String = ""
    var userName: String = ""

    fun extractNameFromEmail(email: String): String {
        if (email.isBlank()) return "Applicant"
        val prefix = email.substringBefore('@')
        // Try to clean up email like rahul.sharma to Rahul Sharma
        return prefix.split(Regex("[._-]"))
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}
