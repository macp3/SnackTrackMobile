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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.CommentViewModel
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun RecipeDetailsScreen(
    recipe: RecipeResponse,
    isAuthor: Boolean,
    isFavourite: Boolean,
    selectedDate: String,
    registeredAlimentationViewModel: RegisteredAlimentationViewModel,
    commentViewModel: CommentViewModel,
    onReportRecipe: (String) -> Unit,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavourite: () -> Unit
) {
    val context = LocalContext.current
    var showMealDialog by remember { mutableStateOf(false) }

    // Stany dla menu opcji i zgłaszania
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp) // Padding na dole, żeby FloatingButton nie zasłaniał treści
        ) {
            // --- IMAGE HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFEEEEEE)),
                contentAlignment = Alignment.Center
            ) {
                // Wyświetlanie obrazka
                if (!recipe.imageUrl.isNullOrBlank()) {
                    val fullUrl = if (recipe.imageUrl.startsWith("http")) {
                        recipe.imageUrl
                    } else {
                        val baseUrl = ApiConfig.BASE_URL.removeSuffix("/")
                        val relativeUrl = recipe.imageUrl.removePrefix("/")
                        "$baseUrl/$relativeUrl"
                    }

                    AsyncImage(
                        model = fullUrl,
                        contentDescription = "Recipe Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder gdy brak zdjęcia
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Text("No image", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                // Back Button (Lewy górny róg)
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                // 🔹 Przycisk Opcji (Prawy górny róg) - Zgłaszanie
                Box(modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                ) {
                    IconButton(
                        onClick = { showOptionsMenu = true },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = showOptionsMenu,
                        onDismissRequest = { showOptionsMenu = false },
                        modifier = Modifier.background(Color.White) // Wymuszenie białego tła menu
                    ) {
                        DropdownMenuItem(
                            text = { Text("Report Recipe", color = Color.Black) },
                            onClick = {
                                showOptionsMenu = false
                                showReportDialog = true
                            }
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // --- PRZYCISKI AKCJI (Ulubione, Edycja, Usuwanie) ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DisplayButton(
                        text = if(isFavourite) "Unfavourite" else "Favourite",
                        onClick = onToggleFavourite,
                        modifier = Modifier.weight(1f).height(50.dp),
                        fontSize = 12,
                        containerColor = if(isFavourite) Color(0xFFFF9999) else Color(0xFFB7F999),
                    )

                    if (isAuthor) {
                        DisplayButton(
                            text = "Edit",
                            onClick = onEdit,
                            modifier = Modifier.weight(1f).height(50.dp),
                            fontSize = 12,
                            containerColor = Color(0xFFE0E0E0),
                        )

                        DisplayButton(
                            text = "Delete",
                            onClick = onDelete,
                            modifier = Modifier.weight(1f).height(50.dp),
                            fontSize = 12,
                            containerColor = Color(0xFFFF9999),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Nazwa i Opis
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
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                // Lista składników
                if (recipe.ingredients.isEmpty()) {
                    Text("No ingredients listed.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                } else {
                    recipe.ingredients.forEach { ingredient ->
                        if (ingredient.essentialFood != null || ingredient.essentialApi != null) {
                            val dummy = RegisteredAlimentationResponse(
                                id = ingredient.id,
                                userId = 0,
                                essentialFood = ingredient.essentialFood,
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

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // 🔹 SEKCJA KOMENTARZY
                CommentSection(
                    mealId = recipe.id,
                    viewModel = commentViewModel,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        // FAB (Add to Diary)
        FloatingActionButton(
            onClick = { showMealDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF2E7D32),
            contentColor = Color.White,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Add to Diary",
                    fontFamily = montserratFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // --- DIALOG DODAWANIA DO DZIENNIKA ---
    if (showMealDialog) {
        var servingsInput by remember { mutableStateOf("1") }

        AlertDialog(
            onDismissRequest = { showMealDialog = false },
            containerColor = Color.White, // Wymuszenie białego tła dialogu
            title = { Text("Add to Diary", fontFamily = montserratFont, fontWeight = FontWeight.Bold, color = Color.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("How many servings did you eat?", fontSize = 14.sp, color = Color.Black)

                    // 🔹 ZMIANA: Zamiast TextInput używamy OutlinedTextField z wymuszonymi kolorami
                    OutlinedTextField(
                        value = servingsInput,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() || it == '.' } && newValue.length <= 5) {
                                servingsInput = newValue
                            }
                        },
                        label = { Text("Servings (e.g. 1.5)", color = Color.Gray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        // 🔹 FIX KOLORÓW: Wymuszamy czarny tekst i białe tło niezależnie od trybu telefonu
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = Color(0xFF2E7D32),
                            focusedBorderColor = Color(0xFF2E7D32),
                            unfocusedBorderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text("Select meal time:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top=8.dp), color = Color.Black)

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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(meal, color = Color(0xFF00695C), fontFamily = montserratFont)
                                Text(
                                    text = "+ ${String.format("%.1f", servings)} portion(s)",
                                    color = Color(0xFF00695C),
                                    fontFamily = montserratFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMealDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // 🔹 DIALOG ZGŁASZANIA PRZEPISU
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            containerColor = Color.White,
            title = { Text("Report Recipe", color = Color.Black) },
            text = {
                Column {
                    Text("Why are you reporting this recipe?", color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Tu też wymuszamy kolory dla spójności
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Reason", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = Color(0xFF2E7D32),
                            focusedBorderColor = Color(0xFF2E7D32),
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReportRecipe(reportReason)
                        showReportDialog = false
                        reportReason = ""
                    },
                    enabled = reportReason.isNotBlank()
                ) {
                    Text("Report", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Cancel", color = Color.Gray) }
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