package study.snacktrackmobile.presentation.ui.components

import DropdownField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    isEditMode: Boolean = false, // 🔽 nowy parametr – tryb edycji
    productId: Int? = null       // 🔽 id istniejącego wpisu do edycji
) {
    val context = LocalContext.current

    // Opcje jednostek
    val options = listOf(product.servingSizeUnit ?: "unit", "piece")
    var selectedOption by remember { mutableStateOf(options.first()) }

    // Stan dla inputa
    var inputValue by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Name: ${product.name}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Description: ${product.description}")
        Text("Calories: ${product.calories}")
        Text("Protein: ${product.protein}")
        Text("Fat: ${product.fat}")
        Text("Carbohydrates: ${product.carbohydrates}")
        Text("Brand: ${product.brandName ?: "-"}")
        Text("Default weight: ${product.defaultWeight}")
        Text("Serving size unit: ${product.servingSizeUnit}")

        Spacer(modifier = Modifier.height(16.dp))

        // 🔽 DropdownField
        DropdownField(
            label = "Jednostka",
            selected = selectedOption,
            options = options,
            onSelected = { selectedOption = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✏️ TextField do wpisania ilości
        TextInput(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = "Quantity",
            isError = isError
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ przycisk Dodaj / Update
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
                    // 🔽 aktualizacja istniejącego wpisu
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
                    // 🔽 dodawanie nowego wpisu
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


