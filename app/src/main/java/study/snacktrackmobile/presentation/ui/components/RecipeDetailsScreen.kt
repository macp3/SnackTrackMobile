package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import study.snacktrackmobile.data.model.dto.IngredientResponse
import study.snacktrackmobile.data.model.dto.RecipeResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse

@Composable
fun RecipeDetailsScreen(
    recipe: RecipeResponse,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Placeholder na zdjęcie
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (!recipe.imageUrl.isNullOrBlank()) {
                Text("Image: ${recipe.imageUrl}", color = Color.DarkGray)
            } else {
                Text("No Image Available", color = Color.DarkGray)
            }

            // Przycisk powrotu
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
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (recipe.description.isNullOrBlank()) "No description" else recipe.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Lista składników
            recipe.ingredients.forEach { ingredient ->
                // Mapowanie IngredientResponse -> RegisteredAlimentationResponse
                // Potrzebne, by wykorzystać logikę wyświetlania makro z ProductRow
                val essentialFood = ingredient.essentialFood ?: ingredient.essentialApi

                // Jeśli produkt jest nullem (co nie powinno się zdarzyć), pomijamy
                if (essentialFood != null) {

                    // Musimy rzutować EssentialFoodResponse na EssentialFoodResponse (są tożsame w DTO)
                    // Jeśli essentialApi i essentialFood to różne klasy, tu trzeba uważać.
                    // Zakładam, że w modelu DTO essentialFood ma priorytet.

                    val dummyAlimentation = RegisteredAlimentationResponse(
                        id = ingredient.id,
                        userId = 0,
                        // Tutaj trik: jeśli essentialFood jest null, bierzemy apiFood i rzutujemy/mapujemy
                        // W Twoim modelu IngredientResponse ma pola nullable dla obu.
                        essentialFood = ingredient.essentialFood, // Zakładam, że tu jest główny produkt
                        mealApi = null,
                        meal = null,
                        timestamp = "",
                        amount = ingredient.amount ?: 0f,
                        pieces = ingredient.pieces ?: 0f,
                        mealName = ""
                    )

                    // Wyświetlamy wiersz
                    // Jeśli ingredient.essentialFood jest null, a dane są w essentialApi,
                    // musisz dostosować logikę mapowania powyżej.
                    if (ingredient.essentialFood != null) {
                        RecipeDetailIngredientRow(dummyAlimentation)
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeDetailIngredientRow(
    alimentation: RegisteredAlimentationResponse
) {
    val food = alimentation.essentialFood ?: return

    // Logika wyświetlania ilości (taka sama jak w ProductRow)
    val amountText = when {
        alimentation.pieces != null && alimentation.pieces > 0 ->
            "${alimentation.pieces} piece${if (alimentation.pieces > 1) "s" else ""}"
        alimentation.amount != null && alimentation.amount > 0f ->
            "${String.format("%.1f", alimentation.amount)} g"
        else -> "-"
    }

    // Obliczanie makro
    val kcal = calcNutrient(alimentation, food.calories)
    val protein = calcNutrient(alimentation, food.protein)
    val fat = calcNutrient(alimentation, food.fat)
    val carbs = calcNutrient(alimentation, food.carbohydrates)

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
            // Lewa strona: Nazwa i ilość
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name ?: "-",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = montserratFont,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont,
                    color = Color.Gray
                )
            }

            // Prawa strona: Makro (bez przycisku usuwania)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.0f", kcal)} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont,
                    fontWeight = FontWeight.Bold
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