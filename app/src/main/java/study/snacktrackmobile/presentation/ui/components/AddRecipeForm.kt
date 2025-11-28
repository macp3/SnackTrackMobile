package study.snacktrackmobile.presentation.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import coil.compose.AsyncImage
import study.snacktrackmobile.data.model.dto.ApiFoodResponseDetailed
import study.snacktrackmobile.data.model.dto.EssentialFoodResponse
import study.snacktrackmobile.presentation.ui.views.montserratFont

@Composable
fun AddRecipeForm(
    name: String,
    desc: String,
    imageUrl: String?,
    selectedImageUri: Uri?,
    ingredients: MutableList<IngredientFormEntry>,
    isNameError: Boolean,
    nameErrorMessage: String?,
    isDescError: Boolean,
    descErrorMessage: String?,
    serverErrorMessage: String?,
    onNameChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onStartAddIngredient: () -> Unit,
    onSelectIngredient: (Int) -> Unit,
    onDeleteIngredient: (Int) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val maxNameLength = 100
    val maxDescLength = 1024

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> onImageSelected(uri) }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color(0xFFF5F5F5))
                    .clickable {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                val imageToShow: Any? = selectedImageUri ?: imageUrl

                if (imageToShow != null && imageToShow.toString().isNotBlank()) {
                    AsyncImage(
                        model = imageToShow,
                        contentDescription = "Recipe Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Edit, "Edit", tint = Color.White)
                            Text("Tap to change", color = Color.White, fontSize = 12.sp)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, "Add Photo", tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Text("Add Recipe Photo", color = Color.Gray)
                    }
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.8f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                TextInput(value = name, onValueChange = { if (it.length <= maxNameLength) onNameChange(it) }, label = "Recipe Name", isError = isNameError, errorMessage = nameErrorMessage, singleLine = true)
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = desc,
                    onValueChange = { if (it.length <= maxDescLength) onDescChange(it) },
                    label = { Text("Describe how to make...", fontFamily = montserratFont, color = if (isDescError) Color.Red else Color.Gray) },
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    isError = isDescError,
                    singleLine = false,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        unfocusedBorderColor = Color.Black,
                        cursorColor = Color(0xFF2E7D32),
                        focusedLabelColor = Color(0xFF2E7D32),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                if (isDescError && !descErrorMessage.isNullOrBlank()) {
                    Text(text = descErrorMessage, color = Color.Red, fontSize = 12.sp, fontFamily = montserratFont, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }
                Text("${desc.length} / $maxDescLength", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End, style = MaterialTheme.typography.bodySmall, color = if (desc.length >= maxDescLength) Color.Red else Color.Gray, fontFamily = montserratFont)
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = montserratFont, color = Color.Black)
                IconButton(onClick = onStartAddIngredient) { Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF2E7D32)) }
            }
        }

        if (ingredients.isEmpty()) {
            item { Text("No ingredients added.", color = Color.Gray, fontFamily = montserratFont, modifier = Modifier.padding(horizontal = 16.dp)) }
        } else {
            itemsIndexed(ingredients) { index, ingredient ->
                val itemName = ingredient.essentialFood?.name ?: ingredient.essentialApi?.name ?: "Unknown"
                val amountText = if (ingredient.pieces != null && ingredient.pieces > 0) "${ingredient.pieces} pieces" else "${ingredient.amount} g/ml"

                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f).clickable { onSelectIngredient(index) }) {
                            Text(itemName, fontWeight = FontWeight.SemiBold, fontFamily = montserratFont, color = Color.Black)
                            Text(amountText, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontFamily = montserratFont)
                        }
                        IconButton(onClick = { onDeleteIngredient(index) }) { Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red) }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (serverErrorMessage != null) Text(serverErrorMessage, color = Color.Red, fontFamily = montserratFont)
                Spacer(modifier = Modifier.height(16.dp))
                DisplayButton(text = "Save Recipe", onClick = onSubmit, modifier = Modifier.fillMaxWidth().height(50.dp))

                Spacer(modifier = Modifier.height(12.dp))
                DisplayButton(
                    text = "Cancel",
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    containerColor = Color.Gray
                )
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