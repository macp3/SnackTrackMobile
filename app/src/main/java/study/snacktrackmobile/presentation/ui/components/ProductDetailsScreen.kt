package study.snacktrackmobile.presentation.ui.components

import DropdownField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationRequest
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse
import study.snacktrackmobile.presentation.ui.views.montserratFont
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

    // 🔹 1. DANE BAZOWE (Na 100g)
    val name = alimentation.essentialFood?.name
        ?: alimentation.mealApi?.name
        ?: "Unknown Product"

    val rawUnit = alimentation.essentialFood?.servingSizeUnit
        ?: alimentation.mealApi?.servingSizeUnit

    fun extractWeight(quantityStr: String?): Float? {
        if (quantityStr == null) return null
        val regex = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:g|ml)", RegexOption.IGNORE_CASE)
        val match = regex.find(quantityStr)
        return match?.groupValues?.get(1)?.toFloatOrNull()
    }

    val determinedWeight = alimentation.essentialFood?.defaultWeight
        ?: alimentation.mealApi?.defaultWeight
        ?: extractWeight(alimentation.mealApi?.quantity)
        ?: 100f

    val defaultWeight = if (determinedWeight > 0f) determinedWeight else 100f

    val rawKcal = alimentation.essentialFood?.calories
        ?: alimentation.mealApi?.calorie?.toFloat()
        ?: 0f

    val rawP = alimentation.essentialFood?.protein ?: alimentation.mealApi?.protein ?: 0f
    val rawF = alimentation.essentialFood?.fat ?: alimentation.mealApi?.fat ?: 0f
    val rawC = alimentation.essentialFood?.carbohydrates ?: alimentation.mealApi?.carbohydrates ?: 0f

    // Normalizacja do 100g
    val isApi = alimentation.mealApi != null
    val needsNormalization = isApi && defaultWeight != 100f

    val baseKcal = if (needsNormalization) (rawKcal * 100f) / defaultWeight else rawKcal
    val baseP = if (needsNormalization) (rawP * 100f) / defaultWeight else rawP
    val baseF = if (needsNormalization) (rawF * 100f) / defaultWeight else rawF
    val baseC = if (needsNormalization) (rawC * 100f) / defaultWeight else rawC

    // 🔹 2. UI SETUP
    val unitRawString = rawUnit?.lowercase()?.trim()
    val normalizedUnit = when {
        unitRawString == "gram" -> "g"
        unitRawString == "milliliter" -> "ml"
        (unitRawString.isNullOrEmpty()) -> "g"
        else -> unitRawString!!
    }

    val options: List<String> = listOf("piece", normalizedUnit).distinct()
    var selectedOption by remember { mutableStateOf(options.first()) }

    var inputValue by remember {
        mutableStateOf(
            if (alimentation.pieces != null && alimentation.pieces > 0) alimentation.pieces.toString()
            else if (selectedOption == "piece") "1"
            else defaultWeight.toInt().toString()
        )
    }
    var isError by remember { mutableStateOf(false) }

    // 🔹 3. KALKULATOR NA ŻYWO
    val liveSummary = remember(inputValue, selectedOption) {
        val qty = inputValue.toFloatOrNull() ?: 0f
        val totalGrams = if (selectedOption == "piece") {
            qty * defaultWeight
        } else {
            qty
        }
        val ratio = totalGrams / 100f

        MacroSummaryUi(
            kcal = baseKcal * ratio,
            p = baseP * ratio,
            f = baseF * ratio,
            c = baseC * ratio,
            weight = totalGrams
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black, fontFamily = montserratFont)
        Text(
            "One piece weights: ${defaultWeight.toInt()} g",
            color = Color.Gray,
            fontFamily = montserratFont,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        DropdownField(
            label = "Serving unit",
            selected = selectedOption,
            options = options,
            onSelected = { new ->
                selectedOption = new
                inputValue = if (selectedOption == "piece") "1" else defaultWeight.toInt().toString()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextInput(
            value = inputValue,
            onValueChange = { newValue ->
                if (newValue.length <= 8) {
                    inputValue = newValue
                }
            },
            label = "Quantity ($selectedOption)",
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 4. KARTA PODSUMOWANIA (Zabezpieczona przed rozjeżdżaniem)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Summary (${String.format("%.0f", liveSummary.weight)} g)",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = montserratFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1, // Zabezpieczenie tytułu
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = Color.LightGray)
                Spacer(modifier = Modifier.height(8.dp))

                // Używamy wag (weight) aby każda kolumna miała tyle samo miejsca
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween // To teraz działa jako fallback, bo używamy weight
                ) {
                    MacroItem(
                        value = liveSummary.kcal,
                        label = "Kcal",
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f) // Równy podział (25%)
                    )
                    MacroItem(
                        value = liveSummary.p,
                        label = "Protein",
                        modifier = Modifier.weight(1f)
                    )
                    MacroItem(
                        value = liveSummary.f,
                        label = "Fat",
                        modifier = Modifier.weight(1f)
                    )
                    MacroItem(
                        value = liveSummary.c,
                        label = "Carbs",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        DisplayButton(
            text = if (onYieldResult != null) "Add to Recipe" else (if (isEditMode) "Save" else "Add"),
            onClick = {
                if (inputValue.isBlank()) {
                    isError = true
                    return@DisplayButton
                }

                val inputFloat = inputValue.toFloatOrNull()
                if (inputFloat == null || inputFloat <= 0) {
                    isError = true
                    return@DisplayButton
                }

                val amount: Float? = if (selectedOption == "piece") null else inputFloat
                val pieces: Float? = if (selectedOption == "piece") inputFloat else null

                if (onYieldResult != null) {
                    onYieldResult(amount, pieces)
                } else {
                    val essentialId = alimentation.essentialFood?.id
                    val mealApiId = alimentation.mealApi?.id

                    val dto = RegisteredAlimentationRequest(
                        essentialId = essentialId,
                        mealApiId = mealApiId,
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
                        registeredAlimentationViewModel.addMealProduct(
                            context = context,
                            essentialId = essentialId,
                            mealApiId = mealApiId,
                            mealName = selectedMeal,
                            date = selectedDate,
                            amount = amount,
                            pieces = pieces
                        )
                    }
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            fontSize = 16
        )

        Spacer(modifier = Modifier.height(8.dp))

        DisplayButton(
            text = "Back",
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            containerColor = Color.LightGray,
            fontSize = 16
        )
    }
}

// Pomocnicza klasa do UI
data class MacroSummaryUi(
    val kcal: Float,
    val p: Float,
    val f: Float,
    val c: Float,
    val weight: Float
)

@Composable
fun MacroItem(
    value: Float,
    label: String,
    color: Color = Color.Black,
    modifier: Modifier = Modifier // Dodano modifier
) {
    Column(
        modifier = modifier, // Użycie modifiera (wagi)
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (value >= 10000) "9999+" else String.format("%.0f", value), // Opcjonalne zabezpieczenie przed milionami
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = montserratFont,
            color = color,
            maxLines = 1, // Zabezpieczenie przed łamaniem linii
            overflow = TextOverflow.Ellipsis // Kropki jeśli za długie
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = montserratFont,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}