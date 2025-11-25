package study.snacktrackmobile.presentation.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.api.UserApi
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.data.repository.RecipeRepository
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.components.MealsDailyView
import study.snacktrackmobile.viewmodel.FoodViewModel
import study.snacktrackmobile.viewmodel.RecipeViewModel
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel
import study.snacktrackmobile.viewmodel.UserViewModel
import java.time.LocalDate

@Composable
fun SnackTrackApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    val userViewModel: UserViewModel = viewModel()

    val foodViewModel: FoodViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FoodViewModel(Request.foodApi, context) as T
            }
        }
    )

    val api = Request.api
    val repo = RegisteredAlimentationRepository(api)
    val registeredAlimentationViewModel: RegisteredAlimentationViewModel =
        viewModel(factory = RegisteredAlimentationViewModel.provideFactory(repo))

    // --- LOGIKA SPRAWDZANIA TOKENA ---
    LaunchedEffect(Unit) {
        val existingToken = TokenStorage.getToken(context)
        if (existingToken.isNullOrEmpty()) {
            startDestination = "StartView"
            return@LaunchedEffect
        }

        val bearer = if (existingToken.startsWith("Bearer ")) existingToken else "Bearer $existingToken"
        val userApi = Request.userApi

        // 1) Weryfikacja profilu
        val profileRes = try {
            userApi.getProfile(bearer)
        } catch (e: Exception) {
            null
        }

        val profileOk = profileRes?.isSuccessful == true && profileRes.body() != null
        if (!profileOk) {
            TokenStorage.clearToken(context)
            startDestination = "StartView"
            return@LaunchedEffect
        }

        // 2) refreshSurvey → decyduje o InitialSurveyView vs MainView
        val refreshRes = try {
            userApi.refreshSurvey(bearer)
        } catch (e: Exception) {
            null
        }

        startDestination = if (refreshRes?.isSuccessful == true && refreshRes.body()?.showSurvey == true) {
            "InitialSurveyView"
        } else {
            "MainView"
        }
    }

    // --- WYŚWIETLANIE ---
    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination!!
        ) {
            composable("StartView") {
                StartView(navController)
            }

            composable("RegisterView") {
                RegisterView(navController, userViewModel)
            }

            composable("LoginView") {
                LoginView(navController, userViewModel)
            }

            composable(
                route = "MainView?tab={tab}&meal={meal}&date={date}",
                arguments = listOf(
                    navArgument("tab") {
                        type = NavType.StringType
                        defaultValue = "Meals"
                    },
                    navArgument("meal") {
                        type = NavType.StringType
                        defaultValue = "Breakfast"
                    },
                    navArgument("date") {
                        type = NavType.StringType
                        defaultValue = LocalDate.now().toString()
                    }
                )
            ) { backStackEntry ->
                val viewModelEmail by userViewModel.currentUserEmail.collectAsState()
                val finalEmail = viewModelEmail ?: ""

                val initialTab = backStackEntry.arguments?.getString("tab") ?: "Meals"
                val initialMeal = backStackEntry.arguments?.getString("meal") ?: "Breakfast"
                val initialDate = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()

                MainView(
                    navController = navController,
                    registeredAlimentationViewModel = registeredAlimentationViewModel,
                    loggedUserEmail = finalEmail,
                    initialTab = initialTab,
                    initialMeal = initialMeal,
                    initialDate = initialDate,
                    foodViewModel = foodViewModel,
                    userViewModel = userViewModel
                )
            }

            composable("InitialSurveyView") {
                InitialSurveyView(navController)
            }

            composable("MealsDaily/{date}") { backStackEntry ->
                val selectedDate = backStackEntry.arguments?.getString("date") ?: ""
                MealsDailyView(
                    selectedDate = selectedDate,
                    viewModel = registeredAlimentationViewModel,
                    navController = navController,
                    onEditProduct = { _ ->
                        navController.navigate("MainView?tab=AddProduct")
                    }
                )
            }
        }
    }
}