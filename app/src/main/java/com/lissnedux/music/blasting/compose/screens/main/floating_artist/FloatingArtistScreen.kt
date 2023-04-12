package com.lissnedux.music.blasting.compose.screens.main.floating_artist

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lissnedux.music.blasting.compose.R
import com.lissnedux.music.blasting.compose.data.variables.SCREEN_PADDING
import com.lissnedux.music.blasting.compose.activities.main.MainVM
import com.lissnedux.music.blasting.compose.settings.SettingsVM
import com.lissnedux.music.blasting.compose.ui.composables.CustomText
import com.lissnedux.music.blasting.compose.ui.composables.CustomToolbar
import com.lissnedux.music.blasting.compose.ui.composables.PlayAndShuffleRow
import com.lissnedux.music.blasting.compose.ui.composables.SongItem
import com.lissnedux.music.blasting.compose.ui.composables.spacers.MediumHeightSpacer
import com.lissnedux.music.blasting.compose.ui.composables.spacers.SmallHeightSpacer
import com.lissnedux.music.blasting.compose.functions.getAppString
import moe.tlaster.nestedscrollview.VerticalNestedScrollView
import moe.tlaster.nestedscrollview.rememberNestedScrollViewState

@Composable
fun FloatingArtistScreen(
    mainVM: MainVM,
    settingsVM: SettingsVM,
    artistID: Long,
    artistVM: FloatingArtistScreenVM,
    onBackClicked: () -> Unit
) {

    val context = LocalContext.current

    val nestedScrollViewState = rememberNestedScrollViewState()

    val surfaceColor = mainVM.surfaceColor.collectAsState().value

    val screenLoaded = artistVM.screenLoaded.collectAsState().value

    val selectedSong = mainVM.selectedSong.collectAsState().value

    val artistName = artistVM.artistName.collectAsState().value

    val artistCover = artistVM.artistCover.collectAsState().value

    val tintCover = artistVM.tintCover.collectAsState().value

    val songs = artistVM.artistSongs.collectAsState().value


    if (!screenLoaded) {
        artistVM.loadScreen(artistID, mainVM, settingsVM)
    }


    VerticalNestedScrollView(
        modifier = Modifier
            .background(surfaceColor)
            .padding(SCREEN_PADDING),
        state = nestedScrollViewState,
        header = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {

                CustomToolbar(
                    backText = remember { getAppString(context, R.string.Artists) },
                    onBackClick = { onBackClicked() }
                )

                SmallHeightSpacer()

                Image(
                    bitmap = artistCover.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = if (tintCover) ColorFilter.tint(MaterialTheme.colorScheme.primary) else null,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .aspectRatio(1f)
                        .align(Alignment.CenterHorizontally)
                        .padding(20.dp)
                        .clip(RoundedCornerShape(14.dp))
                )

                CustomText(
                    modifier = Modifier.fillMaxWidth(),
                    text = artistName,
                    textAlign = TextAlign.Center,
                    size = 20.sp,
                    weight = FontWeight.Bold
                )

                MediumHeightSpacer()
            }
        },
        content = {

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                if (screenLoaded) {

                    PlayAndShuffleRow(
                        surfaceColor = surfaceColor,
                        onPlayClick = {
                            mainVM.unshuffleAndPlay(songs!!, 0)
                        },
                        onSuffleClick = {
                            mainVM.shuffleAndPlay(songs!!)
                        }
                    )

                    MediumHeightSpacer()

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        content = {

                            items(
                                items = songs!!,
                                key = { song -> song.id }
                            ) { song ->

                                SongItem(
                                    song = song,
                                    songAlbumArt = mainVM.songsImages.collectAsState().value?.find { it.albumID == song.albumID }!!.albumArt,
                                    highlight = song.path == selectedSong?.path,
                                    onSongClick = { mainVM.selectSong(songs, songs.indexOf(song)) }
                                )
                            }
                        }
                    )
                }
            }
        }
    )
}