package study.snacktrackmobile.presentation.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import study.snacktrackmobile.data.database.AppDatabase
import study.snacktrackmobile.presentation.ui.components.ShoppingListViewModel
import study.snacktrackmobile.viewmodel.UserViewModel

@Composable
fun SnackTrackApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()

    // Pobranie DAO z Room
    val shoppingListDao = AppDatabase.getDatabase(context).shoppingListDao()

    // Fabryka ViewModel z wstrzykniętym DAO
    val shoppingListViewModel: ShoppingListViewModel = viewModel<ShoppingListViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ShoppingListViewModel(shoppingListDao) as T
            }
        }
    )

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
            MainView(navController, shoppingListViewModel)
        }
        composable("InitialSurveyView") {
            InitialSurveyView(navController)
        }
    }
}
