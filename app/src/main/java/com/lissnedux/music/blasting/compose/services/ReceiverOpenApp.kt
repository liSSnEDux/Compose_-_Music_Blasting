package com.lissnedux.music.blasting.compose.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.lissnedux.music.blasting.compose.activities.main.MainActivity

class ReceiverOpenApp: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {

        val newIntent = Intent( context, MainActivity::class.java ).addFlags( FLAG_ACTIVITY_NEW_TASK )
        newIntent.putExtra( "restore", true )

        context.startActivity( newIntent )
    }
}