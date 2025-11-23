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
    isEditMode: Boolean = false,
    onYieldResult: ((Float?, Float?) -> Unit)? = null
) {
    val context = LocalContext.current

    // 🔹 1. BEZPIECZNE POBIERANIE DANYCH (Lokalne vs API)
    // Jeśli essentialFood jest null (produkt z API), bierzemy dane z mealApi
    val name = alimentation.essentialFood?.name
        ?: alimentation.mealApi?.name
        ?: "Unknown Product"

    val rawUnit = alimentation.essentialFood?.servingSizeUnit
        ?: alimentation.mealApi?.servingSizeUnit

    val defaultWeight = alimentation.essentialFood?.defaultWeight
        ?: alimentation.mealApi?.defaultWeight
        ?: 100f // Domyślna waga dla API jeśli brak danych

    // 🔹 2. Normalizacja jednostki
    val unitRawString = rawUnit?.lowercase()?.trim()
    val normalizedUnit = when (unitRawString) {
        "gram" -> "g"
        "milliliter" -> "ml"
        null, "" -> "g" // Domyślnie gramy, jeśli API nie podało jednostki
        else -> unitRawString
    }

    // 🔹 3. Dropdown: zawsze "piece" + jednostka produktu
    val options: List<String> = listOf("piece", normalizedUnit).distinct()
    var selectedOption by remember { mutableStateOf(options.first()) }

    // 🔹 4. Prefill quantity
    var inputValue by remember {
        mutableStateOf(
            if (selectedOption == "piece") "1"
            else (defaultWeight.toInt().toString())
        )
    }
    var isError by remember { mutableStateOf(false) }

    // 🔹 5. UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Name: $name", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text("Serving size unit: $normalizedUnit", color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        DropdownField(
            label = "Serving unit",
            selected = selectedOption,
            options = options,
            onSelected = { new ->
                selectedOption = new
                // Reset wartości przy zmianie typu
                inputValue = if (selectedOption == "piece") "1" else defaultWeight.toInt().toString()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextInput(
            value = inputValue,
            onValueChange = { inputValue = it },
            label = "Quantity ($selectedOption)",
            isError = isError
        )

        Spacer(modifier = Modifier.height(16.dp))

        DisplayButton(
            text = if (onYieldResult != null) "Add to Recipe" else (if (isEditMode) "Save" else "Add"),
            onClick = {
                if (inputValue.isBlank()) {
                    isError = true
                    return@DisplayButton
                }

                // 6) Wylicz amount/pieces
                val amount: Float? = if (selectedOption == "piece") null else inputValue.toFloatOrNull()
                val pieces: Float? = if (selectedOption == "piece") inputValue.toFloatOrNull() else null

                // Walidacja
                if (selectedOption == "piece" && pieces == null) {
                    isError = true; return@DisplayButton
                }
                if (selectedOption != "piece" && amount == null) {
                    isError = true; return@DisplayButton
                }

                // 🔹 LOGIKA ZAPISU
                if (onYieldResult != null) {
                    // Tryb przepisu (tylko zwracamy dane)
                    onYieldResult(amount, pieces)
                } else {
                    // Przygotowanie requestu (DTO)
                    // Musimy sprawdzić, które ID wysłać (lokalne czy API)
                    val essentialId = alimentation.essentialFood?.id
                    val mealApiId = alimentation.mealApi?.id

                    // Tworzymy obiekt requestu
                    val dto = RegisteredAlimentationRequest(
                        essentialId = essentialId, // Może być null dla produktu z API
                        mealApiId = mealApiId,     // Może być null dla produktu lokalnego
                        mealId = alimentation.meal?.id,
                        timestamp = selectedDate,
                        mealName = selectedMeal.lowercase(),
                        amount = amount,
                        pieces = pieces
                    )

                    if (isEditMode) {
                        registeredAlimentationViewModel.updateMealProduct(
                            context = context,
                            productId = alimentation.id,
                            dto = dto,
                            date = selectedDate
                        )
                    } else {
                        // UWAGA: Tutaj używamy generycznej metody addMealProduct
                        // Jeśli Twoja metoda w ViewModelu przyjmuje 'essentialId' jako nie-nullowy Int,
                        // musisz ją zaktualizować w ViewModelu, aby przyjmowała DTO lub nullable ID.
                        // Zakładam, że ViewModel ma metodę obsługującą oba przypadki,
                        // lub użyjemy tutaj DTO jeśli ViewModel na to pozwala.

                        // Jeśli ViewModel wymaga osobnych parametrów, a essentialId jest null,
                        // to prawdopodobnie masz tam metodę obsługującą mealApiId lub musisz ją dodać.

                        // Bezpieczniejsza wersja (przekazanie DTO do ViewModelu, jeśli obsługuje):
                        // registeredAlimentationViewModel.addMealProductFromDto(context, dto)

                        // Wersja dopasowana do Twojego starego kodu (z poprawką na nulle):
                        registeredAlimentationViewModel.addMealProduct(
                            context = context,
                            essentialId = essentialId,
                            mealApiId = mealApiId, // <--- UPEWNIJ SIĘ, ŻE VIEWMODEL TO PRZYJMUJE
                            mealName = selectedMeal,
                            date = selectedDate,
                            amount = amount,
                            pieces = pieces
                        )
                    }
                    onBack()
                }
            },
            modifier = Modifier.size(width = 160.dp, height = 50.dp),
            fontSize = 14
        )

        Spacer(modifier = Modifier.height(8.dp))

        DisplayButton(
            text = "Back",
            onClick = onBack,
            modifier = Modifier.size(width = 160.dp, height = 50.dp),
            fontSize = 14
        )
    }
}