package study.snacktrackmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.dao.ShoppingListDao
import study.snacktrackmobile.data.model.ShoppingList
import study.snacktrackmobile.data.model.ShoppingListItem

class ShoppingListViewModel(
    private val dao: ShoppingListDao
) : ViewModel() {

    private val _shoppingLists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val shoppingLists: StateFlow<List<ShoppingList>> = _shoppingLists.asStateFlow()

    private var currentDate: String? = null
    private var currentUserEmail: String? = null

    fun setUser(email: String) {
        currentUserEmail = email
    }

    private var shoppingListJob: Job? = null

    fun setDate(date: String) {
        val email = currentUserEmail ?: return
        if (currentDate == date) return
        currentDate = date

        shoppingListJob?.cancel()
        shoppingListJob = viewModelScope.launch {
            dao.getByDateAndUser(date, email).collect { lists ->
                _shoppingLists.value = lists
            }
        }
    }


    fun addNewList(date: String, name: String, onAdded: (Long) -> Unit) {
        val email = currentUserEmail ?: return

        viewModelScope.launch {
            val newList = ShoppingList(
                name = name,
                date = date,
                userEmail = email
            )
            val newId = dao.insert(newList)
            onAdded(newId)
        }
    }



    fun deleteList(list: ShoppingList) {
        viewModelScope.launch {
            dao.delete(list)
        }
    }

    fun addItemToList(list: ShoppingList, itemName: String, quantity: String, description: String = "") {
        viewModelScope.launch {
            val updatedList = list.copy(
                items = list.items + ShoppingListItem(
                    name = itemName,
                    quantity = quantity,
                    description = description
                )
            )
            dao.update(updatedList)
        }
    }

    fun deleteItemFromList(list: ShoppingList, item: ShoppingListItem) {
        viewModelScope.launch {
            val updatedList = list.copy(items = list.items - item)
            dao.update(updatedList)
        }
    }

    fun toggleItemBought(list: ShoppingList, item: ShoppingListItem) {
        viewModelScope.launch {
            val updatedItems = list.items.map {
                if (it == item) it.copy(bought = !it.bought) else it
            }
            val updatedList = list.copy(items = updatedItems)
            dao.update(updatedList)
        }
    }

}

