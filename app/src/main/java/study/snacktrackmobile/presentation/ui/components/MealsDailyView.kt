package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun MealsDailyView(
    selectedDate: String,
    viewModel: RegisteredAlimentationViewModel,
    navController: NavController,
    onEditProduct: (RegisteredAlimentationResponse) -> Unit
) {
    val context = LocalContext.current
    val mealsState by viewModel.meals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState() // Obserwujemy stan ładowania

    LaunchedEffect(selectedDate) {
        val token = TokenStorage.getToken(context) ?: return@LaunchedEffect
        viewModel.loadMeals(token, selectedDate)
    }

    // Jeśli trwa ładowanie -> POKAŻ LOADER NA ŚRODKU
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF2E7D32))
        }
    } else {
        // Jeśli dane załadowane -> POKAŻ LISTĘ
        val defaultMeals = listOf(
            Meal(name = "Breakfast", alimentations = emptyList(), kcal = 0),
            Meal(name = "Lunch", alimentations = emptyList(), kcal = 0),
            Meal(name = "Dinner", alimentations = emptyList(), kcal = 0),
            Meal(name = "Supper", alimentations = emptyList(), kcal = 0),
            Meal(name = "Snack", alimentations = emptyList(), kcal = 0)
        )

        val mealsToDisplay = defaultMeals.map { default ->
            mealsState.find { it.name == default.name } ?: default
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mealsToDisplay) { meal ->
                MealCard(
                    meal = meal,
                    viewModel = viewModel,
                    navController = navController,
                    selectedDate = selectedDate,
                    onEditProduct = onEditProduct
                )
            }
        }
    }
}