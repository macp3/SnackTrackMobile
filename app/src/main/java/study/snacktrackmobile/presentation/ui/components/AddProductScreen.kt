package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.viewmodel.FoodViewModel

@Composable
fun AddProductScreen(
    selectedDate: String,
    selectedMeal: String,
    navController: NavController,
    foodViewModel: FoodViewModel,
    onProductClick: (RegisteredAlimentationResponse) -> Unit,
    // 🔹 Jedyna nowość: flaga, czy jesteśmy w trybie dodawania do przepisu
    isRecipeMode: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    val allFoods by foodViewModel.foods.collectAsState()
    val context = LocalContext.current

    val filteredProducts = allFoods.filter { it.name?.contains(searchQuery, ignoreCase = true) == true }

    LaunchedEffect(Unit) {
        val token = TokenStorage.getToken(context)
        if (token != null) {
            foodViewModel.fetchAllFoods(token)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = selectedMeal,
            modifier = Modifier.padding(16.dp),
            color = Color.Black,
            fontFamily = montserratFont,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TextInput(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search product",
                isError = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tego nie ruszamy - Twoja oryginalna logika
            items(filteredProducts) { product ->
                ProductItem(product) { clicked ->
                    // Budujemy obiekt tak jak wcześniej, żeby pasował do reszty aplikacji
                    val alimentation = RegisteredAlimentationResponse(
                        id = -1,
                        userId = 0,
                        essentialFood = clicked,
                        mealApi = null,
                        meal = null,
                        timestamp = selectedDate,
                        amount = clicked.defaultWeight ?: 100f,
                        pieces = 0f,
                        mealName = selectedMeal
                    )
                    onProductClick(alimentation)
                }
            }
        }

        // 🔹 Ukrywamy przyciski tylko jeśli to tryb przepisu,
        // w przeciwnym razie zostawiamy stare przyciski
        if (!isRecipeMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DisplayButton(
                    "Add new product",
                    onClick = {
                        navController.navigate("MainView?tab=AddProductToDatabase&meal=$selectedMeal&date=$selectedDate")
                    },
                    modifier = Modifier.size(width = 120.dp, height = 60.dp),
                    fontSize = 14
                )
                Spacer(modifier = Modifier.width(16.dp))
                DisplayButton(
                    "Add meal",
                    onClick = { /* TODO: obsługa dodania całego posiłku */ },
                    modifier = Modifier.size(width = 120.dp, height = 60.dp),
                    fontSize = 14
                )
            }
        }
    }
}

// 👇 To jest Twój oryginalny komponent, który musi tu zostać
@Composable
fun ProductItem(product: EssentialFoodResponse, onClick: (EssentialFoodResponse) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Białe tło
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // Cień jak w RecipeRow/ProductRow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // 🔹 Kliknięcie w cały wiersz
                .clickable { onClick(product) }
                .padding(16.dp), // Zwiększony padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // 🔹 Nazwa Produktu
                Text(
                    text = product.name ?: "-",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    fontFamily = montserratFont,
                    color = Color.Black
                )
                Text(
                    text = product.description ?: "-",
                    style = MaterialTheme.typography.bodyLarge.copy(),
                    fontFamily = montserratFont,
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}