package study.snacktrackmobile.presentation.ui.components

import DropdownField
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationRequest
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun ProductDetailsScreen(
    alimentation: RegisteredAlimentationResponse,
    selectedDate: String,
    selectedMeal: String,
    onBack: () -> Unit,
    registeredAlimentationViewModel: RegisteredAlimentationViewModel,
    productId: Int? = null,
    isEditMode : Boolean = false
) {
    val food = alimentation.essentialFood ?: return
    val context = LocalContext.current

    // Opcje jednostek
    val options = listOf(food.servingSizeUnit ?: "unit", "piece")
    var selectedOption by remember { mutableStateOf(options.first()) }

    var inputValue by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Name: ${food.name}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Description: ${food.description}")
        Text("Calories: ${food.calories}")
        Text("Protein: ${food.protein}")
        Text("Fat: ${food.fat}")
        Text("Carbohydrates: ${food.carbohydrates}")
        Text("Brand: ${food.brandName ?: "-"}")
        Text("Default weight: ${food.defaultWeight}")
        Text("Serving size unit: ${food.servingSizeUnit}")

        Spacer(modifier = Modifier.height(16.dp))

        DropdownField(
            label = "Serving unit",
            selected = selectedOption,
            options = options,
            onSelected = { selectedOption = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextInput(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = "Quantity",
            isError = isError
        )

        Spacer(modifier = Modifier.height(16.dp))

        DisplayButton(
            text = if (isEditMode) "Save" else "Add",   // 🔽 zmiana
            onClick = {
                if (inputValue.isBlank()) {
                    isError = true
                    return@DisplayButton
                }

                val amount: Float
                val pieces: Int

                if (selectedOption == "piece") {
                    pieces = inputValue.toIntOrNull() ?: 1
                    amount = (food.defaultWeight ?: 1f) * pieces
                } else {
                    amount = inputValue.toFloatOrNull() ?: (food.defaultWeight ?: 100f)
                    pieces = 0
                }

                if (isEditMode) {
                    val dto = RegisteredAlimentationRequest(
                        essentialId = food.id,
                        mealApiId = alimentation.mealApi?.id,   // jeśli wpis pochodzi z API
                        mealId = alimentation.meal?.id,         // jeśli wpis pochodzi z lokalnego Meal
                        timestamp = selectedDate,               // np. "2025-11-13"
                        mealName = selectedMeal.lowercase(),    // np. "BREAKFAST"
                        amount = if (pieces == 0) amount else null,
                        pieces = if (pieces > 0) pieces else null
                    )

                    registeredAlimentationViewModel.updateMealProduct(
                        context = context,
                        productId = alimentation.id, // ID wpisu w bazie
                        dto = dto,
                        date = selectedDate
                    )
                } else {
                    registeredAlimentationViewModel.addMealProduct(
                        context = context,
                        essentialId = food.id,
                        mealName = selectedMeal,
                        date = selectedDate,
                        amount = amount,
                        pieces = pieces
                    )
                }

                onBack()
            },
            modifier = Modifier.size(width = 120.dp, height = 50.dp),
            fontSize = 14
        )


        Spacer(modifier = Modifier.height(8.dp))

        DisplayButton(
            text = "Back",
            onClick = onBack,
            modifier = Modifier.size(width = 120.dp, height = 50.dp),
            fontSize = 14
        )
    }
}
