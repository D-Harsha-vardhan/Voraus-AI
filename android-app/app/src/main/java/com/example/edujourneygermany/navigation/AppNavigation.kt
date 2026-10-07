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
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Screens where the floating nav bar should be visible
    val bottomBarRoutes = listOf("home", "documents", "qualification", "advisor", "opportunities", "profile")

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") {
                SplashScreen(
                    onGetStarted = { navController.navigate("register") },
                    onLogin = { navController.navigate("login") }
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
                    onNext = { navController.navigate("onboarding_about_you") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("onboarding_about_you") {
                OnboardingAboutYouScreen(
                    onNext = { navController.navigate("onboarding_documents") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("onboarding_documents") {
                OnboardingDocumentsScreen(
                    navController = navController,
                    onNext = { 
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
                    onNavigateToEditProfile = { navController.navigate("edit_profile") }
                )
            }
            composable("edit_profile") {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
            composable("cv") {
                CvGeneratorScreen()
            }
            composable("video_intro") {
                VideoIntroScreen(onBack = { navController.popBackStack() })
            }
            composable("documents") {
                DocumentsScreen(
                    onNavigateToExtraction = { docType ->
                        navController.navigate("extraction_review/$docType")
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
                    onConfirm = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("qualification") {
                QualificationScreen()
            }
            composable("verification") {
                VerificationScreen(
                    onVerificationComplete = { navController.navigate("home") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("advisor") {
                AiAdvisorScreen(onBookConsultant = { navController.navigate("consultant_dashboard") })
            }
            composable("consultant_dashboard") {
                ConsultantDashboardScreen(
                    onLogout = { navController.navigate("register") { popUpTo(0) } }
                )
            }
            composable("opportunities") {
                OpportunitiesScreen()
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
