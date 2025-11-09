package study.snacktrackmobile.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import study.snacktrackmobile.data.model.ShoppingList
import study.snacktrackmobile.data.model.ShoppingListItem
import study.snacktrackmobile.viewmodel.ShoppingListViewModel
import study.snacktrackmobile.presentation.ui.views.montserratFont

@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    selectedDate: String
) {
    var showDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }
    var newlyAddedListId by remember { mutableStateOf<Long?>(null) }
    var showDeleteListDialog by remember { mutableStateOf<ShoppingList?>(null) }

    LaunchedEffect(selectedDate) {
        viewModel.setDate(selectedDate)
    }

    val lists by viewModel.shoppingLists.collectAsState()

    LazyColumn(
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
            Button(
                onClick = { showDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0E0E0),
                    contentColor = Color.Black
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add list")
                Spacer(Modifier.width(8.dp))
                Text("Add new list", fontFamily = montserratFont)
            }
        }
    }

    // Dialog tworzenia nowej listy
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("New Shopping List") },
            text = {
                TextField(
                    value = newListName,
                    onValueChange = { if (it.length <= 40) newListName = it }, // limit 40 znaków
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

    // Dialog potwierdzenia usunięcia listy
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Nagłówek listy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    list.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = montserratFont
                )
                IconButton(onClick = onDeleteList) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete list")
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
                            onCheckedChange = { viewModel.toggleItemBought(list, item) }
                        )

                        Text(
                            text = if (item.name.length > 50) item.name.take(50) else item.name,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Visible // nie skracamy z "..."
                        )

                        Text(
                            text = if (item.quantity.length > 9) item.quantity.take(9) else item.quantity,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .widthIn(max = 80.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        IconButton(onClick = { viewModel.deleteItemFromList(list, item) }) {
                            Text("X", color = Color.Red)
                        }
                    }

                    // Description z ikoną rozwinięcia
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
                            color = Color.Gray // jaśniejszy kolor
                        )
                        if (!item.description.isNullOrBlank()) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand description"
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            IconButton(onClick = { showAddItemDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        }
    }

    // Dialog dodawania pozycji
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
                        viewModel.addItemToList(
                            list,
                            newItemName,
                            newItemQuantity,
                            newItemDescription
                        )
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
}
