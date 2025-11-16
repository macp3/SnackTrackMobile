package study.snacktrackmobile.presentation.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Edit Body Parameters",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = montserratFont,
                        color = Color.Black
                    )
                }

                item {
                    DropdownSelector(
                        label = "Sex",
                        options = Sex.entries.map { it.name },
                        selected = sex.name,
                        onSelect = { sex = Sex.valueOf(it) }
                    )
                }

                item { NumberTextField(label = "Height (cm)", value = height, onValueChange = { height = it }) }
                item { NumberTextField(label = "Weight (kg)", value = weight, onValueChange = { weight = it }) }
                item { NumberTextField(label = "Age", value = age, onValueChange = { age = it }) }

                item {
                    DropdownSelector(
                        label = "Daily activity",
                        options = listOf("None", "Little", "Average", "Intense", "Professional"),
                        selected = activityLevel,
                        onSelect = { activityLevel = it }
                    )
                }

                item {
                    DropdownSelector(
                        label = "Training intensity",
                        options = listOf("None", "Little", "Average", "Intense", "Professional"),
                        selected = trainingIntensity,
                        onSelect = { trainingIntensity = it }
                    )
                }

                item { NumberTextField(label = "Weekly weight change (kg/week)", value = weeklyWeightChangeTempo, onValueChange = { weeklyWeightChangeTempo = it }) }
                item { NumberTextField(label = "Goal weight (kg)", value = goalWeight, onValueChange = { goalWeight = it }) }

                item {
                    Button(
                        onClick = {
                            // validate numbers
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save changes", color = Color.White, fontFamily = montserratFont)
                    }
                }
            }
        }
    }

    // show validation error
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

    // success dialog
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

@Composable
fun NumberTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= 6) onValueChange(it.filter { c -> c.isDigit() || c == '.' }) },
        label = { Text(label, color = Color.Black, fontFamily = montserratFont) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(
            fontFamily = montserratFont,
            fontSize = 16.sp,
            color = Color.Black        // ← tekst wpisywany czarny
        ),
    )
}

@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 180f else 0f)

    Column(Modifier.fillMaxWidth()) {
        Text(label, color = Color.DarkGray, modifier = Modifier.padding(bottom = 4.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(selected, color = Color.Black)
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation),
                    tint = Color(0xFF2E7D32)
                )
            }
        }

        AnimatedVisibility(expanded) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(option)
                                    expanded = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option, color = Color.Black)
                        }
                        Divider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }
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
