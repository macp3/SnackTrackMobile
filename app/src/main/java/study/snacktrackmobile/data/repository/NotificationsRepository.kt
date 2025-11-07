package study.snacktrackmobile.data.repository

import androidx.compose.runtime.mutableStateListOf
import study.snacktrackmobile.data.model.Notification

object NotificationsRepository {
    private val _notifications = mutableStateListOf<Notification>()
    val notifications: List<Notification> get() = _notifications

    fun addNotification(notification: Notification) {
        _notifications.add(0, notification) // nowe na górze
    }

    fun clearNotifications() {
        _notifications.clear()
    }

    fun removeNotification(notification: Notification) {
        _notifications.remove(notification)
    }

}
