package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.FoodUiItem
import study.snacktrackmobile.viewmodel.FoodViewModel

@Composable
fun AddProductScreen(
    selectedDate: String,
    selectedMeal: String,
    navController: NavController,
    foodViewModel: FoodViewModel,
    onProductClick: (RegisteredAlimentationResponse) -> Unit,
    isRecipeMode: Boolean = false,
    onAddMealClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val combinedResults by foodViewModel.combinedResults.collectAsState()
    val isLoading by foodViewModel.isLoading.collectAsState()

    var token by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        token = TokenStorage.getToken(context)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if(isRecipeMode) "Add to Recipe" else selectedMeal,
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
                onValueChange = { query ->
                    searchQuery = query
                    token?.let { jwt ->
                        foodViewModel.onSearchQueryChanged(jwt, query)
                    }
                },
                label = "Search product",
                isError = false,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(combinedResults) { item ->
                        FoodResultRow(
                            name = item.name,
                            description = item.description,
                            kcal = item.kcal,
                            quantityLabel = item.quantityLabel,
                        ) {
                            val alimentation = when (item) {
                                is FoodUiItem.Local -> RegisteredAlimentationResponse(
                                    id = -1,
                                    userId = 0,
                                    essentialFood = item.data,
                                    mealApi = null,
                                    meal = null,
                                    timestamp = selectedDate,
                                    // Logic: If the list showed 1 piece, we pre-fill pieces. Else we pre-fill default weight.
                                    amount = if(item.quantityLabel.contains("piece")) 0f else (item.data.defaultWeight ?: 100f),
                                    pieces = if(item.quantityLabel.contains("piece")) 1f else 0f,
                                    mealName = selectedMeal
                                )
                                is FoodUiItem.Api -> RegisteredAlimentationResponse(
                                    id = -1,
                                    userId = 0,
                                    essentialFood = null,
                                    mealApi = item.data,
                                    meal = null,
                                    timestamp = selectedDate,
                                    // Logic: If API is "piece" based OR we converted it to piece
                                    amount = if(item.quantityLabel.contains("piece")) 0f else 100f,
                                    pieces = if(item.quantityLabel.contains("piece")) 1f else 0f,
                                    mealName = selectedMeal
                                )
                            }
                            onProductClick(alimentation)
                        }
                    }

                    if (!isLoading && searchQuery.isNotEmpty() && combinedResults.isEmpty()) {
                        item {
                            Text(
                                "No products found.",
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                fontFamily = montserratFont
                            )
                        }
                    }
                }
            }
        }

        if (!isRecipeMode) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DisplayButton(
                    "Add new product",
                    onClick = { navController.navigate("MainView?tab=AddProductToDatabase&meal=$selectedMeal&date=$selectedDate") },
                    modifier = Modifier.size(width = 120.dp, height = 60.dp),
                    fontSize = 14
                )
                Spacer(modifier = Modifier.width(16.dp))

                DisplayButton(
                    "Add meal",
                    onClick = onAddMealClick,
                    modifier = Modifier.size(width = 120.dp, height = 60.dp),
                    fontSize = 14
                )
            }
        }
    }
}

@Composable
fun FoodResultRow(
    name: String,
    description: String,
    kcal: Float,
    quantityLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    fontFamily = montserratFont,
                    color = Color.Black
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = montserratFont,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${kcal.toInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    fontFamily = montserratFont,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = quantityLabel,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}