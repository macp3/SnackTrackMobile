package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.data.model.Meal
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun MealsDailyView(
    selectedDate: String,
    viewModel: RegisteredAlimentationViewModel,
    navController: NavController,
    isUserPremium: Boolean,
    onNavigateToPremium: () -> Unit,
    onEditProduct: (RegisteredAlimentationResponse) -> Unit,
    onAddProductClick: (String) -> Unit
) {
    val context = LocalContext.current
    val mealsState by viewModel.meals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAiDialog by remember { mutableStateOf(false) }
    var showPremiumUpsellDialog by remember { mutableStateOf(false) }
    var aiPrompt by remember { mutableStateOf("") }

    LaunchedEffect(selectedDate) {
        val token = TokenStorage.getToken(context) ?: return@LaunchedEffect
        viewModel.loadMeals(token, selectedDate)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2E7D32))
            }
        } else {
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isUserPremium) {
                                    showAiDialog = true
                                } else {
                                    showPremiumUpsellDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isUserPremium) Color(0xFFD1C4E9) else Color(0xFFE0E0E0),
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            if (isUserPremium) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isUserPremium) "Auto Plan" else "Auto Plan (Pro)",
                                color = if (isUserPremium) Color.Black else Color.Gray,
                                fontFamily = montserratFont
                            )
                        }
                    }
                }

                items(mealsToDisplay) { meal ->
                    MealCard(
                        meal = meal,
                        viewModel = viewModel,
                        selectedDate = selectedDate,
                        onEditProduct = onEditProduct,
                        onAddProductClick = onAddProductClick
                    )
                }
            }
        }

        if (showAiDialog) {
            AlertDialog(
                containerColor = Color.White,
                titleContentColor = Color.Black,
                textContentColor = Color.Black,
                onDismissRequest = { showAiDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFF673AB7))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Diet Planner", fontFamily = montserratFont)
                    }
                },
                text = {
                    Column {
                        Text(
                            "Describe your goal for today (e.g. 'High protein', 'Vegetarian', 'Around 2000 kcal').",
                            fontFamily = montserratFont,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = aiPrompt,
                            onValueChange = { aiPrompt = it },
                            placeholder = { Text("e.g. Healthy and light", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black,
                                focusedBorderColor = Color(0xFF673AB7),
                                unfocusedBorderColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Note: AI will search your database for best matches. This may take a few seconds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            fontFamily = montserratFont
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAiDialog = false
                            viewModel.generateAiDiet(context, selectedDate, aiPrompt)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                    ) {
                        Text("Generate Plan", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAiDialog = false }) { Text("Cancel", color = Color.Gray) }
                }
            )
        }

        if (showPremiumUpsellDialog) {
            AlertDialog(
                containerColor = Color.White,
                onDismissRequest = { showPremiumUpsellDialog = false },
                icon = {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(48.dp))
                },
                title = { Text("Premium Feature", color = Color.Black, fontFamily = montserratFont) },
                text = {
                    Text(
                        "AI Diet Planner is available only for Premium users.\n\nUpgrade now to generate full daily meal plans tailored to your needs!",
                        textAlign = TextAlign.Center,
                        color = Color.Black,
                        fontFamily = montserratFont
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showPremiumUpsellDialog = false
                            onNavigateToPremium()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Go Premium", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPremiumUpsellDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }
}
