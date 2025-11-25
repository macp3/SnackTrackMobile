package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.api.ApiService
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.api.Request.api
import study.snacktrackmobile.data.model.dto.EssentialFoodRequest
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.viewmodel.FoodViewModel

@Composable
fun AddProductToDatabaseScreen(
    navController: NavController,
    foodViewModel: FoodViewModel,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var carbohydrates by remember { mutableStateOf("") }
    var defaultWeight by remember { mutableStateOf("") }
    var brandName by remember { mutableStateOf("") }
    var servingSizeUnit by remember { mutableStateOf("") }

    var wasSubmitted by remember { mutableStateOf(false) }

    val errorMessage by foodViewModel.errorMessage
    val success by foodViewModel.success

    // ✅ KLUCZOWA ZMIANA: Stan przewijania
    val scrollState = rememberScrollState()

    // Funkcja pomocnicza do określania błędu wyświetlania
    val shouldShowError: (String) -> Boolean = { value ->
        wasSubmitted && value.isBlank()
    }

    // GŁÓWNA KOLUMNA: Zawiera przewijany formularz i stałe przyciski
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // SEKCJA PRZEWIJANA (FORMULARZ)
        Column(
            modifier = Modifier
                .verticalScroll(scrollState) // ✅ Umożliwia przewijanie
                .padding(horizontal = 10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier.height(16.dp))

            Text("Add new product", fontSize = 24.sp, fontFamily = montserratFont,)
            // Pola formularza
            TextInput(
                value = name,
                label = "Name",
                isError = shouldShowError(name),
                onValueChange = { name = it })
            Spacer(modifier = Modifier.height(8.dp))
            TextInput(
                value = description,
                label = "Description",
                isError = shouldShowError(description),
                onValueChange = { description = it })
            Spacer(modifier = Modifier.height(8.dp))
            TextInput(
                value = calories,
                label = "Calories",
                isError = shouldShowError(calories),
                onValueChange = { calories = it })
            Spacer(modifier = Modifier.height(8.dp))
            TextInput(
                value = protein,
                label = "Protein",
                isError = shouldShowError(protein),
                onValueChange = { protein = it })
            Spacer(modifier = Modifier.height(8.dp))
            TextInput(
                value = fat,
                label = "Fat",
                isError = shouldShowError(fat),
                onValueChange = { fat = it })
            Spacer(modifier = Modifier.height(8.dp))
            TextInput(
                value = carbohydrates,
                label = "Carbohydrates",
                isError = shouldShowError(carbohydrates),
                onValueChange = { carbohydrates = it })
            Spacer(modifier = Modifier.height(8.dp))
            TextInput(
                value = defaultWeight,
                label = "Default weight",
                isError = shouldShowError(defaultWeight),
                onValueChange = { defaultWeight = it })
            Spacer(modifier = Modifier.height(8.dp))
            TextInput(
                value = servingSizeUnit,
                label = "Serving size unit",
                isError = shouldShowError(servingSizeUnit),
                onValueChange = { servingSizeUnit = it })
            Spacer(modifier = Modifier.height(8.dp))
            TextInput(
                value = brandName,
                label = "Brand name (optional)",
                isError = false,
                onValueChange = { brandName = it })
            Spacer(modifier = Modifier.height(16.dp))

            // Obsługa błędów
            errorMessage?.let {
                Text(text = it, color = Color.Red, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // SEKCJA STAŁA (PRZYCISKI) - zostaje na dole
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp), // Dodatkowy padding na górze przycisków
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DisplayButton(
                    "Cancel",
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(width = 120.dp, height = 50.dp),
                    fontSize = 14
                )
                DisplayButton(
                    "Save",
                    onClick = {
                        wasSubmitted = true
                        val validationError = validateForm(
                            name,
                            description,
                            calories,
                            protein,
                            fat,
                            carbohydrates,
                            defaultWeight,
                            servingSizeUnit,
                            brandName
                        )
                        if (validationError != null) {
                            foodViewModel.setError(validationError)
                        } else {
                            foodViewModel.addFood(
                                EssentialFoodRequest(
                                    name = name,
                                    description = description,
                                    calories = calories.toFloat(),
                                    protein = protein.toFloat(),
                                    fat = fat.toFloat(),
                                    carbohydrates = carbohydrates.toFloat(),
                                    defaultWeight = defaultWeight.toFloat(),
                                    servingSizeUnit = servingSizeUnit,
                                    brandName = brandName
                                )
                            )
                        }
                    },
                    modifier = Modifier.size(width = 120.dp, height = 50.dp),
                    fontSize = 14
                )
            } // Koniec przewijanej kolumny
            Spacer(modifier.height(16.dp))
        }
    }

    if (success) {
        wasSubmitted = false
        foodViewModel.resetSuccess()
        navController.popBackStack()
    }
}


// Funkcja walidacji zgodna z backendem + limity długości pól
fun validateForm(
    name: String,
    description: String,
    calories: String,
    protein: String,
    fat: String,
    carbohydrates: String,
    defaultWeight: String,
    servingSizeUnit: String,
    brandName: String
): String? {
    if (name.isBlank()) return "Field 'name' is required"
    if (name.length > 50) return "Field 'name' must be at most 50 characters"

    if (description.isBlank()) return "Field 'description' is required"
    if (description.length > 100) return "Field 'description' must be at most 100 characters"

    val cal = calories.toFloatOrNull() ?: return "Calories must be a number"
    if (cal <= 0) return "Field 'calories' must be greater than 0"

    val prot = protein.toFloatOrNull() ?: return "Protein must be a number"
    if (prot < 0) return "Field 'protein' must be greater or equal 0"

    val f = fat.toFloatOrNull() ?: return "Fat must be a number"
    if (f < 0) return "Field 'fat' must be greater or equal 0"

    val carb = carbohydrates.toFloatOrNull() ?: return "Carbohydrates must be a number"
    if (carb < 0) return "Field 'carbohydrates' must be greater or equal 0"

    val dw = defaultWeight.toFloatOrNull() ?: return "Default weight must be a number"
    if (dw < 0) return "Field 'defaultWeight' must be greater or equal 0"

    if (servingSizeUnit.isBlank()) return "Field 'servingSizeUnit' is required"
    if (servingSizeUnit.length > 50) return "Field 'servingSizeUnit' must be at most 50 characters"

    if (brandName.isNotBlank() && brandName.length > 50) {
        return "Field 'brandName' must be at most 50 characters"
    }

    return null
}

