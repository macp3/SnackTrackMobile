package study.snacktrackmobile.presentation.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.dto.BodyParametersRequest
import study.snacktrackmobile.data.model.enums.Sex
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.presentation.ui.components.*
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import study.snacktrackmobile.data.api.Request
import study.snacktrackmobile.data.model.LoginResponse
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.presentation.ui.components.montserratFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialSurveyView(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    // 🔹 Ustawienie jawnych kolorów dla pól tekstowych (TextInput i DropdownField)
    // To jest kluczowe dla zapewnienia białego tła inputów, niezależnie od motywu
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White,
        focusedIndicatorColor = colors.primary,
        unfocusedIndicatorColor = colors.onSurface.copy(alpha = 0.5f),
        errorContainerColor = Color.White,
        errorIndicatorColor = colors.error,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        errorTextColor = Color.Red,
        focusedLabelColor = Color.Black,
        unfocusedLabelColor = Color.Black,
    )


    // 🔹 POLA
    var sex by remember { mutableStateOf(Sex.male.name) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("Average") }
    var trainingIntensity by remember { mutableStateOf("Average") }
    var weeklyWeightChangeTempo by remember { mutableStateOf("") }
    var goalWeight by remember { mutableStateOf("") }

    // 🔹 ERROR FLAGS
    var sexError by remember { mutableStateOf(false) }
    var heightError by remember { mutableStateOf(false) }
    var weightError by remember { mutableStateOf(false) }
    var ageError by remember { mutableStateOf(false) }
    var activityError by remember { mutableStateOf(false) }
    var trainingError by remember { mutableStateOf(false) }
    var tempoError by remember { mutableStateOf(false) }
    var goalError by remember { mutableStateOf(false) }

    // 🔹 WIADOMOŚCI O BŁĘDACH
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var backendMessage by remember { mutableStateOf<String?>(null) }

    val sexOptions = listOf(Sex.male.name, Sex.female.name)
    val activityOptions = listOf("None", "Little", "Average", "Intense", "Professional")

    Scaffold(
        topBar = { SnackTrackTopBar() }
    ) { padding ->

        // KLUCZOWA POPRAWKA TŁA KOMPONENTU:
        // Wymuszenie jawnego białego koloru na Surface, jeśli motyw jest zbyt ciemny.
        Surface(
            color = Color.White, // <--- JAWNE WYMUSZENIE BIAŁEGO TŁA WIDOKU
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            // LazyColumn dla wydajnego przewijania
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // TYTUŁ
                item {
                    Text(
                        "Please fill initial survey for us to calculate your progress",
                        style = MaterialTheme.typography.headlineMedium,
                        fontFamily = montserratFont,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black // Upewnienie się, że tekst jest widoczny
                    )
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }

                // SEX
                item {
                    DropdownField(
                        label = "Sex",
                        selected = sex,
                        options = sexOptions,
                        isError = sexError,
                        onSelected = { sex = it; sexError = false },
                        modifier = Modifier.fillMaxWidth(0.85f),
                        // colors = textFieldColors // Dodaj ten parametr do DropdownField jeśli akceptuje
                    )
                }

                // HEIGHT
                item {
                    TextInput(
                        value = height,
                        label = "Height (cm)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = heightError,
                        onValueChange = { height = it; heightError = false },
                        modifier = Modifier.fillMaxWidth(0.85f),
                    )
                }

                // WEIGHT
                item {
                    TextInput(
                        value = weight,
                        label = "Weight (kg)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = weightError,
                        onValueChange = { weight = it; weightError = false },
                        modifier = Modifier.fillMaxWidth(0.85f),
                    )
                }

                // AGE
                item {
                    TextInput(
                        value = age,
                        label = "Age",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = ageError,
                        onValueChange = { age = it; ageError = false },
                        modifier = Modifier.fillMaxWidth(0.85f),
                    )
                }

                // ACTIVITY LEVEL
                item {
                    DropdownField(
                        label = "Daily activity level",
                        selected = activityLevel,
                        options = activityOptions,
                        isError = activityError,
                        onSelected = { activityLevel = it; activityError = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }

                // TRAINING INTENSITY
                item {
                    DropdownField(
                        label = "Training intensity",
                        selected = trainingIntensity,
                        options = activityOptions,
                        isError = trainingError,
                        onSelected = { trainingIntensity = it; trainingError = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }

                // WEEKLY WEIGHT CHANGE
                item {
                    TextInput(
                        value = weeklyWeightChangeTempo,
                        label = "Weekly weight change (0–1 kg/week)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = tempoError,
                        onValueChange = { weeklyWeightChangeTempo = it; tempoError = false },
                        modifier = Modifier.fillMaxWidth(0.85f),
                    )
                }

                // GOAL WEIGHT
                item {
                    TextInput(
                        value = goalWeight,
                        label = "Goal weight (kg)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = goalError,
                        onValueChange = { goalWeight = it; goalError = false },
                        modifier = Modifier.fillMaxWidth(0.85f),
                    )
                }

                // ODCZYT BŁĘDÓW I PRZYCISK
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    (backendMessage ?: errorMessage)?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontFamily = montserratFont,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                item {
                    DisplayButton(
                        text = "Next",
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .padding(bottom = 24.dp),
                        onClick = {
                            backendMessage = null
                            errorMessage = null

                            // reset error flags
                            sexError = false
                            heightError = false
                            weightError = false
                            ageError = false
                            activityError = false
                            trainingError = false
                            tempoError = false
                            goalError = false

                            fun fieldError(condition: Boolean, setError: () -> Unit) {
                                if (condition) setError()
                            }

                            fieldError(sex.isBlank()) { sexError = true }
                            fieldError(height.isBlank() || height.toFloatOrNull() == null || height.toFloat() <= 0f) { heightError = true }
                            fieldError(weight.isBlank() || weight.toFloatOrNull() == null || weight.toFloat() <= 0f) { weightError = true }
                            fieldError(age.isBlank() || age.toIntOrNull() == null || age.toInt() <= 0) { ageError = true }
                            fieldError(weeklyWeightChangeTempo.isBlank() || weeklyWeightChangeTempo.toFloatOrNull() == null ||
                                    weeklyWeightChangeTempo.toFloat() !in 0f..1f) { tempoError = true }
                            fieldError(goalWeight.isBlank() || goalWeight.toFloatOrNull() == null || goalWeight.toFloat() <= 0f) { goalError = true }
                            fieldError(activityLevel.isBlank()) { activityError = true }
                            fieldError(trainingIntensity.isBlank()) { trainingError = true }

                            if (sexError || heightError || weightError || ageError || tempoError || goalError || activityError || trainingError) {
                                errorMessage = "Please fill all fields correctly"
                                return@DisplayButton
                            }

                            val request = BodyParametersRequest(
                                sex = Sex.valueOf(sex),
                                height = height.toFloat(),
                                weight = weight.toFloat(),
                                age = age.toInt(),
                                dailyActivityFactor = mapLevelToFloatDaily(activityLevel),
                                dailyActivityTrainingFactor = mapLevelToFloatTraining(trainingIntensity),
                                weeklyWeightChangeTempo = weeklyWeightChangeTempo.toFloat(),
                                goalWeight = goalWeight.toFloat(),
                            )

                            scope.launch {
                                val token = TokenStorage.getToken(context)
                                if (token != null) {
                                    val bearer = "Bearer $token"
                                    val addRes = Request.userApi.addParameters(bearer, request)
                                    if (addRes.isSuccessful) {
                                        val refreshRes = Request.userApi.refreshSurvey(bearer)
                                        if (refreshRes.isSuccessful) {
                                            val loginResponse = refreshRes.body()
                                            if (loginResponse != null && !loginResponse.showSurvey) {
                                                navController.navigate("MainView") {
                                                    popUpTo("InitialSurveyView") { inclusive = true }
                                                }
                                            } else {
                                                backendMessage = "Survey still required"
                                            }
                                        } else {
                                            backendMessage = "RefreshSurvey failed: ${refreshRes.code()}"
                                        }
                                    } else {
                                        backendMessage = addRes.errorBody()?.string() ?: "AddParameters failed"
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}


// 🔹 Mapping functions
fun mapLevelToFloatDaily(level: String): Float = when (level) {
    "None" -> 0.7f
    "Little" -> 0.8f
    "Average" -> 0.9f
    "Intense" -> 1f
    "Professional" -> 1.15f
    else -> 0.9f
}

fun mapLevelToFloatTraining(level: String): Float = when (level) {
    "None" -> 0.5f
    "Little" -> 0.6f
    "Average" -> 0.7f
    "Intense" -> 0.8f
    "Professional" -> 0.95f
    else -> 0.7f
}