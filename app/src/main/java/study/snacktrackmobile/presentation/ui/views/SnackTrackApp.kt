package study.snacktrackmobile.presentation.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.database.AppDatabase
import study.snacktrackmobile.presentation.ui.components.MealsDailyView
import study.snacktrackmobile.presentation.ui.components.ShoppingListViewModel
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel
import study.snacktrackmobile.viewmodel.UserViewModel
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository

@Composable
fun SnackTrackApp() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val userViewModel: UserViewModel = viewModel()

    // Room DAO + ShoppingListViewModel
    val shoppingListDao = AppDatabase.getDatabase(context).shoppingListDao()
    val shoppingListViewModel: ShoppingListViewModel = viewModel<ShoppingListViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ShoppingListViewModel(shoppingListDao) as T
            }
        }
    )

    // RegisteredAlimentationRepository + ViewModel
    val api = Request.api
    val repo = RegisteredAlimentationRepository(api)
    val registeredAlimentationViewModel: RegisteredAlimentationViewModel =
        viewModel(factory = RegisteredAlimentationViewModel.provideFactory(repo))

    // === NavHost ===
    NavHost(navController = navController, startDestination = "StartView") {

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
            // Przekazujemy oba ViewModel do MainView
            MainView(
                navController = navController,
                shoppingListViewModel = shoppingListViewModel,
                registeredAlimentationViewModel = registeredAlimentationViewModel
            )
        }

        composable("InitialSurveyView") {
            InitialSurveyView(navController)
        }

        composable("MealsDaily/{date}") { backStackEntry ->
            val selectedDate = backStackEntry.arguments?.getString("date") ?: ""
            MealsDailyView(
                selectedDate = selectedDate,
                viewModel = registeredAlimentationViewModel
            )
        }
    }
}
