package study.snacktrackmobile.presentation.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import study.snacktrackmobile.presentation.ui.views.montserratFont

@Composable
fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontFamily = montserratFont, color = if (isError) Color.Red else Color.Black) },
            singleLine = singleLine, // 🔹 Użycie parametru
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = montserratFont,
                fontSize = 18.sp,
                color = Color.Black
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) Color.Red else Color.Black,
                unfocusedBorderColor = if (isError) Color.Red else Color.Black,
                errorBorderColor = Color.Red,

                focusedLabelColor = if (isError) Color.Red else Color.Black,
                unfocusedLabelColor = if (isError) Color.Red else Color.Black,
                errorLabelColor = Color.Red,

                cursorColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                errorTextColor = Color.Red,

                // --- KLUCZOWA ZMIANA: JAWNE USTAWIENIE BIAŁEGO TŁA KONTENERA ---
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorContainerColor = Color.White,
                // -----------------------------------------------------------------
            ),
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            isError = isError
        )

        // 🔹 Wyświetlanie komunikatu błędu pod polem
        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = montserratFont,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ImagePicker(
    selectedImageUri: Uri?,
    existingImageUrl: String?,
    onImageSelected: (Uri?) -> Unit
) {
    // Launcher do systemowego wybierania zdjęć
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onImageSelected(uri) }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray)
            .clickable {
                // Uruchomienie pickera
                singlePhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // 1. Priorytet: Nowe zdjęcie wybrane z galerii
        if (selectedImageUri != null) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Selected Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        // 2. Priorytet: Istniejące zdjęcie z backendu (podczas edycji)
        else if (!existingImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = existingImageUrl,
                contentDescription = "Recipe Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        // 3. Placeholder (gdy brak zdjęcia)
        else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Add Image",
                    tint = Color.DarkGray,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Tap to add image",
                    color = Color.DarkGray,
                    fontFamily = montserratFont
                )
            }
        }
    }
}