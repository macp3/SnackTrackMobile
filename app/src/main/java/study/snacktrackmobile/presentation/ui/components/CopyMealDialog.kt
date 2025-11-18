package study.snacktrackmobile.presentation.ui.components

import DropdownField
import androidx.compose.foundation.clickable
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
import study.snacktrackmobile.presentation.ui.views.montserratFont // Upewnij się, że to jest poprawne
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
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

    // Ustawienie początkowej daty na dzisiaj (lub wartość domyślną)
    val initialDateMillis = remember { Calendar.getInstance().timeInMillis }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    // Wartość do wyświetlenia w polu tekstowym (format YYYY-MM-DD)
    val formattedDate by remember(datePickerState.selectedDateMillis) {
        derivedStateOf {
            val millis = datePickerState.selectedDateMillis ?: initialDateMillis
            Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ISO_LOCAL_DATE) // Format: 2025-01-25
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


    // 1. GŁÓWNE OKNO DIALOGOWE (Zawiera pole daty, dropdown i przyciski)
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            // Używamy Modifier.wrapContentSize(), aby Card dostosował się do zawartości
            // (w tym do DatePicker, jeśli ma minimalną szerokość)
            modifier = Modifier.wrapContentSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🔹 Tytuł
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = montserratFont,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )

                // 🔹 POLE TEKSTOWE DATY (clickable)
                OutlinedTextField(
                    value = formattedDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        // Umożliwia kliknięcie na całym polu, by otworzyć kalendarz
                        .clickable { showDatePicker = true }
                )


                // 🔹 DropdownField
                DropdownField(
                    label = "Source meal",
                    selected = fromMealName,
                    options = mealOptions,
                    onSelected = { fromMealName = it },
                    modifier = Modifier.width(300.dp)
                )

                // 🔹 Customowe Przyciski
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

    // 2. OKNO DIALOGOWE KALENDARZA (Wyskakuje po kliknięciu pola daty)
    if (showDatePicker) {
        DatePickerDialog(
            modifier = Modifier.fillMaxSize(),
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                DisplayButton(
                    text = "OK",
                    onClick = { showDatePicker = false }, // Tylko zamyka dialog, data jest już wybrana
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
            }
        ) {
            // Ustawienia DatePicker, które mają szanse na wyświetlenie 7 dni
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}