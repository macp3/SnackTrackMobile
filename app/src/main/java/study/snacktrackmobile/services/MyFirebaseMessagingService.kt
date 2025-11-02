package study.snacktrackmobile.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import study.snacktrackmobile.model.NotificationItem
import study.snacktrackmobile.repository.NotificationsRepository

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "SnackTrack"
        val body = remoteMessage.notification?.body ?: ""

        // Wyświetlenie lokalnej notyfikacji
        showNotification(title, body)

        // Dodanie do dynamicznej listy (np. przez singleton)
        NotificationsRepository.addNotification(
            NotificationItem(
                id = System.currentTimeMillis().toString(),
                title = title,
                body = body
            )
        )
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "snacktrack_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SnackTrack", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
