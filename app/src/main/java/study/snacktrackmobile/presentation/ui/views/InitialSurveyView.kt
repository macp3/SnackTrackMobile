package study.snacktrackmobile.presentation.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
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

@Composable
fun InitialSurveyView(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sex by remember { mutableStateOf(Sex.male.name) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("Average") }
    var trainingIntensity by remember { mutableStateOf("Average") }
    var weeklyWeightChangeTempo by remember { mutableStateOf("") }
    var goalWeight by remember { mutableStateOf("") }

    var validationMessage by remember { mutableStateOf<String?>(null) }
    var backendMessage by remember { mutableStateOf<String?>(null) }

    val sexOptions = listOf(Sex.male.name, Sex.female.name)
    val activityOptions = listOf("None", "Little", "Average", "Intense", "Professional")

    Scaffold(
        topBar = { SnackTrackTopBar() }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Please fill initial survey for us to calculate your progress",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            DropdownField("Sex", sex, sexOptions) { sex = it }
            TextInput(height, "Height (cm)", KeyboardOptions(keyboardType = KeyboardType.Number), isError = false) { height = it }
            TextInput(weight, "Weight (kg)", KeyboardOptions(keyboardType = KeyboardType.Number), isError = false) { weight = it }
            TextInput(age, "Age", KeyboardOptions(keyboardType = KeyboardType.Number), isError = false) { age = it }
            DropdownField("Daily activity level", activityLevel, activityOptions) { activityLevel = it }
            DropdownField("Training intensity", trainingIntensity, activityOptions) { trainingIntensity = it }
            TextInput(weeklyWeightChangeTempo, "Weekly weight change (0-1 kg/week)", KeyboardOptions(keyboardType = KeyboardType.Number), isError = false) { weeklyWeightChangeTempo = it }
            TextInput(goalWeight, "Goal weight (kg)", KeyboardOptions(keyboardType = KeyboardType.Number), isError = false) { goalWeight = it }

            Spacer(modifier = Modifier.height(24.dp))

            // Komunikaty walidacyjne
            validationMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontFamily = montserratFont,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            backendMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontFamily = montserratFont,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            DisplayButton("Next", onClick = {
                validationMessage = null
                backendMessage = null

                val missingFields = mutableListOf<String>()
                if (height.isBlank()) missingFields.add("Height")
                if (weight.isBlank()) missingFields.add("Weight")
                if (age.isBlank()) missingFields.add("Age")
                if (weeklyWeightChangeTempo.toFloatOrNull()?.let { it < 0f || it > 1f } != false) missingFields.add("Weekly weight change")
                if (goalWeight.isBlank()) missingFields.add("Goal weight")
                if (sex.isBlank()) missingFields.add("Sex")
                if (activityLevel.isBlank()) missingFields.add("Daily activity")
                if (trainingIntensity.isBlank()) missingFields.add("Training intensity")

                val heightValid = height.toFloatOrNull() != null
                val weightValid = weight.toFloatOrNull() != null
                val ageValid = age.toIntOrNull() != null
                val tempoValid = weeklyWeightChangeTempo.toFloatOrNull() != null
                val goalValid = goalWeight.toFloatOrNull() != null

                if (missingFields.isNotEmpty() || !heightValid || !weightValid || !ageValid || !tempoValid || !goalValid) {
                    validationMessage = "Please fill all fields correctly: ${missingFields.joinToString(", ")}"
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
                    goalWeight = goalWeight.toFloat()
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
            })
        }
    }
}

// Funkcje mapLevelToFloatDaily / Training
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

// Funkcja wysyłająca dane do backendu
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
        client.post("http://10.0.2.2:8080/users/addParameters") {
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
