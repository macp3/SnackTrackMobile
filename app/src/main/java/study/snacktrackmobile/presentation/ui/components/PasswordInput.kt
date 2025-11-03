package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import study.snacktrackmobile.R

@Composable
fun PasswordInput(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    var visibility by remember { mutableStateOf(false) }

    val icon = if (visibility)
        painterResource(R.drawable.baseline_visibility_24)
    else
        painterResource(R.drawable.baseline_visibility_off_24)

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = montserratFont, fontSize = 16.sp, color = Color.Black) },
        singleLine = true,
        modifier = Modifier
            .width(300.dp)
            .background(Color.White, shape = RoundedCornerShape(12.dp)),
        trailingIcon = {
            IconButton(onClick = { visibility = !visibility }) {
                Icon(painter = icon, contentDescription = "Visibility Icon", tint = Color.Black)
            }
        },
        textStyle = TextStyle(fontSize = 18.sp, fontFamily = montserratFont, color = Color.Black),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) Color.Red else Color.Black,
            unfocusedBorderColor = if (isError) Color.Red else Color.Gray,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visibility) VisualTransformation.None else PasswordVisualTransformation(),
        shape = RoundedCornerShape(12.dp),
        isError = isError
    )
}

