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

    // Ustawienie początkowej daty na dzisiaj
    val initialDateMillis = remember { Calendar.getInstance().timeInMillis }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    // Wartość do wyświetlenia w polu tekstowym (format YYYY-MM-DD)
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

    // InteractionSource jest potrzebny, aby wyłączyć efekt "ripple" przy kliknięciu,
    // jeśli przeszkadza, ale przede wszystkim pomaga obsłużyć kliknięcie na readOnly TextField
    val interactionSource = remember { MutableInteractionSource() }

    // 1. GŁÓWNE OKNO DIALOGOWE
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
                // 🔹 Tytuł
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = montserratFont,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth()
                )

                // 🔹 POLE TEKSTOWE DATY
                // Box pozwala na przechwycenie kliknięcia nad polem tekstowym
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = {},
                        readOnly = true, // Ważne: użytkownik nie może wpisywać z klawiatury
                        label = { Text("Select Date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select date",
                                    tint = Color(0xFF2E7D32) // Zielona ikonka pasująca do stylu
                                )
                            }
                        },
                        // 🔹 ZMIANA KOLORÓW TUTAJ:
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,   // Czarny tekst gdy aktywne
                            unfocusedTextColor = Color.Black, // Czarny tekst gdy nieaktywne (to naprawia Twój problem)
                            disabledTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF2E7D32), // Zielona ramka gdy aktywne
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF2E7D32),
                            unfocusedLabelColor = Color.Gray,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Przezroczysta warstwa clickable na wierzchu pola tekstowego.
                    // To gwarantuje, że kliknięcie w dowolnym miejscu pola otworzy kalendarz.
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null // Brak efektu kliknięcia wizualnego na polu
                            ) { showDatePicker = true }
                    )
                }

                // 🔹 DropdownField
                DropdownField(
                    label = "Source meal",
                    selected = fromMealName,
                    options = mealOptions,
                    onSelected = { fromMealName = it },
                    modifier = Modifier.width(300.dp)
                )

                // 🔹 Przyciski
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

    // 2. OKNO DIALOGOWE KALENDARZA
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
                containerColor = Color.White // Białe tło kalendarza
            )
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier.fillMaxWidth(),
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Color(0xFF2E7D32), // Zielony wybór
                    todayDateBorderColor = Color(0xFF2E7D32),
                    todayContentColor = Color(0xFF2E7D32)
                )
            )
        }
    }
}