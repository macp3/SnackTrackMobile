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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import study.snacktrackmobile.R
import study.snacktrackmobile.data.model.dto.BodyParametersRequest
import study.snacktrackmobile.data.model.enums.DietType
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
        val token = TokenStorage.getToken(context)
        if (token != null) viewModel.getBodyParameters(token)
    }

    var sex by remember { mutableStateOf(Sex.male) }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var activityLevel by remember { mutableStateOf("Average") }
    var trainingIntensity by remember { mutableStateOf("Average") }
    var weeklyWeightChangeTempo by remember { mutableStateOf("") }
    var goalWeight by remember { mutableStateOf("") }
    var selectedDiet by remember { mutableStateOf(DietType.balanced) }
    var expandedDiet by remember { mutableStateOf(false) }

    LaunchedEffect(bodyParameters) {
        bodyParameters?.let {
            sex = it.sex
            height = it.height.toString()
            weight = it.weight.toString()
            age = it.age.toString()
            weeklyWeightChangeTempo = it.weeklyWeightChangeTempo.toString()
            goalWeight = it.goalWeight.toString()
            selectedDiet = it.preferredDiet
        }
    }

    val dietOptions = DietType.entries

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
                item { Text("Edit Body Parameters", style = MaterialTheme.typography.headlineSmall, fontFamily = montserratFont) }

                item {
                    DropdownSelector(label = "Sex", options = Sex.entries.map { it.name }, selected = sex.name, onSelect = { sex = Sex.valueOf(it) })
                }

                item { OutlinedTextField(value = height, onValueChange = { height = it }, label = { Text("Height (cm)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }

                item { OutlinedTextField(value = weight, onValueChange = { weight = it }, label = { Text("Weight (kg)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }

                item { OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }

                item { DropdownSelector(label = "Daily activity", options = listOf("None","Little","Average","Intense","Professional"), selected = activityLevel, onSelect = { activityLevel = it }) }

                item { DropdownSelector(label = "Training intensity", options = listOf("None","Little","Average","Intense","Professional"), selected = trainingIntensity, onSelect = { trainingIntensity = it }) }

                item { OutlinedTextField(value = weeklyWeightChangeTempo, onValueChange = { weeklyWeightChangeTempo = it }, label = { Text("Weekly weight change (kg/week)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }

                item { OutlinedTextField(value = goalWeight, onValueChange = { goalWeight = it }, label = { Text("Goal weight (kg)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) }

                item {
                    ExposedDropdownMenuBox(
                        expanded = expandedDiet,
                        onExpandedChange = { expandedDiet = !expandedDiet }
                    ) {
                        OutlinedTextField(
                            value = selectedDiet.name.replace("_", " ").replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            label = { Text("Preferred Diet") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDiet) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expandedDiet, onDismissRequest = { expandedDiet = false }) {
                            dietOptions.forEach { diet ->
                                DropdownMenuItem(text = { Text(diet.name.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                                    onClick = { selectedDiet = diet; expandedDiet = false })
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            scope.launch {
                                val token = TokenStorage.getToken(context)
                                if (token != null) {
                                    val req = BodyParametersRequest(
                                        sex = sex,
                                        height = height.toFloatOrNull() ?: 0f,
                                        weight = weight.toFloatOrNull() ?: 0f,
                                        age = age.toIntOrNull() ?: 0,
                                        dailyActivityFactor = mapLevelToFloatDaily(activityLevel),
                                        dailyActivityTrainingFactor = mapLevelToFloatTraining(trainingIntensity),
                                        weeklyWeightChangeTempo = weeklyWeightChangeTempo.toFloatOrNull() ?: 0f,
                                        goalWeight = goalWeight.toFloatOrNull() ?: 0f,
                                        preferredDiet = selectedDiet
                                    )
                                    viewModel.changeBodyParameters(token, req)
                                    onBack()
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


// 🔹 mapowanie aktywności
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
