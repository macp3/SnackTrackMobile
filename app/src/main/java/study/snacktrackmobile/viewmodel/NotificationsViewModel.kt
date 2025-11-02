package study.snacktrackmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import study.snacktrackmobile.model.NotificationItem

class NotificationsViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    // Dodanie nowego powiadomienia
    fun addNotification(notification: NotificationItem) {
        viewModelScope.launch {
            _notifications.value = listOf(notification) + _notifications.value
        }
    }

    // Możesz też dodać funkcję czyszczenia powiadomień
    fun clearNotifications() {
        viewModelScope.launch { _notifications.value = emptyList() }
    }
}
