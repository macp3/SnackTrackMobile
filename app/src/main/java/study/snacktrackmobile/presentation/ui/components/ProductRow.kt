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
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.data.model.dto.getDisplayName
import study.snacktrackmobile.presentation.ui.views.montserratFont

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
        name = recipe.name ?: "Unknown Meal" // Nazwa przepisu, np. "Pancakes"

        // Ilość porcji (zapisana w registered alimentation jako pieces)
        // Rzutujemy na Double, bo pieces w DTO jest Float?
        val servings = (alimentation.pieces ?: 1f).toDouble()
        amountText = if (servings == 1.0) "1 serving" else "$servings servings"

        // Iterujemy po składnikach przepisu, aby obliczyć makro
        recipe.ingredients.forEach { ing ->
            val ef = ing.essentialFood
            val api = ing.essentialApi

            // defaultWeight w EssentialFoodResponse jest Float?, rzutujemy na Double
            val baseWeight = (ef?.defaultWeight ?: api?.defaultWeight ?: 100f).toDouble()

            // Ile tego składnika jest w przepisie (Float -> Double)
            val iAmount = (ing.amount ?: 0f).toDouble()
            val iPieces = (ing.pieces ?: 0f).toDouble()

            val ratio = when {
                iPieces > 0.0 -> (iPieces * baseWeight) / 100.0
                iAmount > 0.0 -> iAmount / 100.0
                else -> 0.0
            }

            // Pobieramy wartości (z EF lub API) - TERAZ SĄ TO DOUBLE? (zgodnie z poprawką DTO)
            val itemCal = (ef?.calories ?: api?.calorie?.toDouble() ?: 0.0)
            val itemP = (ef?.protein ?: api?.protein?.toDouble() ?: 0.0)
            val itemF = (ef?.fat ?: api?.fat?.toDouble() ?: 0.0)
            val itemC = (ef?.carbohydrates ?: api?.carbohydrates?.toDouble() ?: 0.0)

            kcal += itemCal * ratio
            protein += itemP * ratio
            fat += itemF * ratio
            carbs += itemC * ratio
        }

        // Mnożymy sumę składników przez liczbę zjedzonych porcji
        kcal *= servings
        protein *= servings
        fat *= servings
        carbs *= servings

    }
    // 2. PRZYPADEK: TO JEST POJEDYNCZY PRODUKT
    else {
        val foodName = alimentation.getDisplayName()
        name = foodName

        val userPieces = (alimentation.pieces ?: 0f).toDouble()
        val userAmount = (alimentation.amount ?: 0f).toDouble()

        amountText = when {
            userPieces > 0.0 ->
                "${userPieces.toFloat()} piece${if (userPieces > 1.0) "s" else ""}" // Wyświetlamy jako Float, żeby nie było 1.0 piece
            userAmount > 0.0 ->
                "${String.format("%.1f", userAmount)} g"
            else -> "-"
        }

        // Helper do obliczania
        // defaultWeight w DTO jest Float?, konwertujemy na Double
        val baseWeight = (alimentation.essentialFood?.defaultWeight ?: alimentation.mealApi?.defaultWeight ?: 100f).toDouble()

        val ratio = when {
            userPieces > 0.0 -> (userPieces * baseWeight) / 100.0
            userAmount > 0.0 -> userAmount / 100.0
            else -> 0.0
        }

        // Pobieramy wartości (są Double? lub konwertujemy z API)
        val baseKcal = (alimentation.essentialFood?.calories ?: alimentation.mealApi?.calorie?.toDouble() ?: 0.0)
        val baseP = (alimentation.essentialFood?.protein ?: alimentation.mealApi?.protein?.toDouble() ?: 0.0)
        val baseF = (alimentation.essentialFood?.fat ?: alimentation.mealApi?.fat?.toDouble() ?: 0.0)
        val baseC = (alimentation.essentialFood?.carbohydrates ?: alimentation.mealApi?.carbohydrates?.toDouble() ?: 0.0)

        kcal = baseKcal * ratio
        protein = baseP * ratio
        fat = baseF * ratio
        carbs = baseC * ratio
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