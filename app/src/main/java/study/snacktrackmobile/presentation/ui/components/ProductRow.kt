package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import study.snacktrackmobile.data.model.Product
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun ProductRow(
    alimentation: RegisteredAlimentationResponse,
    onDelete: (Int) -> Unit,
    onEdit: (RegisteredAlimentationResponse) -> Unit
) {
    // Zmienne wyświetlane w UI
    var name = "Unknown"
    var amountText = "-"
    var kcal = 0.0
    var protein = 0.0
    var fat = 0.0
    var carbs = 0.0

    // 1. PRZYPADEK: TO JEST PRZEPIS (Backend: Meal)
    if (alimentation.meal != null) {
        val recipe = alimentation.meal // To jest obiekt MealResponse
        name = recipe.name // Nazwa przepisu, np. "Pancakes"

        // Ilość porcji (zapisana w registered alimentation jako pieces)
        val servings = alimentation.pieces ?: 1f
        amountText = if (servings == 1f) "1 serving" else "$servings servings"

        // Iterujemy po składnikach przepisu, aby obliczyć makro
        recipe.ingredients.forEach { ing ->
            val ef = ing.essentialFood
            val api = ing.essentialApi

            val baseWeight = (ef?.defaultWeight ?: 100f).toDouble()

            // Ile tego składnika jest w przepisie
            val iAmount = ing.amount ?: 0f
            val iPieces = ing.pieces ?: 0f

            val ratio = when {
                iPieces > 0 -> (iPieces * baseWeight) / 100.0
                iAmount > 0 -> iAmount.toDouble() / 100.0
                else -> 0.0
            }

            // Pobieramy wartości (z EF lub API) i sumujemy
            kcal += (ef?.calories ?: api?.calorie?.toFloat() ?: 0f) * ratio
            protein += (ef?.protein ?: api?.protein ?: 0f) * ratio
            fat += (ef?.fat ?: api?.fat ?: 0f) * ratio
            carbs += (ef?.carbohydrates ?: api?.carbohydrates ?: 0f) * ratio
        }

        // Mnożymy sumę składników przez liczbę zjedzonych porcji
        kcal *= servings
        protein *= servings
        fat *= servings
        carbs *= servings

    }
    // 2. PRZYPADEK: TO JEST POJEDYNCZY PRODUKT
    else {
        val food = alimentation.essentialFood ?: alimentation.mealApi
        if (food != null) {
            name = if (alimentation.essentialFood != null) alimentation.essentialFood.name?:"-" else (alimentation.mealApi?.name?: "-")

            amountText = when {
                alimentation.pieces != null && alimentation.pieces > 0 ->
                    "${alimentation.pieces} piece${if (alimentation.pieces > 1) "s" else ""}"
                alimentation.amount != null && alimentation.amount > 0f ->
                    "${String.format("%.1f", alimentation.amount)} g"
                else -> "-"
            }

            // Helper do obliczania (możesz użyć funkcji calcNutrient lub przenieść logikę tutaj)
            val baseWeight = (alimentation.essentialFood?.defaultWeight ?: 100f).toDouble()
            val userAmount = alimentation.amount ?: 0f
            val userPieces = alimentation.pieces ?: 0f

            val ratio = when {
                userPieces > 0 -> (userPieces * baseWeight) / 100.0
                userAmount > 0 -> userAmount.toDouble() / 100.0
                else -> 0.0
            }

            val baseKcal = (alimentation.essentialFood?.calories ?: alimentation.mealApi?.calorie?.toFloat() ?: 0f).toDouble()
            val baseP = (alimentation.essentialFood?.protein ?: alimentation.mealApi?.protein ?: 0f).toDouble()
            val baseF = (alimentation.essentialFood?.fat ?: alimentation.mealApi?.fat ?: 0f).toDouble()
            val baseC = (alimentation.essentialFood?.carbohydrates ?: alimentation.mealApi?.carbohydrates ?: 0f).toDouble()

            kcal = baseKcal * ratio
            protein = baseP * ratio
            fat = baseF * ratio
            carbs = baseC * ratio
        }
    }

    // WIDOK KARTY
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
                .clickable { onEdit(alimentation) }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lewa strona (Nazwa + Ilość)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = montserratFont,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = montserratFont,
                    color = Color.Gray
                )
            }

            // Prawa strona (Makro)
            Row(verticalAlignment = Alignment.CenterVertically) {
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

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onDelete(alimentation.id) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}