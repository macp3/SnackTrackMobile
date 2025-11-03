package study.snacktrackmobile.ui.views

import android.R.attr.enabled
import android.R.attr.type
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import study.snacktrackmobile.model.BodyParametersRequest
import study.snacktrackmobile.model.enums.Sex
import study.snacktrackmobile.storage.TokenStorage
import study.snacktrackmobile.ui.components.SnackTrackTopBar
import study.snacktrackmobile.R

@Composable
fun InitialSurveyView(
    onSubmit: (BodyParametersRequest) -> Unit
) {
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

    val sexOptions = listOf(Sex.male.name, Sex.female.name)
    val activityOptions = listOf("None", "Little", "Average", "Intense", "Professional")

    Scaffold(
        topBar = { SnackTrackTopBar() }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text("Please fill initial survey for us to calculate your progress", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center, fontSize = 20.sp)
                Spacer(Modifier.height(24.dp))
                DropdownField("Sex", sex, sexOptions) { sex = it }
                RoundedInputField("Height (cm)", height) { height = it }
                RoundedInputField("Weight (kg)", weight) { weight = it }
                RoundedInputField("Age", age) { age = it }
                DropdownField("How active during the day are you?", activityLevel, activityOptions) { activityLevel = it }
                DropdownField("How intense are your trainings?", trainingIntensity, activityOptions) { trainingIntensity = it }
                RoundedInputField("How fast do you want to loose weight? (0-1kg/week)", weeklyWeightChangeTempo) { weeklyWeightChangeTempo = it }
                RoundedInputField("Goal weight (kg)", goalWeight) { goalWeight = it }
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    val missingFields = mutableListOf<String>()

                    if (height.isBlank()) missingFields.add("Height")
                    if (weight.isBlank()) missingFields.add("Weight")
                    if (age.isBlank()) missingFields.add("Age")
                    if (weeklyWeightChangeTempo.isBlank() || weeklyWeightChangeTempo.toFloat() > 1f || weeklyWeightChangeTempo.toFloat() < 0f) missingFields.add("Weekly weight change")
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
                        Toast.makeText(
                            context,
                            "Please fill all fields correctly:\n${missingFields.joinToString(", ")}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
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
                                Toast.makeText(context, "Data saved", Toast.LENGTH_SHORT).show()
                                onSubmit(request)
                            } else {
                                Toast.makeText(context, "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "No authorization token", Toast.LENGTH_LONG).show()
                        }
                    }
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBFFF99),
                    contentColor = Color.Black
                )) {
                    Text("Next", style = MaterialTheme.typography.titleMedium)
                }

            }
        }
    }
}

@Composable
fun RoundedInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(label: String, selected: String, options: List<String>, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                .padding(vertical = 8.dp),
            shape = MaterialTheme.shapes.large
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun mapLevelToFloatDaily(level: String): Float {
    return when (level) {
        "None" -> 0.7f
        "Little" -> 0.8f
        "Average" -> 0.9f
        "Intense" -> 1f
        "Professional" -> 1.15f
        else -> 0.9f
    }
}

fun mapLevelToFloatTraining(level: String): Float {
    return when (level) {
        "None" -> 0.5f
        "Little" -> 0.6f
        "Average" -> 0.7f
        "Intense" -> 0.8f
        "Professional" -> 0.95f
        else -> 0.7f
    }
}

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
        client.post("${R.string.server_base_url}/addParameters") {
            headers {
                append("Authorization", "Bearer $token")
            }
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
