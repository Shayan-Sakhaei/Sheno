package ir.android.sheno.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import ir.android.sheno.client.MainActivity
import ir.android.sheno.R
import ir.android.sheno.service.ExoPlayerService

object NotificationHandler {

    fun createNotification(service: Service) {

        val startMainActivityIntent = Intent(service, MainActivity::class.java)
        val mainActivityPendingIntent = PendingIntent
            .getActivity(
                service, ExoPlayerService.START_ACTIVITY_REQUEST_CODE,
                startMainActivityIntent, 0
            )

        val playServiceIntent = Intent(service, ExoPlayerService::class.java)
        playServiceIntent.putExtra(ExoPlayerService.KEY_COMMAND, ExoPlayerService.COMMAND_PLAY)
        val playServicePendingIntent = PendingIntent
            .getService(service, ExoPlayerService.NOTIFICATION_TO_SERVICE_PLAY_REQUEST_CODE,
                playServiceIntent, 0)

        val pauseServiceIntent = Intent(service, ExoPlayerService::class.java)
        pauseServiceIntent.putExtra(ExoPlayerService.KEY_COMMAND, ExoPlayerService.COMMAND_PAUSE)
        val pauseServicePendingIntent = PendingIntent
            .getService(service, ExoPlayerService.NOTIFICATION_TO_SERVICE_PAUSE_REQUEST_CODE,
                pauseServiceIntent, 0)

        val stopServiceIntent = Intent(service, ExoPlayerService::class.java)
        stopServiceIntent.putExtra(ExoPlayerService.KEY_COMMAND, ExoPlayerService.COMMAND_STOP)
        val stopServicePendingIntent = PendingIntent
            .getService(service, ExoPlayerService.NOTIFICATION_TO_SERVICE_STOP_REQUEST_CODE,
                stopServiceIntent, 0)



        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            val playActionPreOreo = NotificationCompat.Action.Builder(
                R.drawable.ic_play_24, "Play", playServicePendingIntent
            ).build()

            val pauseActionPreOreo = NotificationCompat.Action.Builder(
                R.drawable.ic_pause_24, "Pause", pauseServicePendingIntent
            ).build()

            val stopActionPreOreo = NotificationCompat.Action.Builder(
                R.drawable.ic_stop_24, "Stop", stopServicePendingIntent
            ).build()

            val notification = NotificationCompat.Builder(service)
                .setContentTitle("Sheno")
                .setContentText("Sheno in playing now...")
                .setSmallIcon(R.drawable.ic_phonograph_24)
                .setContentIntent(mainActivityPendingIntent)
                .addAction(playActionPreOreo)
                .addAction(pauseActionPreOreo)
                .addAction(stopActionPreOreo)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0,1,2)
                )
                .build()

            service.startForeground(ExoPlayerService.START_SERVICE_REQUEST_CODE, notification)

        } else {

            val playActionOreo = Notification.Action.Builder(
                R.drawable.ic_play_24, "Play", playServicePendingIntent
            ).build()
            val pauseActionOreo = Notification.Action.Builder(
                R.drawable.ic_pause_24, "Pause", pauseServicePendingIntent
            ).build()
            val stopActionOreo = Notification.Action.Builder(
                R.drawable.ic_stop_24, "Stop", stopServicePendingIntent
            ).build()

            val channelId = createChannel(service)
            val notification = Notification.Builder(service, channelId)
                .setContentTitle("Sheno")
                .setContentText("Sheno is playing now...")
                .setSmallIcon(R.drawable.ic_phonograph_24)
                .setContentIntent(mainActivityPendingIntent)
                .addAction(playActionOreo)
                .addAction(pauseActionOreo)
                .addAction(stopActionOreo)
                .setStyle(Notification.MediaStyle().setShowActionsInCompactView(0, 1, 2))
                .build()

            service.startForeground(ExoPlayerService.START_SERVICE_REQUEST_CODE, notification)

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(service: Service): String {
        val channelId = "ExoPlayerChannelId"
        val notificationManager = service
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelName = "Playback Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        notificationManager.createNotificationChannel(notificationChannel)
        return channelId
    }
}