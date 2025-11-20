package study.snacktrackmobile.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import study.snacktrackmobile.data.dao.ShoppingListDao
import study.snacktrackmobile.data.services.AiApiService
import study.snacktrackmobile.data.storage.TokenStorage

class ShoppingListViewModelFactory(
    private val context: Context,
    private val dao: ShoppingListDao,
    private val aiService: AiApiService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShoppingListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShoppingListViewModel(
                dao = dao,
                aiService = aiService,
                getToken = { TokenStorage.getToken(context.applicationContext) }
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}