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
    private var shoppingListJob: Job? = null

    fun setUser(email: String) {
        currentUserEmail = email
    }

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

            // Odświeżenie list po dodaniu
            val updatedLists = dao.getByDateAndUserOnce(date, email)
            _shoppingLists.value = updatedLists

            onAdded(newId)
        }
    }

    fun deleteList(list: ShoppingList) {
        viewModelScope.launch {
            dao.delete(list)
            // Odświeżenie list
            val email = currentUserEmail ?: return@launch
            val date = currentDate ?: return@launch
            _shoppingLists.value = dao.getByDateAndUserOnce(date, email)
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
            refreshCurrentLists()
        }
    }

    fun deleteItemFromList(list: ShoppingList, item: ShoppingListItem) {
        viewModelScope.launch {
            val updatedList = list.copy(items = list.items - item)
            dao.update(updatedList)
            refreshCurrentLists()
        }
    }

    fun toggleItemBought(list: ShoppingList, item: ShoppingListItem) {
        viewModelScope.launch {
            val updatedItems = list.items.map {
                if (it == item) it.copy(bought = !it.bought) else it
            }
            dao.update(list.copy(items = updatedItems))
            refreshCurrentLists()
        }
    }

    fun editItemInList(
        list: ShoppingList,
        oldItem: ShoppingListItem,
        newName: String,
        newQuantity: String,
        newDescription: String
    ) {
        viewModelScope.launch {
            val updatedItems = list.items.map {
                if (it == oldItem) it.copy(
                    name = newName,
                    quantity = newQuantity,
                    description = newDescription
                ) else it
            }
            dao.update(list.copy(items = updatedItems))
            refreshCurrentLists()
        }
    }

    fun updateListName(list: ShoppingList, newName: String) {
        viewModelScope.launch {
            dao.update(list.copy(name = newName))
            refreshCurrentLists()
        }
    }

    fun copyListsFromDate(fromDate: String, toDate: String) {
        val email = currentUserEmail ?: return

        viewModelScope.launch {
            val listsToCopy = dao.getByDateAndUserOnce(fromDate, email)
            listsToCopy.forEach { list ->
                val resetItems = list.items.map { it.copy(bought = false) }
                val copiedList = list.copy(
                    id = 0,
                    date = toDate,
                    items = resetItems
                )
                dao.insert(copiedList)
            }
            refreshCurrentLists()
        }
    }

    private fun refreshCurrentLists() {
        val email = currentUserEmail ?: return
        val date = currentDate ?: return
        viewModelScope.launch {
            _shoppingLists.value = dao.getByDateAndUserOnce(date, email)
        }
    }
}
