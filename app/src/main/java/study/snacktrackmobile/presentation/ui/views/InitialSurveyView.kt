package study.snacktrackmobile.presentation.ui.views

import DropdownField
import androidx.compose.foundation.layout.*
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
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import study.snacktrackmobile.data.network.ApiConfig
import study.snacktrackmobile.presentation.ui.components.montserratFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialSurveyView(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Please fill initial survey for us to calculate your progress",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = montserratFont,
                fontSize = 25.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // SEX
            DropdownField(
                label = "Sex",
                selected = sex,
                options = sexOptions,
                isError = sexError,
                onSelected = { sex = it; sexError = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // HEIGHT
            TextInput(
                value = height,
                label = "Height (cm)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = heightError,
                onValueChange = { height = it; heightError = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // WEIGHT
            TextInput(
                value = weight,
                label = "Weight (kg)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = weightError,
                onValueChange = { weight = it; weightError = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // AGE
            TextInput(
                value = age,
                label = "Age",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = ageError,
                onValueChange = { age = it; ageError = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ACTIVITY LEVEL
            DropdownField(
                label = "Daily activity level",
                selected = activityLevel,
                options = activityOptions,
                isError = activityError,
                onSelected = { activityLevel = it; activityError = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TRAINING INTENSITY
            DropdownField(
                label = "Training intensity",
                selected = trainingIntensity,
                options = activityOptions,
                isError = trainingError,
                onSelected = { trainingIntensity = it; trainingError = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // WEEKLY WEIGHT CHANGE
            TextInput(
                value = weeklyWeightChangeTempo,
                label = "Weekly weight change (0–1 kg/week)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = tempoError,
                onValueChange = { weeklyWeightChangeTempo = it; tempoError = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // GOAL WEIGHT
            TextInput(
                value = goalWeight,
                label = "Goal weight (kg)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = goalError,
                onValueChange = { goalWeight = it; goalError = false },
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            (backendMessage ?: errorMessage)?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontFamily = montserratFont,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            DisplayButton(
                text = "Next",
                modifier = Modifier.fillMaxWidth(0.6f),
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
                            val result = sendBodyParameters(token, request)
                            if (result.isSuccess) {
                                navController.navigate("MainView") {
                                    popUpTo("InitialSurveyView") { inclusive = true }
                                }
                            } else {
                                backendMessage = result.exceptionOrNull()?.message
                            }
                        } else {
                            backendMessage = "No authorization token"
                        }
                    }
                }
            )
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

// 🔹 Send request
suspend fun sendBodyParameters(
    token: String,
    request: BodyParametersRequest
): Result<Unit> {
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    return try {
        client.post("${ApiConfig.BASE_URL}/users/addParameters") {
            headers { append("Authorization", "Bearer $token") }
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    } finally {
        client.close()
    }
}
