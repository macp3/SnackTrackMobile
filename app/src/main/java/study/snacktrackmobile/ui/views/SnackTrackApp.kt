package study.snacktrackmobile.ui.views

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun SnackTrackApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "StartView", builder = {
        composable("StartView") {
            StartView(navController)
        }
        composable("RegisterView") {
            RegisterView(navController)
        }
        composable("LoginView") {
            LoginView(navController)
        }
        composable("MainView") {
            MainView(navController)
        }
        composable("InitialSurvey") {
            InitialSurveyView {  }
        }
    })
}