package study.snacktrackmobile.presentation.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import study.snacktrackmobile.data.model.dto.ApiFoodResponseDetailed
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.data.model.dto.RegisteredAlimentationResponse

@Composable
fun AddRecipeForm(
    name: String,
    desc: String,
    imageUrl: String?, // URL z backendu (dla edycji)
    selectedImageUri: Uri?, // Nowy stan: URI wybrane z galerii
    ingredients: SnapshotStateList<IngredientFormEntry>,

    isNameError: Boolean,
    nameErrorMessage: String?,
    isDescError: Boolean,
    descErrorMessage: String?,
    serverErrorMessage: String?,

    onNameChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit, // Callback po wybraniu zdjęcia
    onStartAddIngredient: () -> Unit,
    onSelectIngredient: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Launcher do wybierania zdjęć (nowoczesny Photo Picker)
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onImageSelected(uri) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color.White)
    ) {
        // --- SEKCJA ZDJĘCIA (Klikalna) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFFF5F5F5)) // Jasne tło
                .clickable {
                    // Otwórz galerię
                    singlePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Logika wyświetlania:
            // 1. Jeśli wybrano nowe zdjęcie z galerii -> Pokaż je
            // 2. Jeśli nie wybrano, ale jest URL z backendu -> Pokaż URL
            // 3. Jeśli nic nie ma -> Pokaż placeholder

            val imageToShow: Any? = selectedImageUri ?: imageUrl

            if (imageToShow != null && imageToShow.toString().isNotBlank()) {
                AsyncImage(
                    model = imageToShow,
                    contentDescription = "Recipe Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Nakładka z ikoną edycji, żeby user wiedział, że może zmienić
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Image",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Text("Tap to change", color = Color.White, fontSize = 12.sp)
                    }
                }
            } else {
                // Placeholder (gdy brak zdjęcia)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Add Photo",
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Add Recipe Photo",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // --- FORMULARZ ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Nazwa
            TextInput(
                value = name,
                onValueChange = onNameChange,
                label = "Recipe Name",
                isError = isNameError,
                modifier = Modifier.fillMaxWidth()
            )
            if (isNameError && nameErrorMessage != null) {
                Text(nameErrorMessage, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Opis
            TextInput(
                value = desc,
                onValueChange = onDescChange,
                label = "Description",
                isError = isDescError,
                modifier = Modifier.fillMaxWidth()
            )
            if (isDescError && descErrorMessage != null) {
                Text(descErrorMessage, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Ingredients", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            // Lista składników (uproszczona dla zwięzłości, wklej swoją logikę pętli)
            Column(modifier = Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ingredients.forEachIndexed { index, entry ->
                    if (entry.essentialFood != null) {
                        val dummy = RegisteredAlimentationResponse(index, 0, entry.essentialFood, null, null, "", entry.amount?:0f, entry.pieces?:0f, "")
                        ProductRow(alimentation = dummy, onDelete = { ingredients.removeAt(index) }, onEdit = { onSelectIngredient(index) })
                    }
                }
            }

            if (serverErrorMessage != null) {
                Text(serverErrorMessage, color = Color.Red, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                DisplayButton(text = "Add Ingredient", onClick = onStartAddIngredient, modifier = Modifier.weight(1f).height(55.dp), fontSize = 14)
                Spacer(modifier = Modifier.width(16.dp))
                DisplayButton(text = "Save Recipe", onClick = onSubmit, modifier = Modifier.weight(1f).height(55.dp), fontSize = 14)
            }
        }
    }
}
data class IngredientFormEntry(
    val essentialFood: EssentialFoodResponse? = null,
    val essentialApi: ApiFoodResponseDetailed? = null,
    val amount: Float? = null,
    val pieces: Float? = null
)