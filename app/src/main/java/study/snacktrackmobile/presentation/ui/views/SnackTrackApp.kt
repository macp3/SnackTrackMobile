package study.snacktrackmobile.presentation.ui.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import study.snacktrackmobile.presentation.ui.components.AddProductToDatabaseScreen
import study.snacktrackmobile.presentation.ui.components.ProductDetailsScreen
import study.snacktrackmobile.viewmodel.FoodViewModel
import study.snacktrackmobile.viewmodel.RecipeViewModel
import java.time.LocalDate

@Composable
fun SnackTrackApp() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val recipeRepository = RecipeRepository(Request.recipeApi)
    val recipesViewModel: RecipeViewModel = viewModel(
        factory = RecipeViewModel.provideFactory(recipeRepository)
    )

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

        // ✅ Poprawiona trasa MainView: dodanie opcjonalnych argumentów dla głębokiej nawigacji
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
            val loggedUserEmail by userViewModel.currentUserEmail.collectAsState()
            val initialTab = backStackEntry.arguments?.getString("tab") ?: "Meals"
            val initialMeal = backStackEntry.arguments?.getString("meal") ?: "Breakfast"
            val initialDate = backStackEntry.arguments?.getString("date") ?: LocalDate.now().toString()

            MainView(
                navController = navController,
                shoppingListViewModel = shoppingListViewModel,
                registeredAlimentationViewModel = registeredAlimentationViewModel,
                loggedUserEmail = loggedUserEmail ?: "",
                initialTab = initialTab, // Przekazywanie initial states
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
                    // Tutaj decydujesz, co zrobić po kliknięciu produktu
                    // np. ustawienie stanu w MainView lub nawigacja:
                    navController.navigate("MainView?tab=AddProduct") {
                        // dodatkowe opcje jeśli trzeba
                    }
                }
            )
        }
    }
}