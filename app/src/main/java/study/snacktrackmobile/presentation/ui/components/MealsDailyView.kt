package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun MealsDailyView(
    selectedDate: String,
    viewModel: RegisteredAlimentationViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val mealsState by viewModel.meals.collectAsState()

    LaunchedEffect(selectedDate) {
        val token = TokenStorage.getToken(context) ?: return@LaunchedEffect
        viewModel.loadMeals(token, selectedDate)
    }

    // remember the meals so recomposition isn't heavy
    remember(mealsState) { mealsState }
    val defaultMeals = listOf(
        Meal(name = "Breakfast", alimentations = emptyList(), kcal = 0),
        Meal(name = "Lunch", alimentations = emptyList(), kcal = 0),
        Meal(name = "Dinner", alimentations = emptyList(), kcal = 0),
        Meal(name = "Supper", alimentations = emptyList(), kcal = 0),
        Meal(name = "Snack", alimentations = emptyList(), kcal = 0)
    )
    val mealsToDisplay = remember(mealsState) {
        defaultMeals.map { default ->
            mealsState.find { it.name == default.name } ?: default
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(mealsToDisplay) { meal ->
            MealCard(
                meal = meal,
                viewModel = viewModel,
                navController = navController,
                selectedDate = selectedDate,
                onEditProduct = { alimentation ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("editAlimentation", alimentation) // przekazujesz cały RegisteredAlimentationResponse
                }
            )
        }
    }
}
