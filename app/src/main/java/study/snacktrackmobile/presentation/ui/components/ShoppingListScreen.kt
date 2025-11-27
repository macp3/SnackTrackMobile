package study.snacktrackmobile.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // 🔹 Dodano import dla fontSize
import study.snacktrackmobile.data.model.ShoppingList
import study.snacktrackmobile.data.model.ShoppingListItem
import study.snacktrackmobile.viewmodel.ShoppingListViewModel
import study.snacktrackmobile.presentation.ui.views.montserratFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    selectedDate: String,
    isUserPremium: Boolean,
    onNavigateToPremium: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var newListName by remember { mutableStateOf("") }
    var newlyAddedListId by remember { mutableStateOf<Long?>(null) }
    var showDeleteListDialog by remember { mutableStateOf<ShoppingList?>(null) }

    // AI & Premium States
    var showAiDialog by remember { mutableStateOf(false) }
    var showPremiumUpsellDialog by remember { mutableStateOf(false) }
    var aiPrompt by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(selectedDate) {
        viewModel.setDate(selectedDate)
    }

    val lists by viewModel.shoppingLists.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lists) { list ->
                val isNew = list.id == newlyAddedListId
                val backgroundColor by animateColorAsState(
                    targetValue = if (isNew) Color(0xFFE0F7FA) else Color(0xFFF5F5F5)
                )

                ShoppingListItemCard(
                    list = list,
                    backgroundColor = backgroundColor,
                    viewModel = viewModel,
                    onDeleteList = { showDeleteListDialog = list }
                )

                if (isNew) {
                    LaunchedEffect(list.id) {
                        kotlinx.coroutines.delay(2000)
                        newlyAddedListId = null
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. ADD BUTTON
                    Button(
                        onClick = { showDialog = true },
                        modifier = Modifier.weight(1f),
                        // 🔹 ZMNIEJSZENIE PADDINGU WEWNĘTRZNEGO
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp) // 🔹 MNIEJSZA IKONA
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "New",
                            fontFamily = montserratFont,
                            color = Color.Black,
                            maxLines = 1,
                            fontSize = 12.sp // 🔹 MNIEJSZA CZCIONKA
                        )
                    }

                    // 2. COPY BUTTON
                    Button(
                        onClick = { showCopyDialog = true },
                        modifier = Modifier.weight(1f),
                        // 🔹 ZMNIEJSZENIE PADDINGU WEWNĘTRZNEGO
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp) // 🔹 MNIEJSZA IKONA
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Copy",
                            fontFamily = montserratFont,
                            color = Color.Black,
                            maxLines = 1,
                            fontSize = 12.sp // 🔹 MNIEJSZA CZCIONKA
                        )
                    }

                    // 3. AI BUTTON
                    Button(
                        onClick = {
                            if (isUserPremium) {
                                showAiDialog = true
                            } else {
                                showPremiumUpsellDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        // 🔹 ZMNIEJSZENIE PADDINGU WEWNĘTRZNEGO
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUserPremium) Color(0xFFD1C4E9) else Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        )
                    ) {
                        if (isUserPremium) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp) // 🔹 MNIEJSZA IKONA
                            )
                        } else {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp) // 🔹 MNIEJSZA IKONA
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "AI",
                            fontFamily = montserratFont,
                            color = Color.Black,
                            fontSize = 12.sp // 🔹 MNIEJSZA CZCIONKA
                        )
                    }
                }
            }
        }

        // Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }

    // --- DIALOGS ---

    // 1. PREMIUM UPSELL DIALOG
    if (showPremiumUpsellDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumUpsellDialog = false },
            icon = {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(48.dp))
            },
            title = { Text("Premium Feature") },
            text = {
                Text(
                    "AI Shopping Assistant is available only for Premium users.\n\nUpgrade now to let AI plan your shopping based on your history!",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPremiumUpsellDialog = false
                        onNavigateToPremium()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Go Premium")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumUpsellDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 2. AI Dialog
    if (showAiDialog) {
        AlertDialog(
            onDismissRequest = { showAiDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("AI Assistant")
                }
            },
            text = {
                Column {
                    Text("Do you want to give more information for your shopping list?")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        label = { Text("e.g. I want to make pizza today") },
                        placeholder = { Text("Leave empty for auto-generation") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "We will use your previous shopping history to personalize the results.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAiDialog = false
                        viewModel.generateAiShoppingList(
                            userPrompt = aiPrompt,
                            selectedDate = selectedDate,
                            onSuccess = { aiPrompt = "" },
                            onError = { error -> println("AI Error: $error") }
                        )
                    }
                ) { Text("Generate") }
            },
            dismissButton = {
                TextButton(onClick = { showAiDialog = false }) { Text("Cancel") }
            }
        )
    }

    // 3. Create New List Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New Shopping List") },
            text = {
                TextField(
                    value = newListName,
                    onValueChange = { if (it.length <= 40) newListName = it },
                    label = { Text("List name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newListName.isNotBlank()) {
                        viewModel.addNewList(selectedDate, newListName) { newId ->
                            newlyAddedListId = newId
                        }
                        newListName = ""
                        showDialog = false
                    }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }

    // 4. Copy List Dialog
    if (showCopyDialog) {
        DatePickerDialog(
            onDismissRequest = { showCopyDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            val formattedDate = formatter.format(Date(selectedMillis))
                            viewModel.copyListsFromDate(formattedDate, selectedDate)
                        }
                        showCopyDialog = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showCopyDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 5. Delete Confirmation Dialog
    if (showDeleteListDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteListDialog = null },
            title = { Text("Delete List") },
            text = { Text("Are you sure you want to delete the list '${showDeleteListDialog!!.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteListDialog?.let { viewModel.deleteList(it) }
                    showDeleteListDialog = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteListDialog = null }) { Text("Cancel") }
            }
        )
    }
}

// Element listy zakupów (Card)
@Composable
fun ShoppingListItemCard(
    list: ShoppingList,
    backgroundColor: Color,
    onDeleteList: () -> Unit,
    viewModel: ShoppingListViewModel
) {
    var showAddItemDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var newItemQuantity by remember { mutableStateOf("") }
    var newItemDescription by remember { mutableStateOf("") }
    var showEditItemDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ShoppingListItem?>(null) }
    var editedItemName by remember { mutableStateOf("") }
    var editedItemQuantity by remember { mutableStateOf("") }
    var editedItemDescription by remember { mutableStateOf("") }
    var showEditListDialog by remember { mutableStateOf(false) }
    var editedListName by remember { mutableStateOf(list.name) }


    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    list.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = montserratFont,
                    modifier = Modifier.weight(1f),
                    color = Color.Black
                )

                IconButton(onClick = { showEditListDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit list name", tint = Color.Black)
                }

                IconButton(onClick = onDeleteList) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete list", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            list.items.forEach { item ->
                var expanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.bought,
                            onCheckedChange = { viewModel.toggleItemBought(list, item) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF2E7D32),
                                uncheckedColor = Color.Gray,
                                checkmarkColor = Color.White
                            )
                        )

                        Text(
                            text = if (item.name.length > 50) item.name.take(50) else item.name,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Visible,
                            color = Color.Black
                        )

                        Text(
                            text = if (item.quantity.length > 9) item.quantity.take(9) else item.quantity,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .widthIn(max = 80.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.Black
                        )

                        Row {
                            IconButton(onClick = {
                                itemToEdit = item
                                editedItemName = item.name
                                editedItemQuantity = item.quantity
                                editedItemDescription = item.description
                                showEditItemDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit item", tint = Color.Black)
                            }

                            IconButton(onClick = { viewModel.deleteItemFromList(list, item) }) {
                                Icon(Icons.Default.Close, contentDescription = "Delete item", tint = Color.Black)
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                            .padding(start = 32.dp)
                    ) {
                        Text(
                            text = item.description ?: "",
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            color = Color.Gray
                        )
                        if (!item.description.isNullOrBlank()) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand description",
                                tint = Color.Black
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            IconButton(onClick = { showAddItemDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add item", tint = Color.Black)
            }
        }
    }

    // --- DIALOGI ITEMÓW ---
    if (showAddItemDialog) {
        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            title = { Text("Add Item") },
            text = {
                Column {
                    TextField(
                        value = newItemName,
                        onValueChange = { if (it.length <= 35) newItemName = it },
                        label = { Text("Item name") },
                        singleLine = true
                    )
                    TextField(
                        value = newItemQuantity,
                        onValueChange = { if (it.length <= 9) newItemQuantity = it },
                        label = { Text("Quantity") },
                        singleLine = true
                    )
                    TextField(
                        value = newItemDescription,
                        onValueChange = { newItemDescription = it },
                        label = { Text("Description") },
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newItemName.isNotBlank() && newItemQuantity.isNotBlank()) {
                        viewModel.addItemToList(list, newItemName, newItemQuantity, newItemDescription)
                        newItemName = ""
                        newItemQuantity = ""
                        newItemDescription = ""
                        showAddItemDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) { Text("Cancel") }
            }
        )
    }
    if (showEditItemDialog && itemToEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditItemDialog = false },
            title = { Text("Edit Item") },
            text = {
                Column {
                    TextField(
                        value = editedItemName,
                        onValueChange = { if (it.length <= 35) editedItemName = it },
                        label = { Text("Item name") },
                        singleLine = true
                    )
                    TextField(
                        value = editedItemQuantity,
                        onValueChange = { if (it.length <= 9) editedItemQuantity = it },
                        label = { Text("Quantity") },
                        singleLine = true
                    )
                    TextField(
                        value = editedItemDescription,
                        onValueChange = { editedItemDescription = it },
                        label = { Text("Description") },
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val oldItem = itemToEdit ?: return@TextButton
                    viewModel.editItemInList(list, oldItem, editedItemName, editedItemQuantity, editedItemDescription)
                    showEditItemDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditItemDialog = false }) { Text("Cancel") }
            }
        )
    }
    if (showEditListDialog) {
        AlertDialog(
            onDismissRequest = { showEditListDialog = false },
            title = { Text("Edit List Name") },
            text = {
                TextField(
                    value = editedListName,
                    onValueChange = { if (it.length <= 40) editedListName = it },
                    label = { Text("List name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (editedListName.isNotBlank()) {
                        viewModel.updateListName(list, editedListName)
                        showEditListDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditListDialog = false }) { Text("Cancel") }
            }
        )
    }
}