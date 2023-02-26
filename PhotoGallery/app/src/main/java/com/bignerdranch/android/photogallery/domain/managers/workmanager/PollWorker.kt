package com.bignerdranch.android.photogallery.domain.managers.workmanager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bignerdranch.android.photogallery.NOTIFICATION_CHANNEL_ID
import com.bignerdranch.android.photogallery.QueryPreferences
import com.bignerdranch.android.photogallery.R
import com.bignerdranch.android.photogallery.domain.api.WebClient
import com.bignerdranch.android.photogallery.domain.models.Photo
import com.bignerdranch.android.photogallery.ui.activity.PhotoGalleryActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "PollWorker"

class PollWorker(val context: Context, workerParams: WorkerParameters)
    : CoroutineWorker(context, workerParams) {

    @SuppressLint("UnspecifiedImmutableFlag")
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val query = QueryPreferences.getStoredQuery(context)
            val lastResultId = QueryPreferences.getLastResultId(context)

            val items: List<Photo> = if (query.isEmpty()) {
                val searchResponse = WebClient.client.fetchImages()
                 searchResponse.photos.photo.map { photo ->
                    Photo(
                        id = photo.id,
                        url = "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}.jpg",
                        title = photo.title
                    )
                }
            } else {
                val searchResponse = WebClient.client.fetchImages(query)
                 searchResponse.photos.photo.map { photo ->
                    Photo(
                        id = photo.id,
                        url = "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}.jpg",
                        title = photo.title
                    )
                }
            }

            if (items.isEmpty()) {
                return@withContext Result.success()
            }
            val resultId = items.first().id
            if (resultId == lastResultId) {
                Log.i(TAG, "Got an old result: $resultId")
            } else {
                Log.i(TAG, "Got a new result: $resultId")
                QueryPreferences.setLastResultId(context, resultId)

                val intent = PhotoGalleryActivity.newIntent(context)
                val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
                val resources = context.resources
                val notification = NotificationCompat
                    .Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(0, notification)
            }
            return@withContext Result.success()
        } catch (error: Throwable) {
            if (runAttemptCount <3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}