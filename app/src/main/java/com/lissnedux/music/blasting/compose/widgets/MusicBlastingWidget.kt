package com.lissnedux.music.blasting.compose.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.lissnedux.music.blasting.compose.R
import com.lissnedux.music.blasting.compose.activities.main.MainActivity
import com.lissnedux.music.blasting.compose.functions.getSongAlbumArt
import com.lissnedux.music.blasting.compose.services.MusicBlasterService

class MusicBlasterWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        println("Action => ${intent?.action}")

        when(intent?.action){

            "openActivity"->{
                val newIntent = Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP}
                context!!.applicationContext!!.startActivity(newIntent)
            }

            "previousSong"->{
                getMusicBlasterService(
                    context = context!!,
                    onConnect = { mbService ->  mbService.selectPreviousSong(context.applicationContext)}
                )
            }

            "playPause"->{
                getMusicBlasterService(
                    context = context!!,
                    onConnect = { mbService ->  mbService.pauseResumeMusic(context.applicationContext)}
                )
            }

            "nextSong"->{
                getMusicBlasterService(
                    context = context!!,
                    onConnect = { mbService ->  mbService.selectNextSong(context.applicationContext)}
                )
            }
        }
    }
}



internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {


    val openActivityIntent = Intent(context, MusicBlasterWidget::class.java).apply { action = "openActivity" }
    val previousSongIntent = Intent(context, MusicBlasterWidget::class.java).apply { action = "previousSong" }
    val playPauseIntent = Intent(context, MusicBlasterWidget::class.java).apply { action = "playPause" }
    val nextSongIntent = Intent(context, MusicBlasterWidget::class.java).apply { action = "nextSong" }

    val openActivityPendingIntent = PendingIntent.getBroadcast(context, 0 , openActivityIntent, PendingIntent.FLAG_IMMUTABLE)
    val previousSongPendingIntent = PendingIntent.getBroadcast(context, 0 , previousSongIntent, PendingIntent.FLAG_IMMUTABLE)
    val playPausePendingIntent = PendingIntent.getBroadcast(context, 0 , playPauseIntent, PendingIntent.FLAG_IMMUTABLE)
    val nextSongPendingIntent = PendingIntent.getBroadcast(context, 0 , nextSongIntent, PendingIntent.FLAG_IMMUTABLE)


    getMusicBlasterService(
        context = context,
        onConnect = { mbService ->

            if(mbService.isMusicPlayingOrPaused()){

                val views = RemoteViews(context.packageName, R.layout.music_blasting_widget)

                val song = mbService.currentSong!!
                views.setTextViewText(R.id.title_Widget, song.title)
                views.setTextViewText(R.id.artist_Widget, song.artist)
                views.setImageViewBitmap(R.id.albumArt_Widget, getSongAlbumArt(context, song.id, song.albumID))

                if(mbService.musicPlaying())
                    views.setImageViewBitmap(R.id.playPauseButton_Widget, ResourcesCompat.getDrawable(context.resources, R.drawable.icon_pause_notification, null)?.toBitmap())
                else
                    views.setImageViewBitmap(R.id.playPauseButton_Widget, ResourcesCompat.getDrawable(context.resources, R.drawable.icon_play_notification, null)?.toBitmap())


                views.setOnClickPendingIntent(R.id.albumArt_Widget, openActivityPendingIntent)
                views.setOnClickPendingIntent(R.id.previousSongButton_Widget, previousSongPendingIntent)
                views.setOnClickPendingIntent(R.id.playPauseButton_Widget, playPausePendingIntent)
                views.setOnClickPendingIntent(R.id.nextSongButton_Widget, nextSongPendingIntent)


                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
            else{

                val views = RemoteViews(context.packageName, R.layout.music_blasting_disabled_widget)
                views.setOnClickPendingIntent(R.id.noMusicPlaying_Widget, openActivityPendingIntent)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    )
}


fun getMusicBlasterService(
    context: Context,
    onConnect: (mbService: MusicBlasterService) -> Unit ={},
    onDisconnect: () -> Unit = {}
){

    val MusicBlasterConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val binder = service as MusicBlasterService.LocalBinder
            val mbService = binder.getService()

            onConnect(mbService)
        }


        override fun onServiceDisconnected(name: ComponentName?) {onDisconnect()}
    }


    val serviceIntent = Intent(context, MusicBlasterService::class.java)
    context.applicationContext.bindService(serviceIntent, MusicBlasterConnection, Context.BIND_AUTO_CREATE)
}