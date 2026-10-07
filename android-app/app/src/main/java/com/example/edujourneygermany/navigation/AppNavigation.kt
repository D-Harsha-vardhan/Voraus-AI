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
                    onNext = { 
                        journeyViewModel.completeStep("1")
                        navController.navigate("home") 
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
                    onNavigate = { route -> navController.navigate(route) },
                    viewModel = journeyViewModel
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
                    onNavigateToExtraction = { docType ->
                        navController.navigate("extraction_review/$docType")
                    }
                )
            }
            composable("extraction_review/{docType}") { backStackEntry ->
                val docType = backStackEntry.arguments?.getString("docType") ?: "Document"
                ExtractionReviewScreen(
                    documentType = docType,
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
