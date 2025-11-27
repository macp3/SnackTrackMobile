package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import study.snacktrackmobile.presentation.ui.views.montserratFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    isError: Boolean = false,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontFamily = montserratFont) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(
                fontFamily = montserratFont,
                color = Color.Black // ✅ Wymuszony czarny kolor tekstu
            ),
            colors = OutlinedTextFieldDefaults.colors(
                // ✅ Kolory tekstu
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,

                // Obramowania
                focusedBorderColor = if (isError) Color.Red else Color(0xFF2E7D32), // Zielony jak w reszcie apki
                unfocusedBorderColor = if (isError) Color.Red else Color.Black,
                errorBorderColor = Color.Red,

                // Etykiety
                focusedLabelColor = if (isError) Color.Red else Color(0xFF2E7D32),
                unfocusedLabelColor = if (isError) Color.Red else Color.Black,
                errorLabelColor = Color.Red,

                // Tło
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
            ),
            isError = isError
        )

        // ✅ Zmiana na ExposedDropdownMenu (lepiej działa z Boxem)
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White) // ✅ Białe tło usuwa czarne rogi
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = montserratFont,
                            color = Color.Black // ✅ Tekst w opcjach też czarny
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}