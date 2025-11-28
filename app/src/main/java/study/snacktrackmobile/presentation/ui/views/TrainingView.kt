package study.snacktrackmobile.presentation.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.R
import study.snacktrackmobile.data.model.dto.TrainingDetailsResponseDTO
import study.snacktrackmobile.viewmodel.TrainingViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun TrainingView(
    viewModel: TrainingViewModel,
    selectedDate: String,
    authToken: String?,
    onDateSelected: (String) -> Unit
) {
    val montserratFont = FontFamily(Font(R.font.montserrat, weight = FontWeight.Normal))

    LaunchedEffect(authToken) {
        if (authToken != null) {
            viewModel.initialize(authToken)
        }
    }

    if (authToken == null || viewModel.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2E7D32))
        }
        return
    }

    viewModel.userTraining?.let { training ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val dayIndex = remember(selectedDate, viewModel.userTrainingAssignedDate, viewModel.trainingDetails) {
                computeTrainingDayIndex(
                    selectedDateStr = selectedDate,
                    assignedDate = viewModel.userTrainingAssignedDate,
                    trainingDetails = viewModel.trainingDetails
                )
            }

            if (dayIndex == null) {
                Text(
                    text = "Unable to determine training day.",
                    fontFamily = montserratFont,
                    color = Color.Black,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                val exercises = viewModel.exercisesForTrainingDay(dayIndex)

                if (exercises.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No exercises for day $dayIndex",
                            fontFamily = montserratFont,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = 5.dp,
                            bottom = 120.dp
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 8.dp)
                    ) {
                        item { Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = training.name,
                                fontFamily = montserratFont,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Button(
                                onClick = { viewModel.depriveTraining(authToken) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(40.dp)
                                    .fillMaxWidth(0.6f)
                            ) {
                                Text("Detach Training", fontFamily = montserratFont, fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                        } }
                        items(exercises) { exercise ->
                            var expanded by remember { mutableStateOf(false) }
                            val rotation by animateFloatAsState(if (expanded) 180f else 0f)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable { expanded = !expanded },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = exercise.name,
                                                fontFamily = montserratFont,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = "${exercise.type} • Difficulty ${exercise.difficulty}",
                                                fontFamily = montserratFont,
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${exercise.numberOfSets}x${exercise.repetitionsPerSet}",
                                                fontFamily = montserratFont,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF2E7D32)
                                            )
                                            Icon(
                                                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier
                                                    .padding(start = 6.dp)
                                                    .size(20.dp)
                                                    .rotate(rotation)
                                            )
                                        }
                                    }

                                    AnimatedVisibility(visible = expanded) {
                                        Text(
                                            text = exercise.description,
                                            fontFamily = montserratFont,
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } ?: run {
        LazyColumn(
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 120.dp
            ),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)

        ) {
            items(viewModel.availableTrainings) { training ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = training.name,
                            fontFamily = montserratFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = training.description,
                            fontFamily = montserratFont,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.assignTraining(training.id, authToken)
                                onDateSelected(LocalDate.now().toString()) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth(0.7f)
                        ) {
                            Text("Assign Training", fontFamily = montserratFont, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

fun computeTrainingDayIndex(
    selectedDateStr: String,
    assignedDate: LocalDate?,
    trainingDetails: TrainingDetailsResponseDTO?
): Int? {
    val selectedDate = try {
        LocalDate.parse(selectedDateStr)
    } catch (e: Exception) {
        return null
    }

    if (assignedDate != null) {
        val daysSinceAssign = ChronoUnit.DAYS.between(assignedDate, selectedDate).toInt()
        return if (daysSinceAssign >= 0) daysSinceAssign + 1 else 1
    }

    return selectedDate.dayOfWeek.value
}
