package study.snacktrackmobile.presentation.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
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
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import study.snacktrackmobile.data.api.FoodApi
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import study.snacktrackmobile.data.services.AiApiService
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.components.MealsDailyView
import study.snacktrackmobile.viewmodel.FoodViewModel
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

            // Trasa dla widoku dziennego (jeśli używana niezależnie)
            composable("MealsDaily/{date}") { backStackEntry ->
                val selectedDate = backStackEntry.arguments?.getString("date") ?: ""

                // 🔹 Tworzymy zależności lokalnie dla tej trasy
                val jsonConfig = Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    coerceInputValues = true
                }

                val aiApiService = remember {
                    Retrofit.Builder()
                        .baseUrl(ApiConfig.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(AiApiService::class.java)
                }

                val foodApi = remember {
                    Retrofit.Builder()
                        .baseUrl(ApiConfig.BASE_URL)
                        .addConverterFactory(jsonConfig.asConverterFactory("application/json".toMediaType()))
                        .build()
                        .create(FoodApi::class.java)
                }

                val repo = remember { RegisteredAlimentationRepository(Request.api) }

                val localViewModel: RegisteredAlimentationViewModel = viewModel(
                    factory = RegisteredAlimentationViewModel.provideFactory(
                        repository = repo,
                        foodApi = foodApi,
                        aiService = aiApiService
                    )
                )

                MealsDailyView(
                    selectedDate = selectedDate,
                    viewModel = localViewModel,
                    navController = navController,

                    isUserPremium = false,
                    onNavigateToPremium = {
                        navController.navigate("MainView?tab=Premium")
                    },

                    onEditProduct = { _ ->
                        navController.navigate("MainView?tab=AddProduct")
                    },
                    // 🔹 DODANO BRAKUJĄCY PARAMETR
                    onAddProductClick = { mealName ->
                        // W tym widoku (standalone) nawigujemy URL-em
                        navController.navigate("MainView?tab=AddProduct&meal=$mealName&date=$selectedDate")
                    }
                )
            }
        }
    }
}