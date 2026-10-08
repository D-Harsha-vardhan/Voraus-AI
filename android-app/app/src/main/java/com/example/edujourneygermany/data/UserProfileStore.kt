package com.example.edujourneygermany.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object UserProfileStore {
    var email by mutableStateOf("")
    var fullName by mutableStateOf("")
    var dob by mutableStateOf("")
    var gender by mutableStateOf("")
    var nationality by mutableStateOf("")
    
    var passportNumber by mutableStateOf("")
    var phone by mutableStateOf("")
    var location by mutableStateOf("")
    
    var degree by mutableStateOf("")
    var university by mutableStateOf("")
    var graduationYear by mutableStateOf("")
    
    var role by mutableStateOf("")
    var company by mutableStateOf("")
    var experienceDuration by mutableStateOf("")
    
    var englishLevel by mutableStateOf("")
    var germanLevel by mutableStateOf("")
    
    var linkedIn by mutableStateOf("")
    var github by mutableStateOf("")
}
