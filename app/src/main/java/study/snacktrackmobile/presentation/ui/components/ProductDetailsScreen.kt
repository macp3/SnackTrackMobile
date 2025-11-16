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
import study.snacktrackmobile.viewmodel.RegisteredAlimentationViewModel

@Composable
fun ProductDetailsScreen(
    product: EssentialFoodResponse,
    selectedDate: String,
    selectedMeal: String,
    onBack: () -> Unit,
    registeredAlimentationViewModel: RegisteredAlimentationViewModel,
    isEditMode: Boolean = false,
    productId: Int? = null
) {
    val context = LocalContext.current

    val options = listOf(product.servingSizeUnit ?: "unit", "piece")
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
        Text("Name: ${product.name}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text("Description: ${product.description}", color = Color.Black)
        Text("Calories: ${product.calories}", color = Color.Black)
        Text("Protein: ${product.protein}", color = Color.Black)
        Text("Fat: ${product.fat}", color = Color.Black)
        Text("Carbohydrates: ${product.carbohydrates}", color = Color.Black)
        Text("Brand: ${product.brandName ?: "-"}", color = Color.Black)
        Text("Default weight: ${product.defaultWeight}", color = Color.Black)
        Text("Serving size unit: ${product.servingSizeUnit}", color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        DropdownField(
            label = "Jednostka",
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
            text = if (isEditMode) "Update" else "Add",
            onClick = {
                if (inputValue.isBlank()) {
                    isError = true
                    return@DisplayButton
                }

                val amount: Float
                val pieces: Int

                if (selectedOption == "piece") {
                    pieces = inputValue.toIntOrNull() ?: 1
                    amount = (product.defaultWeight ?: 1f) * pieces
                } else {
                    amount = inputValue.toFloatOrNull() ?: (product.defaultWeight ?: 100f)
                    pieces = 0
                }

                if (isEditMode && productId != null) {
                    val dto = RegisteredAlimentationRequest(
                        essentialId = product.id,
                        mealName = selectedMeal,
                        amount = amount,
                        pieces = pieces
                    )
                    registeredAlimentationViewModel.updateMealProduct(
                        context = context,
                        productId = productId,
                        dto = dto,
                        date = selectedDate
                    )
                } else {
                    registeredAlimentationViewModel.addMealProduct(
                        context = context,
                        essentialId = product.id,
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
