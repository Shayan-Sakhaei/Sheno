package ir.android.sheno.client

import android.app.Activity
import android.content.*
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.view.View
import ir.android.sheno.R
import ir.android.sheno.service.ExoPlayerService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val OPEN_DOCUMENT_REQUEST_CODE = 999
    }

    private lateinit var exoPlayerService: ExoPlayerService
    private var isBound: Boolean = false
    private var selectedFileUri: Uri? = null
    private lateinit var artist: String
    private lateinit var title: String

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            val binder = iBinder as ExoPlayerService.LocalBinder
            exoPlayerService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPlayPause.setOnClickListener {
            if (exoPlayerService.isPlaying()) {
                exoPlayerService.pause()
            } else {
                if (!exoPlayerService.isServiceStarted()) startService()
                setSelectedFileMediaInfo()
                exoPlayerService.loadMedia(selectedFileUri)
                exoPlayerService.play()
            }
        }

        btnSelectSong.setOnClickListener {
            pickFile()
        }
    }

    override fun onStart() {
        super.onStart()
        bindService()
    }

    private fun bindService() {
        val bindIntent = Intent(this, ExoPlayerService::class.java)
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun startService() {
        val startIntent = Intent(this, ExoPlayerService::class.java)
        startIntent.putExtra(ExoPlayerService.KEY_COMMAND, ExoPlayerService.COMMAND_START)
        startService(startIntent)
    }

    private fun pickFile() {
        val intentChooseFile = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intentChooseFile.type = "audio/*"
        startActivityForResult(
            intentChooseFile,
            OPEN_DOCUMENT_REQUEST_CODE
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                OPEN_DOCUMENT_REQUEST_CODE -> {
                    selectedFileUri = intent?.data
                    getSelectedFileMediaInfo(selectedFileUri)
                    btnPlayPause.isEnabled = true
                }
            }
        }
    }

    private fun getSelectedFileMediaInfo(fileUri: Uri?) {
        val mediaMetadataRetriever = MediaMetadataRetriever()
            .apply {
                setDataSource(this@MainActivity, fileUri)
            }
        artist = mediaMetadataRetriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_ARTIST
        )
        title = mediaMetadataRetriever.extractMetadata(
            MediaMetadataRetriever.METADATA_KEY_TITLE
        )
    }

    private fun setSelectedFileMediaInfo() {
        tvArtist.apply {
            text = artist
            visibility = View.VISIBLE
        }
        tvTitle.apply {
            text = title
            visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
        }
    }
}
