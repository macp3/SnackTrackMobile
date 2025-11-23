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
import study.snacktrackmobile.data.api.FoodApi
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.database.AppDatabase
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.repository.RecipeRepository
import study.snacktrackmobile.presentation.ui.components.MealsDailyView
import study.snacktrackmobile.viewmodel.ShoppingListViewModel
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel
import study.snacktrackmobile.viewmodel.UserViewModel
import study.snacktrackmobile.data.repository.RegisteredAlimentationRepository
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.components.AddProductToDatabaseScreen
import study.snacktrackmobile.presentation.ui.components.ProductDetailsScreen
import study.snacktrackmobile.viewmodel.FoodViewModel
import study.snacktrackmobile.viewmodel.RecipeViewModel
import java.time.LocalDate

@Composable
fun SnackTrackApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    var startDestination by remember { mutableStateOf<String?>(null) }

    val recipeRepository = RecipeRepository(Request.recipeApi)
    val recipesViewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModel.provideFactory(recipeRepository)
    )

    val userViewModel: UserViewModel = viewModel()

    val foodViewModel: FoodViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FoodViewModel(Request.foodApi, context) as T
            }
        }
    )

    // RegisteredAlimentationRepository + ViewModel
    val api = Request.api
    val repo = RegisteredAlimentationRepository(api)
    val registeredAlimentationViewModel: RegisteredAlimentationViewModel =
        viewModel(factory = RegisteredAlimentationViewModel.provideFactory(repo))

    LaunchedEffect(Unit) {
        val existingToken = TokenStorage.getToken(context) // To jest funkcja suspend, tu zadziała!
        startDestination = if (existingToken != null) "MainView" else "StartView"
    }


    // --- 4. LOGIKA WYŚWIETLANIA ---
    if (startDestination == null) {
        // A. POKAZUJEMY EKRAN ŁADOWANIA dopóki sprawdzamy token
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator() // Kręciołek ładowania
        }
    } else {
        // B. GDY MAMY JUŻ EKRAN STARTOWY -> POKAZUJEMY NAVHOST
        NavHost(
            navController = navController,
            startDestination = startDestination!! // Wykrzyknik jest bezpieczny, bo jesteśmy w bloku else
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

                val finalEmail = if (!viewModelEmail.isNullOrBlank()) {
                    viewModelEmail!!
                } else {
                    ""
                }

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
                    onEditProduct = { alimentation ->
                        navController.navigate("MainView?tab=AddProduct")
                    }
                )
            }
        }
    }
}