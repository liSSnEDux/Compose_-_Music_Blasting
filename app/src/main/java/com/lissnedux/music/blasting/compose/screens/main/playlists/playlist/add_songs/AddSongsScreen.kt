package com.lissnedux.music.blasting.compose.screens.main.playlists.playlist.add_songs

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lissnedux.music.blasting.compose.R
import com.lissnedux.music.blasting.compose.data.variables.SCREEN_PADDING
import com.lissnedux.music.blasting.compose.activities.main.MainVM
import com.lissnedux.music.blasting.compose.ui.composables.ClickableMediumIcon
import com.lissnedux.music.blasting.compose.ui.composables.CustomText
import com.lissnedux.music.blasting.compose.ui.composables.CustomToolbar
import com.lissnedux.music.blasting.compose.ui.composables.spacers.MediumHeightSpacer
import com.lissnedux.music.blasting.compose.ui.composables.spacers.SmallWidthSpacer
import com.lissnedux.music.blasting.compose.ui.composables.spacers.XSmallHeightSpacer
import com.lissnedux.music.blasting.compose.screens.main.playlists.playlist.PlaylistScreenVM
import kotlinx.coroutines.launch

@Composable
fun AddSongsScreen(
    mainVM: MainVM,
    addSongsVM: AddSongsScreenVM,
    playlistVM: PlaylistScreenVM,
    playlistID: String,
    onGoBack: () -> Unit
) {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val surfaceColor = mainVM.surfaceColor.collectAsState().value

    val screenLoaded = addSongsVM.screenLoaded.collectAsState().value

    val songs = addSongsVM.songs.collectAsState().value

    val songsImages = mainVM.compressedSongsImages.collectAsState().value

    if (!screenLoaded) {
        addSongsVM.loadScreen(playlistID, mainVM)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(SCREEN_PADDING)
    ) {

        CustomToolbar(
            backText = stringResource(id = androidx.compose.foundation.layout.R.string.Playlist),
            onBackClick = {
                onGoBack()
            }
        )

        MediumHeightSpacer()

        if (screenLoaded) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f, fill = true)
            ) {

                items(
                    items = songs!!,
                    key = { it.id }
                ) { song ->

                    val songAlbumArt = songsImages?.find { it.albumID == song.albumID }?.albumArt

                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    addSongsVM.toggleSong(song.id)
                                },
                            verticalAlignment = Alignment.CenterVertically

                        ) {

                            AsyncImage(
                                model = remember { songAlbumArt ?: BitmapFactory.decodeResource(context.resources, androidx.compose.foundation.layout.R.drawable.record) },
                                contentDescription = null,
                                colorFilter = if (songAlbumArt == null) ColorFilter.tint(MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .height(70.dp)
                                    .width(70.dp)
                            )

                            SmallWidthSpacer()

                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(1f),
                                verticalArrangement = Arrangement.Top
                            ) {

                                CustomText(
                                    text = song.title
                                )

                                CustomText(
                                    text = song.artist
                                )
                            }

                            ClickableMediumIcon(
                                id = when (song.selected) {

                                    true -> androidx.compose.foundation.layout.R.drawable.check

                                    false -> androidx.compose.foundation.layout.R.drawable.plus
                                },
                                color = MaterialTheme.colorScheme.onSurface,
                                onClick = {

                                    addSongsVM.toggleSong(song.id)
                                }
                            )
                        }

                        XSmallHeightSpacer()
                    }
                }
            }

            MediumHeightSpacer()

            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = {

                    scope.launch {

                        addSongsVM.addSongs(
                            playlistID = playlistID,
                            onSuccess = {
                                onGoBack()
                            },
                            playlistVM = playlistVM,
                            mainVM = mainVM
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                CustomText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = androidx.compose.foundation.layout.R.string.Select),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}