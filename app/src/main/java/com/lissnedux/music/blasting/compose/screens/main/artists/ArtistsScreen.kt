package com.lissnedux.music.blasting.compose.screens.main.artists

import android.content.res.Configuration
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lissnedux.music.blasting.compose.R
import com.lissnedux.music.blasting.compose.data.variables.SCREEN_PADDING
import com.lissnedux.music.blasting.compose.data.variables.XSMALL_SPACING
import com.lissnedux.music.blasting.compose.activities.main.MainVM
import com.lissnedux.music.blasting.compose.ui.composables.CustomTextField
import com.lissnedux.music.blasting.compose.ui.composables.ImageCard
import com.lissnedux.music.blasting.compose.ui.composables.spacers.MediumHeightSpacer
import com.lissnedux.music.blasting.compose.functions.getAppString
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArtistsScreen(
    mainVM: MainVM,
    artistsVM: ArtistsScreenVM,
    onArtistClicked: (artistID: Long) -> Unit
) {

    //States
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val configuration = LocalConfiguration.current

    val gridState = rememberLazyGridState()

    //Variables
    val screenLoaded = artistsVM.screenLoaded.collectAsState().value

    val searchText = artistsVM.searchText.collectAsState().value

    val menuExpanded = artistsVM.menuExpanded.collectAsState().value

    val artists = artistsVM.currentArtists.collectAsState().value

    val recentArtists = artistsVM.recentArtists.collectAsState().value

    val oldestArtists = artistsVM.oldestArtists.collectAsState().value

    val ascendentArtists = artistsVM.ascendentArtists.collectAsState().value

    val descendentArtists = artistsVM.descendentArtists.collectAsState().value

    val surfaceColor = mainVM.surfaceColor.collectAsState().value


    val gridCellsCount = when (configuration.orientation) {

        Configuration.ORIENTATION_PORTRAIT -> 2
        else -> 4
    }


    if (!screenLoaded) {
        artistsVM.loadScreen(mainVM)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(SCREEN_PADDING)
    ) {


        if (screenLoaded) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {

                    CustomTextField(
                        text = searchText,
                        placeholder = remember { getAppString(context, R.string.SearchArtists) },
                        textType = "text",
                        onTextChange = {

                            artistsVM.updateSearchText(it)

                            artistsVM.filterArtists()

                            scope.launch { gridState.scrollToItem(0) }
                        },
                        sideIcon = R.drawable.sort,
                        onSideIconClick = {
                            artistsVM.updateMenuExpanded(true)
                        },
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = {
                            artistsVM.updateMenuExpanded(false)
                        }
                    ) {

                        DropdownMenuItem(
                            text = { Text(text = remember { getAppString(context, R.string.SortByRecentlyAdded) }) },
                            onClick = {

                                artistsVM.updateSortType("Recent")

                                artistsVM.updateCurrentArtists(recentArtists)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = remember { getAppString(context, R.string.SortByOldestAdded) }) },
                            onClick = {

                                artistsVM.updateSortType("Oldest")

                                artistsVM.updateCurrentArtists(oldestArtists)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = remember { getAppString(context, R.string.SortByAscendent) }) },
                            onClick = {

                                artistsVM.updateSortType("Ascendent")

                                artistsVM.updateCurrentArtists(ascendentArtists)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = remember { getAppString(context, R.string.SortByDescendent) }) },
                            onClick = {

                                artistsVM.updateSortType("Descendent")

                                artistsVM.updateCurrentArtists(descendentArtists)
                            }
                        )
                    }
                }

                MediumHeightSpacer()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridCellsCount),
                    verticalArrangement = Arrangement.spacedBy(XSMALL_SPACING),
                    horizontalArrangement = Arrangement.spacedBy(XSMALL_SPACING),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxSize(),
                ) {

                    items(
                        items = artists!!,
                        key = { artist -> artist.artistID },
                    ) { artist ->

                        val albumArt = mainVM.songsImages.collectAsState().value?.first { it.albumID == artist.albumID }?.albumArt

                        ImageCard(
                            modifier = Modifier.animateItemPlacement(),
                            cardImage = remember { albumArt ?: BitmapFactory.decodeResource(context.resources, R.drawable.record) },
                            imageTint = if (albumArt == null) ColorFilter.tint(MaterialTheme.colorScheme.primary) else null,
                            cardText = remember { artist.artist },
                            onCardClicked = {

                                onArtistClicked(artist.artistID)
                            }
                        )
                    }
                }
            }
        }
    }
}