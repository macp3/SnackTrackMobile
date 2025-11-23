package study.snacktrackmobile.presentation.ui.components

import DropdownField
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import study.snacktrackmobile.R
import study.snacktrackmobile.data.model.dto.BodyParametersRequest
import study.snacktrackmobile.data.model.enums.Sex
import study.snacktrackmobile.data.storage.TokenStorage
import study.snacktrackmobile.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBodyParametersScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bodyParameters by viewModel.bodyParameters.collectAsState()

    val montserratFont = FontFamily(Font(R.font.montserrat, weight = FontWeight.Normal))

    LaunchedEffect(Unit) {
        TokenStorage.getToken(context)?.let { viewModel.getBodyParameters(it) }
    }

    var sex by remember { mutableStateOf(Sex.male) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("Average") }
    var trainingIntensity by remember { mutableStateOf("Average") }
    var weeklyWeightChangeTempo by remember { mutableStateOf("") }
    var goalWeight by remember { mutableStateOf("") }

    // populate from saved body parameters
    LaunchedEffect(bodyParameters) {
        bodyParameters?.let {
            sex = it.sex
            height = it.height.toString()
            weight = it.weight.toString()
            age = it.age.toString()
            weeklyWeightChangeTempo = it.weeklyWeightChangeTempo.toString()
            goalWeight = it.goalWeight.toString()
        }
    }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Text(
                    "Edit Body Parameters",
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = montserratFont,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            // 🔹 DropdownField zamiast DropdownSelector
            item {
                DropdownField(
                    label = "Sex",
                    options = Sex.entries.map { it.name },
                    selected = sex.name,
                    onSelected = { sex = Sex.valueOf(it) }
                )
            }

            // 🔹 TextInput zamiast NumberTextField
            item {
                TextInput(
                    value = height,
                    onValueChange = { height = it },
                    label = "Height (cm)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                TextInput(
                    value = weight,
                    onValueChange = { weight = it },
                    label = "Weight (kg)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                TextInput(
                    value = age,
                    onValueChange = { age = it },
                    label = "Age",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            item {
                DropdownField(
                    label = "Daily activity",
                    options = listOf("None", "Little", "Average", "Intense", "Professional"),
                    selected = activityLevel,
                    onSelected = { activityLevel = it }
                )
            }

            item {
                DropdownField(
                    label = "Training intensity",
                    options = listOf("None", "Little", "Average", "Intense", "Professional"),
                    selected = trainingIntensity,
                    onSelected = { trainingIntensity = it }
                )
            }

            item {
                TextInput(
                    value = weeklyWeightChangeTempo,
                    onValueChange = { weeklyWeightChangeTempo = it },
                    label = "Weekly weight change (kg/week)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                TextInput(
                    value = goalWeight,
                    onValueChange = { goalWeight = it },
                    label = "Goal weight (kg)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // 🔹 DisplayButton zamiast zwykłego Button
            item {
                DisplayButton(
                    text = "Save changes",
                    onClick = {
                        val h = height.toFloatOrNull()
                        val w = weight.toFloatOrNull()
                        val a = age.toIntOrNull()
                        val weekly = weeklyWeightChangeTempo.toFloatOrNull()
                        val goal = goalWeight.toFloatOrNull()

                        validationError = when {
                            h == null || h <= 0f -> "Please enter a valid height"
                            w == null || w <= 0f -> "Please enter a valid weight"
                            a == null || a <= 0 -> "Please enter a valid age"
                            weekly == null -> "Please enter valid weekly weight change"
                            goal == null || goal <= 0f -> "Please enter a valid goal weight"
                            else -> null
                        }

                        if (validationError == null) {
                            scope.launch {
                                TokenStorage.getToken(context)?.let { token ->
                                    val req = BodyParametersRequest(
                                        sex = sex,
                                        height = h,
                                        weight = w,
                                        age = a,
                                        dailyActivityFactor = mapLevelToFloatDaily(activityLevel),
                                        dailyActivityTrainingFactor = mapLevelToFloatTraining(trainingIntensity),
                                        weeklyWeightChangeTempo = weekly,
                                        goalWeight = goal
                                    )
                                    viewModel.changeBodyParameters(token, req)
                                    showSuccessDialog = true
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

        }
    }

    // 🔹 AlertDialog bez zmian
    validationError?.let { msg ->
        AlertDialog(
            onDismissRequest = { validationError = null },
            title = { Text("Invalid input") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { validationError = null }) { Text("OK") }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBack()
            },
            title = { Text("Success") },
            text = { Text("Body parameters updated successfully!") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onBack()
                }) { Text("OK") }
            }
        )
    }
}


fun mapLevelToFloatDaily(level: String): Float = when (level) {
    "None" -> 0.7f
    "Little" -> 0.8f
    "Average" -> 0.9f
    "Intense" -> 1.0f
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
