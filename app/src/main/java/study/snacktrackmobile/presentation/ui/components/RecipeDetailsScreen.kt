package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun RecipeDetailsScreen(
    recipe: RecipeResponse,
    isAuthor: Boolean,
    isFavourite: Boolean,
    selectedDate: String,
    registeredAlimentationViewModel: RegisteredAlimentationViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    val context = LocalContext.current
    var showMealDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // --- IMAGE PLACEHOLDER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.White.copy(alpha = 0.7f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {

                // --- PRZYCISKI AKCJI ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DisplayButton(
                        text = if(isFavourite) "Unfavourite" else "Favourite",
                        onClick = onToggleFavourite,
                        modifier = Modifier.weight(1f).height(50.dp),
                        fontSize = 12,
                        containerColor = if(isFavourite) Color(0xFFFF9999) else Color(0xFFB7F999)
                    )

                    if (isAuthor) {
                        DisplayButton(
                            text = "Edit",
                            onClick = onEdit,
                            modifier = Modifier.weight(1f).height(50.dp),
                            fontSize = 12,
                            containerColor = Color(0xFFB7F999)
                        )

                        DisplayButton(
                            text = "Delete",
                            onClick = onDelete,
                            modifier = Modifier.weight(1f).height(50.dp),
                            fontSize = 12,
                            containerColor = Color(0xFFFF9999)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (!recipe.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                // 🔹 LISTA SKŁADNIKÓW
                if (recipe.ingredients.isEmpty()) {
                    Text("No ingredients", color = Color.Gray)
                } else {
                    recipe.ingredients.forEach { ingredient ->
                        // Sprawdzamy, czy któryś ze składników istnieje
                        if (ingredient.essentialFood != null || ingredient.essentialApi != null) {

                            val dummy = RegisteredAlimentationResponse(
                                id = ingredient.id,
                                userId = 0,
                                // 1. Mapujemy lokalne jedzenie
                                essentialFood = ingredient.essentialFood,
                                // 2. Mapujemy jedzenie z API (To tutaj był problem!)
                                mealApi = ingredient.essentialApi,
                                meal = null,
                                timestamp = "",
                                amount = ingredient.amount ?: 0f,
                                pieces = ingredient.pieces ?: 0f,
                                mealName = ""
                            )

                            RecipeDetailIngredientRow(dummy)
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showMealDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = Color(0xFF2E7D32),
            contentColor = Color.White,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Add to Diary",
                    fontFamily = montserratFont,
                    color = Color.White
                )
            }
        }
    }

    // --- DIALOG WYBORU POSIŁKU ---
    if (showMealDialog) {
        var servingsInput by remember { mutableStateOf("1") }

        AlertDialog(
            onDismissRequest = { showMealDialog = false },
            title = { Text("Select Meal", fontFamily = montserratFont) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextInput(
                        value = servingsInput,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() || it == '.' }) {
                                servingsInput = newValue
                            }
                        },
                        label = "Servings",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = false
                    )

                    val meals = listOf("Breakfast", "Lunch", "Dinner", "Supper", "Snack")
                    meals.forEach { meal ->
                        val servings = servingsInput.toFloatOrNull() ?: 1f

                        Button(
                            onClick = {
                                if (servings > 0f) {
                                    registeredAlimentationViewModel.addRecipeToMeal(
                                        context = context,
                                        recipe = recipe,
                                        date = selectedDate,
                                        mealName = meal,
                                        servings = servings
                                    )
                                    showMealDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2F1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(meal, color = Color(0xFF00695C), fontFamily = montserratFont)
                                Text(
                                    text = "(${String.format("%.1f", servings)} servings)",
                                    color = Color(0xFF00695C),
                                    fontFamily = montserratFont,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMealDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RecipeDetailIngredientRow(
    alimentation: RegisteredAlimentationResponse
) {
    val name = alimentation.essentialFood?.name
        ?: alimentation.mealApi?.name
        ?: "Unknown Ingredient"

    // --- DEBUGOWANIE ---
    // Jeśli nazwa jest nieznana, wyświetlimy co siedzi w obiekcie
    if (name == "Unknown Ingredient") {
        val apiDebug = alimentation.mealApi
        val id = apiDebug?.id ?: "null"
        val rawName = apiDebug?.name ?: "null"
        Text(
            text = "DEBUG ERROR: API ID=$id, Name=$rawName",
            color = Color.Red,
            fontSize = 10.sp
        )
    }
    // -------------------

    val baseKcal = alimentation.essentialFood?.calories ?: alimentation.mealApi?.calorie?.toFloat() ?: 0f
    val baseP = alimentation.essentialFood?.protein ?: alimentation.mealApi?.protein ?: 0f
    val baseF = alimentation.essentialFood?.fat ?: alimentation.mealApi?.fat ?: 0f
    val baseC = alimentation.essentialFood?.carbohydrates ?: alimentation.mealApi?.carbohydrates ?: 0f

    val defaultWeight = alimentation.essentialFood?.defaultWeight
        ?: alimentation.mealApi?.defaultWeight
        ?: 100f

    val amountText = when {
        alimentation.pieces != null && alimentation.pieces > 0 ->
            "${alimentation.pieces} piece${if (alimentation.pieces > 1) "s" else ""}"
        alimentation.amount != null && alimentation.amount > 0f ->
            "${String.format("%.1f", alimentation.amount)} g"
        else -> "-"
    }

    fun calculateTotal(baseVal: Float): Double {
        val pieces = alimentation.pieces ?: 0f
        val grams = alimentation.amount ?: 0f
        val totalGrams = if (pieces > 0) pieces * defaultWeight else grams
        return baseVal * (totalGrams / 100.0)
    }

    val kcal = calculateTotal(baseKcal)
    val protein = calculateTotal(baseP)
    val fat = calculateTotal(baseF)
    val carbs = calculateTotal(baseC)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = montserratFont,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.0f", kcal)} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "${String.format("%.1f", protein)}P ${String.format("%.1f", fat)}F ${String.format("%.1f", carbs)}C",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont,
                    color = Color.Gray
                )
            }
        }
    }
}