package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextInput(
    value: String,
    label: String,
    keyboardOptions: KeyboardOptions? = null,
    isError: Boolean,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = montserratFont) },
        singleLine = true,
        modifier = Modifier.width(300.dp),
        textStyle = TextStyle(fontFamily = montserratFont, fontSize = 18.sp),

        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) Color.Red else Color.Black,
            unfocusedBorderColor = if (isError) Color.Red else Color.Gray,
            errorBorderColor = Color.Red,

            focusedLabelColor = if (isError) Color.Red else Color.Black,
            unfocusedLabelColor = if (isError) Color.Red else Color.Black,
            errorLabelColor = Color.Red,

            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
        ),

        keyboardOptions = keyboardOptions ?: KeyboardOptions.Default,
        shape = RoundedCornerShape(12.dp),
        isError = isError
    )
}
