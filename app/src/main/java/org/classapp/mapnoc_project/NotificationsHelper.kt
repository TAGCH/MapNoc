package org.classapp.mapnoc_project

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "NotificationsHelper"
private const val CHANNEL_ID = "event_notification_channel"
private const val CHANNEL_NAME = "Event Notifications"

class NotificationsHelper(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val notificationManager = NotificationManagerCompat.from(context)

    override fun doWork(): Result {
        getEventsFromFirebase2(
            onSuccess = { events ->
                events.forEach { event ->
                    if (isToday(event.time)) {
                        showNotification(event)
                    }
                }
            },
            onFailure = { exception ->
                Log.e(TAG, "Error getting events from Firestore", exception)
            }
        )
        return Result.success()
    }

    private fun getEventsFromFirebase2(
        onSuccess: (List<Event2>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        db.collection("events").get()
            .addOnSuccessListener { result ->
                val events = result.documents.mapNotNull { document ->
                    val event = document.toObject(Event2::class.java)
                    if (event?.time.isNullOrEmpty()) {
                        Log.e(TAG, "Document ${document.id} has an empty or invalid time field.")
                        null
                    } else {
                        event
                    }
                }
                onSuccess(events)
            }
            .addOnFailureListener { exception: Exception ->
                Log.e(TAG, "Error fetching events: ", exception)
                onFailure(exception)
            }
    }



    private fun isToday(time: String?): Boolean {
        if (time.isNullOrEmpty()) {
            Log.e(TAG, "Invalid date format or empty string provided.")
            return false
        }

        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(Calendar.getInstance().time)
            val eventDate = dateFormat.parse(time)

            currentDate == dateFormat.format(eventDate)
        } catch (e: ParseException) {
            Log.e(TAG, "Error parsing date: $time", e)
            false
        }
    }


    private fun showNotification(event: Event2) {
        createNotificationChannel()

        val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            // .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure this icon exists in your drawable resources.
            .setContentTitle(event.mark_name)
            .setContentText(event.mark_description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions are not granted, you can request them here as commented.
            return
        }
        notificationManager.notify(1, notificationBuilder.build())
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Event Notifications"
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

data class Event2(
    val mark_name: String = "",
    val mark_description: String = "",
    val time: String = "",
    val location: GeoPoint? = null
)



