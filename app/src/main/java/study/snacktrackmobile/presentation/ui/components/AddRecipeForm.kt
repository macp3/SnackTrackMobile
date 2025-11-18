package study.snacktrackmobile.presentation.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.data.model.dto.ApiFoodResponseDetailed
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse

// Upewnij się, że masz dostęp do `TextInput` i `DisplayButton` w tym pliku
// (np. są w tym samym pakiecie lub odpowiednio zaimportowane).

@Composable
fun AddRecipeForm(
    name: String,
    desc: String,
    ingredients: SnapshotStateList<IngredientFormEntry>,
    // 🔹 Nowe parametry dla walidacji
    isNameError: Boolean,
    nameErrorMessage: String?,
    isDescError: Boolean,
    descErrorMessage: String?,
    serverErrorMessage: String?, // Nowy stan dla błędów z backendu

    onNameChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onStartAddIngredient: () -> Unit,
    onSelectIngredient: (Int) -> Unit,
    onSubmit: () -> Unit // Ta funkcja powinna uruchamiać walidację
) {
    val context = LocalContext.current
    val textInputModifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp)

    // Dodajemy walidację składników po stronie UI
    val areIngredientsValid = ingredients.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // ... (Tytuł sekcji bez zmian)

        // 1. Nazwa przepisu
        TextInput(
            value = name,
            onValueChange = onNameChange,
            label = "Recipe Name (max 50)",
            isError = isNameError,
            modifier = textInputModifier
        )
        // 🔹 Komunikat o błędzie pod polem Name
        if (isNameError && nameErrorMessage != null) {
            Text(
                text = nameErrorMessage,
                color = Color.Red,
                fontFamily = montserratFont,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Opis
        TextInput(
            value = desc,
            onValueChange = onDescChange,
            label = "Description (max 100)",
            isError = isDescError,
            modifier = textInputModifier
        )
        // 🔹 Komunikat o błędzie pod polem Description
        if (isDescError && descErrorMessage != null) {
            Text(
                text = descErrorMessage,
                color = Color.Red,
                fontFamily = montserratFont,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ingredients",
            fontFamily = montserratFont,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        // 🔹 Komunikat o minimalnej liczbie składników
        if (!areIngredientsValid) {
            Text(
                text = "The meal has to have at least one ingredient.",
                color = Color.Red,
                fontFamily = montserratFont,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f) // Używa dostępnej przestrzeni
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(ingredients) { index, entry ->
                // Upewniamy się, że mamy EssentialFood, bo składniki są dodawane tylko po pomyślnym wyborze.
                val essentialFood = entry.essentialFood
                if (essentialFood != null) {

                    // Tworzymy dummyAlimentation do użycia w ProductRow (zakładamy, że ProductRow
                    // jest używany do wyświetlania składników w innych częściach aplikacji)
                    val dummyAlimentation = RegisteredAlimentationResponse(
                        id = index, // Używamy indexu jako tymczasowego ID
                        userId = 0,
                        essentialFood = essentialFood,
                        mealApi = null,
                        meal = null,
                        timestamp = "",
                        // Wyświetlana ilość to ta, którą wybrał użytkownik
                        amount = entry.amount ?: 0f,
                        pieces = entry.pieces ?: 0f,
                        mealName = "Recipe"
                    )

                    ProductRow(
                        alimentation = dummyAlimentation,
                        // Przekazujemy indeks jako ID do usunięcia
                        onDelete = { idAsIndex ->
                            if (idAsIndex >= 0 && idAsIndex < ingredients.size) {
                                ingredients.removeAt(idAsIndex)
                                Toast.makeText(context, "Ingredient removed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        // Przekazujemy indeks do edycji
                        onEdit = {
                            onSelectIngredient(index)
                        }
                    )
                }
            }
        }

        // --- Komunikat z Backendu (nad przyciskami) ---
        if (serverErrorMessage != null) {
            Text(
                text = serverErrorMessage,
                color = Color.Red,
                fontFamily = montserratFont,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Przyciski na dole (bez zmian)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DisplayButton(
                text = "Add Ingredient",
                onClick = onStartAddIngredient,
                modifier = Modifier.weight(1f).height(55.dp),
                fontSize = 14
            )

            Spacer(modifier = Modifier.width(16.dp))

            DisplayButton(
                text = "Save Recipe",
                onClick = onSubmit,
                modifier = Modifier.weight(1f).height(55.dp),
                fontSize = 14
            )
        }
    }
}

// 🔹 Komponent dla pustego wiersza (gdy jeszcze nie wybrano produktu)
@Composable
fun EmptyIngredientRow(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray) // Obramowanie sugerujące "miejsce na coś"
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "+ Select ingredient",
                fontFamily = montserratFont,
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

data class IngredientFormEntry(
    val essentialFood: EssentialFoodResponse? = null,
    val essentialApi: ApiFoodResponseDetailed? = null,
    val amount: Float? = null,
    val pieces: Float? = null
)