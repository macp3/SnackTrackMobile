package study.snacktrackmobile.presentation.ui.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.dao.ShoppingListDao
import study.snacktrackmobile.data.model.ShoppingList
import study.snacktrackmobile.data.model.ShoppingListItem

class ShoppingListViewModel(
    private val dao: ShoppingListDao
) : ViewModel() {

    // Flow przechowujący listy zakupów
    private val _shoppingLists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val shoppingLists: StateFlow<List<ShoppingList>> = _shoppingLists.asStateFlow()

    init {
        loadShoppingLists()
    }

    // Ładowanie list z bazy danych
    private fun loadShoppingLists() {
        viewModelScope.launch {
            val lists = dao.getAll()
            _shoppingLists.value = lists
        }
    }

    // Dodawanie nowej listy (przykładowa nazwa)
    fun addNewList() {
        viewModelScope.launch {
            val newList = ShoppingList(name = "New List")
            dao.insert(newList)
            loadShoppingLists()
        }
    }

    // Usuwanie listy
    fun deleteList(list: ShoppingList) {
        viewModelScope.launch {
            dao.delete(list)
            loadShoppingLists()
        }
    }

    // Aktualizacja listy (np. dodanie nowego elementu)
    fun addItemToList(list: ShoppingList, item: ShoppingListItem) {
        viewModelScope.launch {
            val updatedList = list.copy(items = list.items + item)
            dao.update(updatedList)
            loadShoppingLists()
        }
    }

    // Aktualizacja stanu kupionego elementu
    fun toggleItemBought(list: ShoppingList, item: ShoppingListItem) {
        viewModelScope.launch {
            val updatedItems = list.items.map {
                if (it == item) it.copy(bought = !it.bought) else it
            }
            val updatedList = list.copy(items = updatedItems)
            dao.update(updatedList)
            loadShoppingLists()
        }
    }
}
