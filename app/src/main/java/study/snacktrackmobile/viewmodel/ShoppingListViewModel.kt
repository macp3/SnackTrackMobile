package study.snacktrackmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.dao.ShoppingListDao
import study.snacktrackmobile.data.model.ShoppingList
import study.snacktrackmobile.data.model.ShoppingListItem
import study.snacktrackmobile.data.services.AiApiService
import study.snacktrackmobile.data.services.AiShoppingRequest

class ShoppingListViewModel(
    private val dao: ShoppingListDao,
    private val aiService: AiApiService,
    private val getToken: suspend () -> String?
) : ViewModel() {

    private val _shoppingLists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val shoppingLists: StateFlow<List<ShoppingList>> = _shoppingLists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

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
            refreshCurrentLists()
            onAdded(newId)
        }
    }

    fun deleteList(list: ShoppingList) {
        viewModelScope.launch {
            dao.delete(list)
            refreshCurrentLists()
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

    fun generateAiShoppingList(
        userPrompt: String,
        selectedDate: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val email = currentUserEmail ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recentLists = dao.getLastLists(email)
                val productContext = recentLists
                    .flatMap { it.items }
                    .map { it.name }
                    .distinct()
                    .take(50)

                val request = AiShoppingRequest(
                    prompt = userPrompt,
                    productContext = productContext
                )

                val rawToken = getToken()
                if (rawToken == null) {
                    onError("User not logged in (Token missing)")
                    return@launch
                }
                val token = "Bearer $rawToken"

                val generatedItems = aiService.generateShoppingList(token, request)

                val newShoppingListItems = generatedItems.map {
                    ShoppingListItem(
                        name = it.name,
                        quantity = it.quantity,
                        description = it.description ?: "",
                        bought = false
                    )
                }

                val newList = ShoppingList(
                    name = "AI Plan (${generatedItems.size} items)",
                    date = selectedDate,
                    userEmail = email,
                    items = newShoppingListItems
                )
                dao.insert(newList)

                refreshCurrentLists()
                onSuccess()

            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Unknown error occurred")
            } finally {
                _isLoading.value = false
            }
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