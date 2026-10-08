package com.example.edujourneygermany.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

data class UniversityProfile(
    val id: String,
    val name: String,
    val city: String,
    val state: String,
    val type: String,
    val publicOrPrivate: String,
    val about: String,
    val website: String,
    val applyLink: String,
    val imageUrl: String
)

data class ProgramDetails(
    val duration: String,
    val ects: String,
    val tuition: String,
    val semesterContribution: String,
    val language: String,
    val ielts: String,
    val requirements: String,
    val deadlineNotes: String
)

class UniversityDetailsViewModel : ViewModel() {
    private val _universityProfile = MutableStateFlow<UniversityProfile?>(null)
    val universityProfile: StateFlow<UniversityProfile?> = _universityProfile.asStateFlow()

    private val _programDetails = MutableStateFlow<ProgramDetails?>(null)
    val programDetails: StateFlow<ProgramDetails?> = _programDetails.asStateFlow()

    fun loadDetails(context: Context, uniName: String, programName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Load 24_university_profiles.csv
                val profileIs = context.assets.open("germany_csv/24_university_profiles.csv")
                val profileReader = BufferedReader(InputStreamReader(profileIs))
                profileReader.readLine() // skip header
                var line: String?
                var foundUni: UniversityProfile? = null
                while (profileReader.readLine().also { line = it } != null) {
                    if (line.isNullOrBlank()) continue
                    val parts = line!!.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).map { it.trim('\"', ' ') }
                    if (parts.size >= 7 && (uniName.contains(parts[1], ignoreCase = true) || parts[1].contains(uniName, ignoreCase = true))) {
                        foundUni = UniversityProfile(
                            id = parts[0],
                            name = parts[1],
                            city = parts[2],
                            state = parts[3],
                            type = parts[4],
                            publicOrPrivate = parts[5],
                            about = parts[6],
                            website = if (parts.size > 7) parts[7] else "",
                            applyLink = if (parts.size > 8) parts[8] else "",
                            imageUrl = if (parts.size > 11) parts[11] else ""
                        )
                        break
                    }
                }
                profileReader.close()
                _universityProfile.value = foundUni

                // Load 25_program_admission_details.csv
                val programIs = context.assets.open("germany_csv/25_program_admission_details.csv")
                val programReader = BufferedReader(InputStreamReader(programIs))
                programReader.readLine() // skip header
                var foundProgram: ProgramDetails? = null
                while (programReader.readLine().also { line = it } != null) {
                    if (line.isNullOrBlank()) continue
                    val parts = line!!.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).map { it.trim('\"', ' ') }
                    if (parts.size >= 10) {
                        val csvUni = parts[2]
                        val csvProgram = parts[3]
                        if ((uniName.contains(csvUni, ignoreCase = true) || csvUni.contains(uniName, ignoreCase = true)) &&
                            (programName.contains(csvProgram, ignoreCase = true) || csvProgram.contains(programName, ignoreCase = true))) {
                            foundProgram = ProgramDetails(
                                duration = parts[5],
                                ects = parts[6],
                                tuition = parts[7],
                                semesterContribution = parts[8],
                                language = parts[9],
                                ielts = if (parts.size > 11) parts[11] else "",
                                requirements = if (parts.size > 18) parts[18] else "",
                                deadlineNotes = if (parts.size > 24) parts[24] else ""
                            )
                            break
                        }
                    }
                }
                programReader.close()
                _programDetails.value = foundProgram
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
