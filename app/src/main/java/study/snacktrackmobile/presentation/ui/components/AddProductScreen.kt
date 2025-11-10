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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun AddProductScreen(
    selectedDate: String,
    selectedMeal: String,
    navController: NavController
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMeal by remember { mutableStateOf(selectedMeal) }

    val meals = listOf("Breakfast", "Lunch", "Dinner", "Supper", "Snack")

    val products = remember {
        mutableStateListOf(
            Product(1, "Apple", "100g", 52, 0.3f, 0.2f, 14f),
            Product(2, "Banana", "100g", 89, 1.1f, 0.3f, 23f),
            Product(3, "Orange", "100g", 62, 1f, 0.2f, 15f),
            Product(4, "Bread", "100g", 265, 9f, 3.2f, 49f),
            Product(5, "Milk", "100ml", 42, 3.4f, 1f, 5f),
            Product(6, "Cheese", "100g", 402, 25f, 33f, 1.3f)
        )
    }

    val filteredProducts = products.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize()) {

        // Nagłówek z wybranym posiłkiem
        Text(
            text = "Selected meal: $selectedMeal",
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            color = Color.Black
        )

        // SearchBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TextInput(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search product",
                isError = false
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scrollowalna lista produktów zajmująca pozostałe miejsce
        LazyColumn(
            modifier = Modifier
                .weight(1f) // kluczowe: wypełnia przestrzeń nad przyciskami i nie przykrywa BottomBar
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProducts) { product ->
                ProductItem(product)
            }
        }

        // Przyciski nad BottomBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DisplayButton("Add new product", onClick = {}, modifier = Modifier.size(width = 120.dp, height = 60.dp), fontSize = 14)
            Spacer(modifier = Modifier.width(16.dp)) // odstęp między przyciskami
            DisplayButton("Add meal", onClick = {}, modifier = Modifier.size(width = 120.dp, height = 60.dp), fontSize = 14)
        }
    }
}


@Composable
fun ProductItem(product: Product) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEDEDED), RoundedCornerShape(8.dp))
            .clickable { /* TODO: handle click */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = product.name)
    }
}


