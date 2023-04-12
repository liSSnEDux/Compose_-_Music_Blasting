package com.lissnedux.music.blasting.compose.services

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class ActionsService: Service() {

    var serviceBounded = false
    private lateinit var action: String

    private lateinit var mbService: MusicBlasterService

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        action = intent?.getStringExtra("action").toString()

        val serviceIntent = Intent( this, MusicBlasterService::class.java )
        bindService( serviceIntent, connection, Context.BIND_AUTO_CREATE )

        return super.onStartCommand(intent, flags, startId)
    }

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {

            val binder = service as MusicBlasterService.LocalBinder
            mbService = binder.getService()


            when( action ){

                "stop"->mbService.stopMediaPlayer()
                "pause"->mbService.pauseMusic(applicationContext)
                "previous"->mbService.selectPreviousSong( applicationContext )
                "playPause"->mbService.pauseResumeMusic( applicationContext )
                "skip"->mbService.selectNextSong( applicationContext )
            }


            unbindService( this )
        }

        override fun onServiceDisconnected(name: ComponentName?) {

            serviceBounded = false
        }
    }

}