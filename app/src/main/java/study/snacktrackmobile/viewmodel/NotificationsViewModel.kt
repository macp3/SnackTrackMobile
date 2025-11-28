package study.snacktrackmobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import study.snacktrackmobile.data.model.Notification

class NotificationsViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    fun addNotification(notification: Notification) {
        viewModelScope.launch {
            _notifications.value = listOf(notification) + _notifications.value
        }
    }

    fun clearNotifications() {
        viewModelScope.launch { _notifications.value = emptyList() }
    }
}
