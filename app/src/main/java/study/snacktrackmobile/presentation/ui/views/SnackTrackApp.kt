package study.snacktrackmobile.presentation.ui.views

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import study.snacktrackmobile.data.api.ApiService
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import study.snacktrackmobile.presentation.ui.components.MealsDailyView
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel
import study.snacktrackmobile.viewmodel.UserViewModel

@Composable
fun SnackTrackApp() {
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()
    val api = Request.api

    val repo = RegisteredAlimentationRepository(api)   // <- to jak tworzysz repo
    val registeredAlimentationViewModel: RegisteredAlimentationViewModel =
        viewModel(factory = RegisteredAlimentationViewModel.provideFactory(repo))

    NavHost(navController = navController, startDestination = "StartView", builder = {
        composable("StartView") {
            StartView(navController)
        }
        composable("RegisterView") {
            RegisterView(navController, userViewModel)
        }
        composable("LoginView") {
            LoginView(navController, userViewModel)
        }
        composable("MainView") {
            MainView(navController, registeredAlimentationViewModel)
        }
        composable("InitialSurveyView") {
            InitialSurveyView(navController)
        }
        // ✅ Ekran z posiłkami z parametrem daty
        composable("MealsDaily/{date}") { backStackEntry ->
            val selectedDate = backStackEntry.arguments?.getString("date") ?: ""

            MealsDailyView(
                selectedDate = selectedDate,
                viewModel = registeredAlimentationViewModel
            )
        }
    })
}