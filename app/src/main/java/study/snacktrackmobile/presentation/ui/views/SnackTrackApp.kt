package study.snacktrackmobile.presentation.ui.views

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import study.snacktrackmobile.viewmodel.UserViewModel

@Composable
fun SnackTrackApp() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()
    NavHost(navController = navController, startDestination = "StartView", builder = {
        composable("StartView") {
            StartView(navController)
        }
        composable("RegisterView") {
            RegisterView(navController)
        }
        composable("LoginView") {
            LoginView(navController, userViewModel)
        }
        composable("MainView") {
            MainView(navController)
        }
        composable("InitialSurvey") {
            InitialSurveyView {  }
        }
    })
}