package com.example.edujourneygermany.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.example.edujourneygermany.presentation.JourneyViewModel
import com.example.edujourneygermany.presentation.components.FloatingNavigationBar
import com.example.edujourneygermany.presentation.HomeDashboardScreen
import com.example.edujourneygermany.presentation.VerificationScreen
import com.example.edujourneygermany.presentation.ConsultantDashboardScreen
import com.example.edujourneygermany.profile.ProfileScreen
import com.example.edujourneygermany.profile.EditProfileScreen
import com.example.edujourneygermany.profile.CvGeneratorScreen
import com.example.edujourneygermany.profile.VideoIntroScreen
import com.example.edujourneygermany.auth.LoginScreen
import com.example.edujourneygermany.auth.SplashScreen
import com.example.edujourneygermany.auth.RegistrationScreen
import com.example.edujourneygermany.auth.GoalSelectionScreen
import com.example.edujourneygermany.auth.OnboardingAboutYouScreen
import com.example.edujourneygermany.auth.OnboardingExtendedProfileScreen
import com.example.edujourneygermany.auth.OnboardingDocumentsScreen
import com.example.edujourneygermany.documents.DocumentsScreen
import com.example.edujourneygermany.documents.ExtractionReviewScreen
import com.example.edujourneygermany.qualification.QualificationScreen
import com.example.edujourneygermany.advisor.AiAdvisorScreen
import com.example.edujourneygermany.opportunities.OpportunitiesScreen
import com.example.edujourneygermany.notifications.NotificationsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val journeyViewModel: JourneyViewModel = viewModel()
    val sharedUniversityViewModel: com.example.edujourneygermany.presentation.SharedUniversityViewModel = viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Screens where the floating nav bar should be visible
    val bottomBarRoutes = listOf("home", "documents", "qualification", "advisor", "opportunities", "profile")

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") {
                SplashScreen(
                    onGetStarted = { navController.navigate("register") },
                    onLogin = { navController.navigate("login") },
                    onAlreadyLoggedIn = { 
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }
            composable("register") {
                RegistrationScreen(
                    onRegisterSuccess = { navController.navigate("goal") },
                    onLoginClick = { navController.navigate("login") }
                )
            }
            composable("goal") {
                GoalSelectionScreen(
                    onNext = { 
                        journeyViewModel.completeStep("1")
                        navController.navigate("onboarding_about_you") 
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("onboarding_about_you") {
                // Assuming OnboardingAboutYouScreen is imported
                com.example.edujourneygermany.auth.OnboardingAboutYouScreen(
                    onNext = { 
                        journeyViewModel.completeStep("2")
                        navController.navigate("onboarding_documents") 
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("onboarding_documents") {
                com.example.edujourneygermany.auth.OnboardingDocumentsScreen(
                    navController = navController,
                    onNext = { 
                        journeyViewModel.completeStep("3")
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = false }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { navController.navigate("home") }
                )
            }
            composable("home") {
                HomeDashboardScreen(
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onNavigateToDocuments = { navController.navigate("documents") },
                    onNavigateToQualification = { navController.navigate("qualification") },
                    onNavigateToEditProfile = { navController.navigate("edit_profile") },
                    onLogout = {
                        com.example.edujourneygermany.data.LocalDocumentManager.clearCache(context)
                        navController.navigate("splash") {
                            popUpTo(0)
                        }
                    }
                )
            }
            composable("edit_profile") {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { 
                        journeyViewModel.completeStep("2")
                        navController.popBackStack() 
                    }
                )
            }
            composable("cv") {
                CvGeneratorScreen()
            }
            composable("video_intro") {
                VideoIntroScreen(
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = {
                        journeyViewModel.completeStep("4")
                        navController.popBackStack()
                    }
                )
            }
            composable("documents") {
                DocumentsScreen(
                    onNavigateToExtraction = { docType, uri ->
                        val encodedDocType = java.net.URLEncoder.encode(docType, "UTF-8")
                        val encodedUri = java.net.URLEncoder.encode(uri, "UTF-8")
                        navController.navigate("extraction_review/$encodedDocType?uri=$encodedUri")
                    }
                )
            }
            composable(
                "extraction_review/{docType}?uri={uri}",
                arguments = listOf(
                    androidx.navigation.navArgument("uri") { 
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val docType = backStackEntry.arguments?.getString("docType") ?: "Document"
                val uriStr = backStackEntry.arguments?.getString("uri")
                val uri = if (!uriStr.isNullOrEmpty()) android.net.Uri.parse(uriStr) else null
                ExtractionReviewScreen(
                    documentType = docType,
                    imageUri = uri,
                    onConfirm = { 
                        journeyViewModel.completeStep("3")
                        navController.popBackStack() 
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("qualification") {
                QualificationScreen()
            }
            composable("merge_profile") {
                // Dummy screen for AI merge, immediately finishes and goes to verification
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1000)
                    journeyViewModel.completeStep("5")
                    navController.navigate("verification") {
                        popUpTo("home")
                    }
                }
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = com.example.edujourneygermany.theme.PrimaryBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("AI is merging your profile...")
                    }
                }
            }
            composable("verification") {
                VerificationScreen(
                    onVerificationComplete = { 
                        journeyViewModel.completeStep("6")
                        navController.navigate("home") 
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("advisor") {
                AiAdvisorScreen(
                    onBookConsultant = { navController.navigate("consultant_dashboard") },
                    onViewDetails = { uniName, programName, matchScore ->
                        val encodedUni = android.net.Uri.encode(uniName)
                        val encodedProg = android.net.Uri.encode(programName)
                        val encodedScore = android.net.Uri.encode(matchScore)
                        navController.navigate("university_details/$encodedUni/$encodedProg/$encodedScore")
                    },
                    sharedViewModel = sharedUniversityViewModel
                )
            }
            composable("university_details/{uniName}/{programName}/{matchScore}?tab={tab}") { backStackEntry ->
                val uniName = backStackEntry.arguments?.getString("uniName") ?: ""
                val programName = backStackEntry.arguments?.getString("programName") ?: ""
                val matchScore = backStackEntry.arguments?.getString("matchScore") ?: ""
                val tabString = backStackEntry.arguments?.getString("tab")
                val initialTab = tabString?.toIntOrNull() ?: 0
                com.example.edujourneygermany.presentation.UniversityDetailsScreen(
                    uniName = uniName,
                    programName = programName,
                    matchScore = matchScore,
                    initialTab = initialTab,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("consultant_dashboard") {
                ConsultantDashboardScreen(
                    onLogout = { 
                        com.example.edujourneygermany.data.LocalDocumentManager.clearCache(context)
                        navController.navigate("register") { popUpTo(0) } 
                    }
                )
            }
            composable("opportunities") {
                OpportunitiesScreen(
                    sharedViewModel = sharedUniversityViewModel,
                    onViewRequirements = { uniName, programName, matchScore ->
                        val encodedUni = android.net.Uri.encode(uniName)
                        val encodedProg = android.net.Uri.encode(programName)
                        val encodedScore = android.net.Uri.encode(matchScore.replace("%", ""))
                        navController.navigate("university_details/$encodedUni/$encodedProg/$encodedScore?tab=1")
                    }
                )
            }
            composable("notifications") {
                NotificationsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
        
        // Overlay Floating Navigation Bar
        if (currentRoute in bottomBarRoutes) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                FloatingNavigationBar(
                    currentRoute = currentRoute ?: "home",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
