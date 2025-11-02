package study.snacktrackmobile.repository

import androidx.compose.runtime.mutableStateListOf
import study.snacktrackmobile.model.NotificationItem

object NotificationsRepository {
    private val _notifications = mutableStateListOf<NotificationItem>()
    val notifications: List<NotificationItem> get() = _notifications

    fun addNotification(notification: NotificationItem) {
        _notifications.add(0, notification) // nowe na górze
    }

    fun clearNotifications() {
        _notifications.clear()
    }
}
