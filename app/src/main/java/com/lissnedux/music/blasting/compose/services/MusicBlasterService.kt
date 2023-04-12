package com.lissnedux.music.blasting.compose.services

import android.app.*
import android.appwidget.AppWidgetManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.*
import android.media.AudioManager.OnAudioFocusChangeListener
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import com.lissnedux.music.blasting.compose.R
import com.lissnedux.music.blasting.compose.data.data_classes.Song
import com.lissnedux.music.blasting.compose.activities.main.MainActivity
import com.lissnedux.music.blasting.compose.functions.getSongAlbumArt
import com.lissnedux.music.blasting.compose.widgets.MusicBlasterWidget


@Suppress("DEPRECATION")
class MusicBlasterService : Service() {

    private val mBinder = LocalBinder()
    private lateinit var notification: Notification
    private lateinit var notificationManager: NotificationManager
    private var queueList = ArrayList<Song>()
    private var shuffledQueueList = ArrayList<Song>()
    var currentSongPosition: Int = 0
    var songOnRepeat = false
    private lateinit var audioManager: AudioManager
    val mediaPlayer = MediaPlayer()


    //Listeners
    var onSongSelected: (song: Song) -> Unit = {}
    var onSongSelectedForPlayer: (song: Song) -> Unit = {}
    var onSongSelectedForWidget: (song: Song) -> Unit = {}
    var onPause: () -> Unit = {}
    var onResume: () -> Unit = {}
    var onSecondPassed: () -> Unit = {}
    var onStop: () -> Unit = {}
    var onQueueShuffle: () -> Unit = {}
    var onSongRepeat: () -> Unit = {}


    var currentSong: Song? = null

    //Player States
    private var serviceStarted = false
    var queueShuffled = false
    private var musicStarted = false


    //Others
    private lateinit var mediaButtonReceiver: ComponentName
    private lateinit var mediaSession: MediaSessionCompat


    inner class LocalBinder : Binder() {
        fun getService(): MusicBlasterService = this@MusicBlasterService
    }


    override fun onBind(intent: Intent?): IBinder {

        val context = this

        mediaButtonReceiver = ComponentName(context, ReceiverPlayPause::class.java)

        mediaSession = MediaSessionCompat(context, "MusicBlasterSession").apply {
            setCallback(object : MediaSessionCompat.Callback() {

                override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {

                    val ke = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

                    if (ke?.action == KeyEvent.ACTION_DOWN) {

                        if (ke.keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                            selectPreviousSong(context)

                        if (ke.keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE)
                            pauseResumeMusic(context)

                        if (ke.keyCode == KeyEvent.KEYCODE_MEDIA_PLAY)
                            pauseResumeMusic(context)

                        if (ke.keyCode == KeyEvent.KEYCODE_MEDIA_NEXT)
                            selectNextSong(context)
                    }

                    return super.onMediaButtonEvent(mediaButtonIntent)
                }

                override fun onPlay() {
                    super.onPlay()

                    pauseResumeMusic(context)
                }

                override fun onStop() {
                    super.onStop()

                    stopMediaPlayer()
                }

                override fun onPause() {
                    super.onPause()

                    pauseResumeMusic(context)
                }

                override fun onSkipToNext() {
                    super.onSkipToNext()

                    selectNextSong(context)
                }

                override fun onSkipToPrevious() {
                    super.onSkipToPrevious()

                    selectPreviousSong(context)
                }
            })
        }


        return mBinder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }


    override fun onCreate() {
        super.onCreate()

        notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }


    fun getQueue(): ArrayList<Song> {

        return if (!queueShuffled) queueList else shuffledQueueList
    }

    fun getUpNextQueue(): ArrayList<Song> {

        val currentQueueList = getQueue()
        val upNextQueueList = ArrayList<Song>()

        currentQueueList.forEachIndexed { index, song ->
            if (index > currentSongPosition)
                upNextQueueList.add(song)
        }

        return upNextQueueList
    }


    fun isMusicPlayingOrPaused(): Boolean {
        return musicStarted
    }

    fun selectSong(context: Context, newQueueList: List<Song>, position: Int) {

        queueList = ArrayList(newQueueList)

        if (queueShuffled)
            playAndShuffle(context = context, position = position)
        else {

            currentSongPosition = position
            playSong(context = context)
        }
    }


    fun toggleShuffle() {

        if (!queueShuffled) {

            queueShuffled = true

            shuffledQueueList = ArrayList()

            val tempShuffledPlaylist = ArrayList<Song>()

            //Adds the current song to first position
            queueList.forEach { song ->

                if (song.path != currentSong!!.path)
                    tempShuffledPlaylist.add(song)
                else
                    shuffledQueueList.add(song)
            }

            //Shuffles the temp playlist and adds it to the one with just the current song
            tempShuffledPlaylist.shuffle()

            for (song in tempShuffledPlaylist)
                shuffledQueueList.add(song)

            currentSongPosition = 0

        } else {

            queueShuffled = false

            for (i in queueList.indices) {

                if (queueList[i].path == currentSong!!.path) {

                    currentSongPosition = i
                    break
                }
            }
        }

        onQueueShuffle()
    }


    private fun playAndShuffle(context: Context, position: Int) {

        shuffledQueueList = ArrayList()

        val tempShuffledQueueList = ArrayList(queueList)

        val firstSong = queueList[position]

        tempShuffledQueueList.removeAt(position)

        tempShuffledQueueList.shuffle()

        shuffledQueueList.add(firstSong)

        tempShuffledQueueList.forEach { song ->
            shuffledQueueList.add(song)
        }

        currentSongPosition = 0

        onQueueShuffle()

        playSong(context)
    }

    fun shuffleAndPlay(newQueueList: List<Song>, context: Context) {

        queueList = newQueueList as ArrayList<Song>

        queueShuffled = true

        shuffledQueueList = ArrayList(queueList)

        shuffledQueueList.shuffle()

        currentSongPosition = 0

        onQueueShuffle()

        playSong(context)
    }


    fun musicPlaying(): Boolean {

        return try {
            mediaPlayer.isPlaying
        } catch (_: Exception) {
            false
        }
    }


    private val audioFocusChangeListener = OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {}

            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {}

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {

                if (mediaPlayer.isPlaying)
                    pauseMusic(this)
            }

            AudioManager.AUDIOFOCUS_LOSS -> {

                if (mediaPlayer.isPlaying)
                    pauseMusic(this)
            }
        }
    }


    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
        setAudioAttributes(AudioAttributes.Builder().run {
            setUsage(AudioAttributes.USAGE_MEDIA)
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            build()
        })
        setAcceptsDelayedFocusGain(true)
        setOnAudioFocusChangeListener(audioFocusChangeListener)
        build()
    }


    private fun playSong(context: Context) {

        serviceStarted = true
        musicStarted = true

        val songTitle: String
        val songArtist: String
        val songID: Long
        val songAlbumID: Long
        var songAlbumArt: Bitmap?
        val songDuration: Int


        if (!queueShuffled) {

            songTitle = queueList[currentSongPosition].title
            songArtist = queueList[currentSongPosition].artist
            songID = queueList[currentSongPosition].id
            songAlbumID = queueList[currentSongPosition].albumID
            songAlbumArt = getSongAlbumArt(context, songID, songAlbumID)
            songDuration = queueList[currentSongPosition].duration
        } else {

            songTitle = shuffledQueueList[currentSongPosition].title
            songArtist = shuffledQueueList[currentSongPosition].artist
            songID = shuffledQueueList[currentSongPosition].id
            songAlbumID = shuffledQueueList[currentSongPosition].albumID
            songAlbumArt = getSongAlbumArt(context, songID, songAlbumID)
            songDuration = shuffledQueueList[currentSongPosition].duration
        }

        if (songAlbumArt == null) {
            songAlbumArt = BitmapFactory.decodeResource(context.resources, R.drawable.no_album_art)
        }

        currentSong = getQueue()[currentSongPosition]

        mediaPlayer.reset()
        mediaPlayer.setDataSource(currentSong!!.path)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {

            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

            requestPlayWithFocus()

            //Open App
            val openAppIntent = Intent(context, MainActivity::class.java)
            val pendingOpenAppIntent = TaskStackBuilder.create(context).run {

                addNextIntentWithParentStack(openAppIntent)
                getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
            }

            //Stop Service
            val stopIntent = Intent(context, ReceiverStop::class.java)
            val pendingStopIntent = PendingIntent.getBroadcast(context, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)


            //Previous Music
            val previousSongIntent = Intent(context, ReceiverPreviousSong::class.java)
            val pendingPreviousSongIntent = PendingIntent.getBroadcast(context, 1, previousSongIntent, PendingIntent.FLAG_IMMUTABLE)


            //Pauses/Plays music
            val playPauseIntent = Intent(context, ReceiverPlayPause::class.java)
            val pendingPlayPauseIntent = PendingIntent.getBroadcast(context, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)


            //Skips to next music
            val skipSongIntent = Intent(context, ReceiverSkipSong::class.java)
            val pendingSkipSongIntent = PendingIntent.getBroadcast(context, 1, skipSongIntent, PendingIntent.FLAG_IMMUTABLE)



            notification = NotificationCompat.Builder(context, "Playback")
                .setContentIntent(pendingOpenAppIntent)
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0, 1, 2, 3)
                )
                .setSmallIcon(R.drawable.icon)
                .addAction(R.drawable.icon_x_solid, "Stop Player", pendingStopIntent)
                .addAction(R.drawable.icon_previous_notification, "Previous Music", pendingPreviousSongIntent)
                .addAction(R.drawable.icon_pause_notification, "Play Pause Music", pendingPlayPauseIntent)
                .addAction(R.drawable.icon_next_notification, "Next Music", pendingSkipSongIntent)
                .build()


            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()

                    .putString(MediaMetadata.METADATA_KEY_TITLE, songTitle)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, songArtist)
                    .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, songAlbumArt)
                    .putLong(MediaMetadata.METADATA_KEY_DURATION, songDuration.toLong())
                    .build()
            )


            startForeground(2, notification)
            notificationManager.notify(2, notification)
        }


        handleSongFinished(context)


        onSongSelected(currentSong!!)
        onSongSelectedForPlayer(currentSong!!)
        onSongSelectedForWidget(currentSong!!)


        val bluetoothReceiver = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        context.registerReceiver(bluetoothBroadcastReceiver, bluetoothReceiver)


        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {

                if (musicPlaying()) {

                    onSecondPassed()
                    setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }
                mainHandler.postDelayed(this, 1000)
            }
        })
    }

    fun setPlaybackState(state: Int) {

        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY_PAUSE
                        or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            )
            .apply {
                setState(state, mediaPlayer.currentPosition.toLong(), 1.0f)
            }

        mediaSession.setPlaybackState(stateBuilder.build())
    }


    private val bluetoothBroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(p0: Context?, p1: Intent?) {

            if (musicPlaying()) pauseMusic(p0!!)
        }
    }


    fun seekTo(seconds: Int) {

        val newSongPosition = seconds * 1000

        mediaPlayer.seekTo(newSongPosition)

        if (!mediaPlayer.isPlaying) mediaPlayer.start()
    }


    private fun handleSongFinished(context: Context) {

        mediaPlayer.setOnCompletionListener {

            //If loop mode is activated
            if (songOnRepeat) {

                playSong(context)
            }

            //Is it's the last song
            else if ((currentSongPosition + 1) == queueList.size) {

                stopMediaPlayer()
            } else {

                currentSongPosition++

                playSong(context)
            }
        }
    }


    fun toggleRepeat() {

        songOnRepeat = !songOnRepeat

        onSongRepeat()
    }


    fun stopMediaPlayer() {

        onStop()
        mediaPlayer.stop()
        musicStarted = false
        currentSongPosition = -1

        setPlaybackState(PlaybackStateCompat.STATE_STOPPED)

        val intent = Intent(application, MusicBlasterWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE

        val ids = AppWidgetManager.getInstance(application)
            .getAppWidgetIds(ComponentName(application, MusicBlasterWidget::class.java))

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        application.sendBroadcast(intent)

        stopForeground(true)
    }


    fun selectNextSong(context: Context) {

        if ((currentSongPosition + 1) < queueList.size) {

            currentSongPosition++
            playSong(context)
        }
    }


    fun selectPreviousSong(context: Context) {

        if ((currentSongPosition - 1) >= 0) {

            currentSongPosition--
            playSong(context)
        }

    }


    @Suppress("DEPRECATION")
    fun pauseMusic(context: Context) {


        val playPauseIcon = R.drawable.icon_play_notification
        mediaPlayer.pause()
        mediaSession.isActive = false
        onPause()

        setPlaybackState(PlaybackStateCompat.STATE_PAUSED)

        //Updates the notification
        val playPauseIntent = Intent(context, ReceiverPlayPause::class.java)
        playPauseIntent.putExtra("action", "playPause")
        val pendingPlayPauseIntent = PendingIntent.getBroadcast(context, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)


        notification.actions[2] = Notification.Action(playPauseIcon, "Play Music", pendingPlayPauseIntent)


        startForeground(2, notification)
        notificationManager.notify(2, notification)
    }


    @Suppress("DEPRECATION")
    fun pauseResumeMusic(context: Context) {

        val playPauseIcon: Int

        if (mediaPlayer.isPlaying) {

            playPauseIcon = R.drawable.icon_play_notification

            mediaPlayer.pause()
            setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
            onPause()
        } else {

            playPauseIcon = R.drawable.icon_pause_notification

            requestPlayWithFocus()
            setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
            onResume()
        }


        //Updates the notification
        val playPauseIntent = Intent(context, ReceiverPlayPause::class.java)
        playPauseIntent.putExtra("action", "playPause")
        val pendingPlayPauseIntent = PendingIntent.getBroadcast(context, 1, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)


        notification.actions[2] = Notification.Action(playPauseIcon, "Play Music", pendingPlayPauseIntent)


        startForeground(2, notification)
        notificationManager.notify(2, notification)
    }


    private fun requestPlayWithFocus() {

        val focusLock = Any()
        val res = audioManager.requestAudioFocus(focusRequest)


        synchronized(focusLock) {
            when (res) {
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {

                    mediaPlayer.start()
                    onResume()

                    mediaSession.isActive = true

                    true
                }
                else -> false
            }
        }
    }
}