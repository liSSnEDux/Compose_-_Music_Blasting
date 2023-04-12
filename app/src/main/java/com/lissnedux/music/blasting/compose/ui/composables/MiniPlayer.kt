package com.lissnedux.music.blasting.compose.ui.composables

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lissnedux.music.blasting.compose.R
import com.lighttigerxiv.simple.mp.compose.data.variables.SMALL_SPACING
import com.lissnedux.music.blasting.compose.activities.main.MainVM
import com.lissnedux.music.blasting.compose.ui.composables.spacers.SmallWidthSpacer
import com.lissnedux.music.blasting.compose.functions.getBitmapFromVector

@Composable
fun MiniPlayer(
    mainVM: MainVM
) {


    val context = LocalContext.current

    val selectedSong = mainVM.selectedSong.collectAsState().value

    val songAlbumArt = mainVM.songAlbumArt.collectAsState().value

    val musicPlaying = mainVM.musicPlayling.collectAsState().value

    val playPauseIcon = if (musicPlaying) {
        remember { getBitmapFromVector(context, R.drawable.icon_pause_solid) }
    } else {
        remember { getBitmapFromVector(context, R.drawable.icon_play_solid) }
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(SMALL_SPACING)
    ) {

        if (selectedSong != null) {

            Image(
                bitmap = (songAlbumArt ?: BitmapFactory.decodeResource(context.resources, R.drawable.record)).asImageBitmap(),
                colorFilter = if (songAlbumArt == null) ColorFilter.tint(MaterialTheme.colorScheme.primary) else null,
                contentDescription = "Song Album Art",
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(7.dp))
            )

            SmallWidthSpacer()

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f, fill = true),
                verticalArrangement = Arrangement.Top
            ) {

                CustomText(
                    text = selectedSong.title,
                    weight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                CustomText(
                    text = selectedSong.artist
                )
            }

            SmallWidthSpacer()

            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {

                Image(
                    modifier = Modifier
                        .height(20.dp)
                        .width(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .clickable {
                            mainVM.pauseResumeMusic()
                        },
                    bitmap = playPauseIcon.asImageBitmap(),
                    contentDescription = "Play/Pause button",
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            }

            SmallWidthSpacer()
        }
    }
}