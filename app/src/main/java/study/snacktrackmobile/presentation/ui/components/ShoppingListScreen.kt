package study.snacktrackmobile.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import study.snacktrackmobile.data.model.ShoppingList
import study.snacktrackmobile.presentation.ui.views.montserratFont
import study.snacktrackmobile.viewmodel.ShoppingListViewModel

@Composable
fun ShoppingListScreen(viewModel: ShoppingListViewModel) {
    val lists by viewModel.shoppingLists.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(lists) { list ->
                ShoppingListItemCard(list = list, onDeleteList = { viewModel.deleteList(list) })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.addNewList() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add list")
            Spacer(Modifier.width(8.dp))
            Text("Add new list", fontFamily = montserratFont,)
        }
    }
}

@Composable
fun ShoppingListItemCard(list: ShoppingList, onDeleteList: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(list.name, style = MaterialTheme.typography.titleMedium, fontFamily = montserratFont,)
                IconButton(onClick = onDeleteList) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete list")
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                list.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.name, fontFamily = montserratFont,)
                        Text(item.quantity, fontFamily = montserratFont,)
                        Checkbox(
                            checked = item.bought,
                            onCheckedChange = { /* obsługa zmiany stanu kupione */ }
                        )
                    }
                }

                IconButton(onClick = { /* dodanie nowej pozycji */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Add item")
                }
            }
        }
    }
}
