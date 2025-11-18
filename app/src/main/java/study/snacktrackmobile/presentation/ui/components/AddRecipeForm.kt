package study.snacktrackmobile.presentation.ui.components

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.data.model.dto.ApiFoodResponseDetailed
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.presentation.ui.views.montserratFont

// DTO dla formularza
data class IngredientFormEntry(
    val essentialFood: EssentialFoodResponse? = null,
    val essentialApi: ApiFoodResponseDetailed? = null,
    val amount: Float? = null,
    val pieces: Float? = null
)

@Composable
fun AddRecipeForm(
    name: String,
    desc: String,
    ingredients: SnapshotStateList<IngredientFormEntry>,
    // Walidacja
    isNameError: Boolean,
    nameErrorMessage: String?,
    isDescError: Boolean,
    descErrorMessage: String?,
    serverErrorMessage: String?,
    // Callbacks
    onNameChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onStartAddIngredient: () -> Unit,
    onSelectIngredient: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    val textInputModifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp)

    val areIngredientsValid = ingredients.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        // --- TYTUŁ ---
        Text(
            text = "Create New Recipe",
            fontFamily = montserratFont,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- NAZWA ---
        TextInput(
            value = name,
            onValueChange = onNameChange,
            label = "Recipe Name (max 50)",
            isError = isNameError,
            modifier = textInputModifier
        )
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

        // --- OPIS ---
        TextInput(
            value = desc,
            onValueChange = onDescChange,
            label = "Description (max 100)",
            isError = isDescError,
            modifier = textInputModifier
        )
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

        // --- NAGŁÓWEK SKŁADNIKÓW ---
        Text(
            text = "Ingredients",
            fontFamily = montserratFont,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        if (!areIngredientsValid) {
            Text(
                text = "The meal has to have at least one ingredient.",
                color = Color.Red,
                fontFamily = montserratFont,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // --- LISTA SKŁADNIKÓW ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Usunięto logikę EmptyIngredientRow - teraz po prostu renderuje listę (lub nic, jeśli pusta)
            itemsIndexed(ingredients) { index, entry ->
                val essentialFood = entry.essentialFood
                if (essentialFood != null) {
                    val dummyAlimentation = RegisteredAlimentationResponse(
                        id = index,
                        userId = 0,
                        essentialFood = essentialFood,
                        mealApi = null,
                        meal = null,
                        timestamp = "",
                        amount = entry.amount ?: 0f,
                        pieces = entry.pieces ?: 0f,
                        mealName = "Recipe"
                    )

                    ProductRow(
                        alimentation = dummyAlimentation,
                        onDelete = { _ ->
                            if (index >= 0 && index < ingredients.size) {
                                ingredients.removeAt(index)
                                Toast.makeText(context, "Ingredient removed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onEdit = {
                            onSelectIngredient(index)
                        }
                    )
                }
            }
        }

        // --- ERROR SERWERA ---
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

        // --- PRZYCISKI ---
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