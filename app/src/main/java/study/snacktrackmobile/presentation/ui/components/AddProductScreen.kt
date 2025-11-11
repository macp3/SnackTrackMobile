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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.viewmodel.FoodViewModel
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun AddProductScreen(
    selectedDate: String,
    selectedMeal: String,
    navController: NavController,
    foodViewModel: FoodViewModel,
    onProductClick: (EssentialFoodResponse) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allFoods by foodViewModel.foods.collectAsState()

    val context = LocalContext.current
    val filteredProducts = allFoods.filter { it.name.toString().contains(searchQuery, ignoreCase = true) }

    // pobranie danych przy pierwszym wejściu
    LaunchedEffect(Unit) {
        val token = TokenStorage.getToken(context)
        if (token != null) {
            foodViewModel.fetchAllFoods(token)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nagłówek
        Text(
            text = selectedMeal,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
            color = Color.Black,
            fontFamily = montserratFont,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
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
                isError = false,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Lista produktów z backendu
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredProducts) { product ->
                ProductItem(product) { clicked ->
                    onProductClick(clicked) // ✅ zamiast navController.navigate
                }
            }
        }

        // Przyciski
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DisplayButton(
                "Add new product",
                onClick = { navController.navigate("MainView?tab=AddProductToDatabase&meal=$selectedMeal&date=$selectedDate") },
                modifier = Modifier.size(width = 120.dp, height = 60.dp),
                fontSize = 14
            )
            Spacer(modifier = Modifier.width(16.dp))
            DisplayButton("Add meal", onClick = {}, modifier = Modifier.size(width = 120.dp, height = 60.dp), fontSize = 14)
        }
    }
}

@Composable
fun ProductItem(product: EssentialFoodResponse, onClick: (EssentialFoodResponse) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFEDEDED), RoundedCornerShape(8.dp))
            .clickable { onClick(product) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = product.name ?: "", fontFamily = montserratFont, fontSize = 16.sp)
    }
}

