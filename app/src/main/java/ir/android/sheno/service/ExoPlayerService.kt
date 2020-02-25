package ir.android.sheno.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import ir.android.sheno.utils.NotificationHandler


class ExoPlayerService : Service() {

    companion object {
        const val KEY_COMMAND = "command"
        const val COMMAND_START = "start"
        const val COMMAND_PLAY = "play"
        const val COMMAND_PAUSE = "pause"
        const val COMMAND_STOP = "stop"
        const val START_SERVICE_REQUEST_CODE = 111
        const val NOTIFICATION_TO_SERVICE_PLAY_REQUEST_CODE = 123
        const val NOTIFICATION_TO_SERVICE_PAUSE_REQUEST_CODE = 234
        const val NOTIFICATION_TO_SERVICE_STOP_REQUEST_CODE = 345
        const val START_ACTIVITY_REQUEST_CODE = 555
    }

    private val mBinder: IBinder = LocalBinder()
    private lateinit var mExoPlayer: SimpleExoPlayer
    private var mServiceStarted = false


    override fun onCreate() {
        super.onCreate()
        mExoPlayer = SimpleExoPlayer.Builder(this).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.getStringExtra(KEY_COMMAND)) {
            COMMAND_START -> {
                if (!mServiceStarted) {
                    NotificationHandler.createNotification(this)
                    mServiceStarted = true
                }
            }
            COMMAND_STOP -> {
                commandStop()
            }
            COMMAND_PLAY -> {
                play()
            }
            COMMAND_PAUSE -> {
                pause()
            }
        }
        return START_NOT_STICKY
    }

    inner class LocalBinder : Binder() {
        fun getService(): ExoPlayerService {
            return this@ExoPlayerService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

    private fun commandStop() {
        pause()
        mExoPlayer.release()
        stopForeground(true)
        mServiceStarted = false
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        commandStop()
    }

    fun isServiceStarted(): Boolean {
        return mServiceStarted
    }

    fun loadMedia(fileUri: Uri?) {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            this, Util.getUserAgent(this, "Sheno")
        )
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(fileUri)
        mExoPlayer.prepare(mediaSource)
    }

    fun isPlaying(): Boolean {
        return mExoPlayer.isPlaying
    }

    fun play() {
        mExoPlayer.playWhenReady = true
    }

    fun pause() {
        mExoPlayer.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        mExoPlayer.release()
    }
}