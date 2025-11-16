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
    isEditMode: Boolean = false
) {
    val food = alimentation.essentialFood ?: return
    val context = LocalContext.current

    // 1) Jednostka z produktu (znormalizowana)
    val unitRaw = food.servingSizeUnit?.lowercase()?.trim()
    val normalizedUnit = when (unitRaw) {
        "gram" -> "g"
        "milliliter" -> "ml"
        null, "" -> "g"
        else -> unitRaw!!
    }

    // 2) Dropdown: zawsze "piece" + servingSizeUnit z produktu
    val options: List<String> = listOf("piece", normalizedUnit).distinct()
    var selectedOption by remember { mutableStateOf(options.first()) }

    // 3) Prefill quantity zgodnie z wyborem
    var inputValue by remember {
        mutableStateOf(if (selectedOption == "piece") "1" else (food.defaultWeight?.toInt()?.toString() ?: ""))
    }
    var isError by remember { mutableStateOf(false) }

    // 4) UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Name: ${food.name}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text("Serving size unit: ${normalizedUnit}", color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        DropdownField(
            label = "Serving unit",
            selected = selectedOption,
            options = options,
            onSelected = { new ->
                selectedOption = new
                // aktualizuj domyślną wartość po zmianie jednostki
                inputValue = if (selectedOption == "piece") "1" else (food.defaultWeight?.toInt()?.toString() ?: "")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextInput(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = "Quantity (${selectedOption})",
            isError = isError
        )

        Spacer(modifier = Modifier.height(16.dp))

        DisplayButton(
            text = if (isEditMode) "Save" else "Add",
            onClick = {
                if (inputValue.isBlank()) {
                    isError = true
                    return@DisplayButton
                }

                // 5) Wylicz amount/pieces wyłącznie wg wybranej jednostki
                val amount: Float? = if (selectedOption == "piece") null else inputValue.toFloatOrNull()
                val pieces: Float? = if (selectedOption == "piece") inputValue.toFloatOrNull() else null

                // walidacja
                if (selectedOption == "piece" && pieces == null) {
                    isError = true; return@DisplayButton
                }
                if (selectedOption != "piece" && amount == null) {
                    isError = true; return@DisplayButton
                }

                if (isEditMode) {
                    val dto = RegisteredAlimentationRequest(
                        essentialId = food.id,
                        mealApiId = alimentation.mealApi?.id,
                        mealId = alimentation.meal?.id,
                        timestamp = selectedDate,
                        mealName = selectedMeal.lowercase(),
                        amount = amount,
                        pieces = pieces
                    )
                    registeredAlimentationViewModel.updateMealProduct(
                        context = context,
                        productId = alimentation.id,
                        dto = dto,
                        date = selectedDate
                    )
                } else {
                    registeredAlimentationViewModel.addMealProduct(
                        context = context,
                        essentialId = food.id,
                        mealName = selectedMeal,
                        date = selectedDate,
                        amount = amount,   // Float? (null gdy piece)
                        pieces = pieces    // Int? (null gdy g/ml)
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


