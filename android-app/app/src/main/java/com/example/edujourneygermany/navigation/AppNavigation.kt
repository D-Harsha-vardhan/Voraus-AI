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
import com.example.edujourneygermany.profile.ProfileScreen
import com.example.edujourneygermany.profile.EditProfileScreen
import com.example.edujourneygermany.profile.CvGeneratorScreen
import com.example.edujourneygermany.auth.LoginScreen
import com.example.edujourneygermany.auth.SplashScreen
import com.example.edujourneygermany.auth.RegistrationScreen
import com.example.edujourneygermany.auth.GoalSelectionScreen
import com.example.edujourneygermany.documents.DocumentsScreen
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
                    onNext = { navController.navigate("home") },
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
            composable("documents") {
                DocumentsScreen()
            }
            composable("qualification") {
                QualificationScreen()
            }
            composable("advisor") {
                AiAdvisorScreen()
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
