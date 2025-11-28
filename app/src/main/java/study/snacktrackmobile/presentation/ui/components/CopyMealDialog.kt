package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import study.snacktrackmobile.presentation.ui.views.montserratFont
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyMealDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    mealOptions: List<String>,
    initialMeal: String = "breakfast"
) {
    var fromMealName by remember { mutableStateOf(initialMeal) }
    var showDatePicker by remember { mutableStateOf(false) }

    val initialDateMillis = remember { Calendar.getInstance().timeInMillis }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    val formattedDate by remember(datePickerState.selectedDateMillis) {
        derivedStateOf {
            val millis = datePickerState.selectedDateMillis ?: initialDateMillis
            Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    val confirmAction: () -> Unit = {
        val millis = datePickerState.selectedDateMillis
        if (millis != null) {
            val calendar = Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.MONDAY
                timeInMillis = millis
            }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val fromDate = String.format("%04d-%02d-%02d", year, month, day)

            onConfirm(fromDate, fromMealName)
        }
        onDismiss()
    }

    val interactionSource = remember { MutableInteractionSource() }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier.wrapContentSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = montserratFont,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select date",
                                    tint = Color(0xFF2E7D32)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF2E7D32),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF2E7D32),
                            unfocusedLabelColor = Color.Gray,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { showDatePicker = true }
                    )
                }

                DropdownField(
                    label = "Source meal",
                    selected = fromMealName,
                    options = mealOptions,
                    onSelected = { fromMealName = it },
                    modifier = Modifier.width(300.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DisplayButton(
                        text = "Cancel",
                        onClick = { onDismiss() },
                        modifier = Modifier.width(150.dp),
                        fontSize = 16
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    DisplayButton(
                        text = "Confirm",
                        onClick = confirmAction,
                        modifier = Modifier.width(150.dp),
                        fontSize = 16
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            modifier = Modifier.fillMaxSize(),
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                DisplayButton(
                    text = "OK",
                    onClick = { showDatePicker = false },
                    modifier = Modifier.width(120.dp),
                    fontSize = 16
                )
            },
            dismissButton = {
                DisplayButton(
                    text = "Cancel",
                    onClick = { showDatePicker = false },
                    modifier = Modifier.width(120.dp),
                    fontSize = 16
                )
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color.White
            )
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth(),
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF2E7D32),
                    todayDateBorderColor = Color(0xFF2E7D32),
                    todayContentColor = Color(0xFF2E7D32)
                )
            )
        }
    }
}
