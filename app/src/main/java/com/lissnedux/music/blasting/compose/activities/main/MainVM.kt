package com.lissnedux.music.blasting.compose.activities.main

import android.annotation.SuppressLint
import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.*
import android.graphics.Bitmap
import android.os.IBinder
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import com.lissnedux.music.blasting.compose.*
import com.lissnedux.music.blasting.compose.data.data_classes.Song
import com.lissnedux.music.blasting.compose.data.data_classes.SongArt
import com.lissnedux.music.blasting.compose.functions.getAllAlbumsImages
import com.lissnedux.music.blasting.compose.functions.getSongs
import com.lissnedux.music.blasting.compose.services.MusicBlastingService
import com.lissnedux.music.blasting.compose.widgets.MusicBlastingWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

class MainVM(application: Application) : AndroidViewModel(application) {


    //************************************************
    // Variables
    //************************************************

    private val _navBarHeight = MutableStateFlow(55.dp)
    val navBarHeight = _navBarHeight.asStateFlow()
    fun updateNavbarHeight(newValue: Dp) {
        _navBarHeight.update { newValue }
    }


    private val context = application

    @SuppressLint("StaticFieldLeak")
    private var mbService: MusicBlasterService? = null

    private val _surfaceColor = MutableStateFlow(Color(0xFF000000))
    val surfaceColor = _surfaceColor.asStateFlow()
    fun updateSurfaceColor(newValue: Color) {
        _surfaceColor.update { newValue }
    }

    private val _showNavigationBar = MutableStateFlow(true)
    val showNavigationBar = _showNavigationBar.asStateFlow()
    fun updateShowNavigationBar(newValue: Boolean) {

        mbService?.let { mb ->

            if (mb.isMusicPlayingOrPaused() && newValue) {
                _miniPlayerHeight.update { 60.dp }
            } else {
                _miniPlayerHeight.update { 0.dp }
            }
        }

        _showNavigationBar.update { newValue }
    }

    private val _songs = MutableStateFlow<List<Song>?>(null)
    val songs = _songs.asStateFlow()

    private val _songsImages = MutableStateFlow<List<SongArt>?>(null)
    val songsImages = _songsImages.asStateFlow()

    private val _compressedSongsImages = MutableStateFlow<List<SongArt>?>(null)
    val compressedSongsImages = _compressedSongsImages.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>?>(null)
    val queue = _queue.asStateFlow()

    private val _upNextQueue = MutableStateFlow<List<Song>?>(null)
    val upNextQueue = _upNextQueue.asStateFlow()

    private val _selectedSong = MutableStateFlow<Song?>(null)
    val selectedSong = _selectedSong.asStateFlow()

    private val _songAlbumArt = MutableStateFlow<Bitmap?>(null)
    val songAlbumArt = _songAlbumArt.asStateFlow()

    private val _songMinutesAndSecondsText = MutableStateFlow("")
    val songMinutesAndSecondsText = _songMinutesAndSecondsText.asStateFlow()

    private val _currentSongMinutesAndSecondsText = MutableStateFlow("")
    val currentSongMinutesAndSecondsText = _currentSongMinutesAndSecondsText.asStateFlow()
    fun updateCurrentSongMinutesAndSecondsText(newValue: String) {
        _currentSongMinutesAndSecondsText.update { newValue }
    }

    private val _musicPlaying = MutableStateFlow(false)
    val musicPlayling = _musicPlaying.asStateFlow()

    private val _queueShuffled = MutableStateFlow(false)
    val queueShuffled = _queueShuffled.asStateFlow()

    private val _songOnRepeat = MutableStateFlow(false)
    val songOnRepeat = _songOnRepeat.asStateFlow()

    private val _songPosition = MutableStateFlow(0)
    val songPosition = _songPosition.asStateFlow()

    private val _songSeconds = MutableStateFlow(0f)
    val songSeconds = _songSeconds.asStateFlow()

    private val _miniPlayerHeight = MutableStateFlow(0.dp)
    val miniPlayerHeight = _miniPlayerHeight.asStateFlow()


    //************************************************
    // Callbacks
    //************************************************


    var onSongSelected: () -> Unit = {}


    //************************************************
    // Functions
    //************************************************

    private val MusicBlasterConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val binder = service as MusicBlasterService.LocalBinder
            mbService = binder.getService()

            mbService?.let { mb ->

                _musicPlaying.update { mb.musicPlaying() }

                _queueShuffled.update { mb.queueShuffled }

                _songOnRepeat.update { mb.songOnRepeat }


                if (mb.isMusicPlayingOrPaused()) {

                    _selectedSong.update { mb.currentSong }

                    _songAlbumArt.update { getAlbumArt() }

                    _songSeconds.update { (mb.mediaPlayer.currentPosition / 1000).toFloat() }

                    _songMinutesAndSecondsText.update { getMinutesAndSeconds(selectedSong.value!!.duration / 1000) }

                    _currentSongMinutesAndSecondsText.update { getMinutesAndSeconds(mb.mediaPlayer.currentPosition / 1000) }

                    _miniPlayerHeight.update { 60.dp }

                    _queue.update { mb.getQueue() }

                    _upNextQueue.update { mb.getUpNextQueue() }

                    _songPosition.update { mb.currentSongPosition }

                    onSongSelected()
                }


                mb.onSongSelected = { song ->

                    _selectedSong.update { song }

                    _songSeconds.update { (mb.mediaPlayer.currentPosition / 1000).toFloat() }

                    _songMinutesAndSecondsText.update { getMinutesAndSeconds(selectedSong.value!!.duration / 1000) }

                    _songAlbumArt.update { getAlbumArt() }

                    _songOnRepeat.update { mb.songOnRepeat }

                    _miniPlayerHeight.update { 60.dp }

                    _queue.update { mb.getQueue() }

                    _upNextQueue.update { mb.getUpNextQueue() }

                    _songPosition.update { mb.currentSongPosition }

                    onSongSelected()

                    updateWidget()
                }


                mb.onSecondPassed = {

                    _songSeconds.update { (mb.mediaPlayer.currentPosition / 1000).toFloat() }
                }

                mb.onQueueShuffle = {

                    _queueShuffled.update { mb.queueShuffled }

                    _queue.update { mb.getQueue() }

                    _upNextQueue.update { mb.getUpNextQueue() }

                    _songPosition.update { mb.currentSongPosition }
                }


                mb.onPause = {

                    _musicPlaying.update { mb.musicPlaying() }

                    updateWidget()
                }


                mb.onResume = {

                    _musicPlaying.update { mb.musicPlaying() }

                    _songPosition.update { mb.currentSongPosition }

                    updateWidget()
                }

                mb.onSongRepeat = {

                    _songOnRepeat.update { mb.songOnRepeat }
                }

                mb.onStop = {

                    _selectedSong.update { null }

                    _miniPlayerHeight.update { 0.dp }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            Log.d("Service Disconnection", "Service was disconnected")
        }
    }

    private fun updateWidget() {
        val intent = Intent(context, MusicBlasterWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }

        val ids = AppWidgetManager.getInstance(context)
            .getAppWidgetIds(ComponentName(context, MusicBlasterWidget::class.java))

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)

        context.sendBroadcast(intent)
    }

    fun getMinutesAndSeconds(receivedSeconds: Int): String {

        val minutes = ((receivedSeconds * 1000) / (1000 * 60)) % 60

        val seconds = receivedSeconds % 60

        var stringSeconds = "$seconds"

        if (seconds < 10) {

            stringSeconds = "0$seconds"
        }

        return "$minutes:$stringSeconds"
    }

    private fun getAlbumArt(compressed: Boolean = false): Bitmap? {

        return when (compressed) {

            true -> {
                compressedSongsImages.value?.first { it.albumID == selectedSong.value?.albumID }?.albumArt
            }
            false -> {
                songsImages.value?.first { it.albumID == selectedSong.value?.albumID }?.albumArt
            }
        }
    }

    //************************************************
    // Playback Function
    //************************************************

    fun selectSong(newQueueList: List<Song>, position: Int) {

        if (newQueueList.isNotEmpty()) {

            mbService?.let { mb ->

                mb.selectSong(getApplication(), newQueueList, position)

                _selectedSong.update { mb.currentSong }

                _songMinutesAndSecondsText.update { getMinutesAndSeconds(selectedSong.value!!.duration / 1000) }

                _songAlbumArt.update { getAlbumArt() }

                _queue.update { mb.getQueue() }

                _upNextQueue.update { mb.getUpNextQueue() }
            }
        }
    }

    fun shuffleAndPlay(newQueueList: List<Song>) {

        mbService?.shuffleAndPlay(newQueueList, context)
    }

    fun unshuffleAndPlay(newQueueList: List<Song>, position: Int) {

        mbService?.let { mb ->

            if (mb.queueShuffled) {

                mb.toggleShuffle()
            }

            mb.selectSong(context, newQueueList, position)
        }
    }

    fun shuffle(newQueueList: List<Song>) {

        mbService?.shuffleAndPlay(newQueueList, getApplication())
    }


    fun seekSongSeconds(seconds: Int) {

        mb?.let { mb ->

            mb.seekTo(seconds)

            _musicPlaying.update { mb.musicPlaying() }
        }
    }

    fun toggleShuffle() {

        mbService?.let { mb ->

            mb.toggleShuffle()

            _musicPlaying.update { mb.musicPlaying() }

            _queueShuffled.update { mb.queueShuffled }

            _queue.update { mb.getQueue() }

            _upNextQueue.update { mb.getUpNextQueue() }

            _songPosition.update { mb.currentSongPosition }
        }
    }

    fun selectPreviousSong() {

        mbService?.selectPreviousSong(context)
    }

    fun selectNextSong() {

        mbService?.selectNextSong(context)
    }

    fun toggleRepeat() {

        mbService?.let { mb ->

            mb.toggleRepeat()

            _songOnRepeat.update { mb.songOnRepeat }
        }
    }

    fun pauseResumeMusic() {

        mb?.pauseResumeMusic(context)
    }


    init {

        val serviceIntent = Intent(context, MusicBlasterService::class.java)

        context.bindService(serviceIntent, MusicBlasterConnection, Context.BIND_AUTO_CREATE)

        _songs.update { getSongs(context, "Recent") }

        _songsImages.update { getAllAlbumsImages(context) }

        _compressedSongsImages.update { getAllAlbumsImages(context, compressed = true) }
    }
}