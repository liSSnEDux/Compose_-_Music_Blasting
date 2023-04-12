package com.lissnedux.music.blasting.compose.screens.main.artist.select_cover

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.lissnedux.music.blasting.compose.R
import com.lissnedux.music.blasting.compose.ui.composables.CustomText
import com.lissnedux.music.blasting.compose.ui.composables.CustomToolbar
import com.lissnedux.music.blasting.compose.activities.main.MainVM
import com.lissnedux.music.blasting.compose.data.variables.SCREEN_PADDING
import com.lissnedux.music.blasting.compose.data.variables.SMALL_SPACING
import com.lissnedux.music.blasting.compose.ui.composables.spacers.MediumHeightSpacer
import com.lissnedux.music.blasting.compose.ui.composables.spacers.SmallHeightSpacer
import com.lissnedux.music.blasting.compose.functions.getAppString
import com.lissnedux.music.blasting.compose.functions.getBitmapFromVector
import com.lissnedux.music.blasting.compose.screens.main.artist.ArtistScreenVM
import java.io.ByteArrayOutputStream

@Composable
fun SelectArtistCoverScreen(
    mainVM: MainVM,
    selectArtistCoverVM: SelectArtistCoverScreenVM,
    artistVM: ArtistScreenVM,
    artistName: String,
    artistID: Long,
    onGoBack: () -> Unit,
    onGetArtistCover: () -> Unit
){

    val context = LocalContext.current

    val surfaceColor = mainVM.surfaceColor.collectAsState().value

    val screenLoaded = selectArtistCoverVM.screenLoaded.collectAsState().value

    val covers = selectArtistCoverVM.covers.collectAsState().value

    selectArtistCoverVM.onGoBack = {
        onGoBack()
    }

    if(!screenLoaded){
        selectArtistCoverVM.loadScreen(artistName, artistID, artistVM)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(surfaceColor)
            .padding(SCREEN_PADDING)
    ) {

        CustomToolbar(
            backText = remember { getAppString(context, R.string.Artist) },
            onBackClick = {onGoBack()},
        )

        MediumHeightSpacer()

        if(screenLoaded){

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(percent = 100))
                        .clip(RoundedCornerShape(percent = 100))
                        .clickable {

                            selectArtistCoverVM.clearArtistCover()
                        }
                        .padding(SMALL_SPACING),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    CustomText(
                        text = remember{ getAppString(context, R.string.UseDefault) },
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                SmallHeightSpacer()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 1.dp, color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(percent = 100))
                        .clip(RoundedCornerShape(percent = 100))
                        .clickable {
                            onGetArtistCover()
                        }
                        .padding(SMALL_SPACING),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    CustomText(
                        text = remember{ getAppString(context, R.string.SelectLocalCover) },
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                MediumHeightSpacer()

                if(covers != null){

                    CustomText(
                        text = remember{ getAppString(context, R.string.OnlineResults) },
                        weight = FontWeight.Bold,
                        size = 20.sp
                    )

                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        content = {

                            items(
                                items = covers,
                                key = {it.id}
                            ){ result ->

                                val artistPicture = remember{ mutableStateOf<Bitmap?>(null) }

                                Image(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {

                                            if(artistPicture.value != null){

                                                val baos = ByteArrayOutputStream()

                                                artistPicture.value!!.compress(Bitmap.CompressFormat.PNG, 100, baos)

                                                val b = baos.toByteArray()

                                                val encodedImage = Base64.encodeToString(b, Base64.DEFAULT)

                                                selectArtistCoverVM.updateArtistCover(encodedImage)
                                            }
                                        },
                                    bitmap = if(artistPicture.value == null)
                                        remember{ getBitmapFromVector(context, R.drawable.loading).asImageBitmap()}
                                    else
                                        artistPicture.value!!.asImageBitmap(),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    colorFilter = if(artistPicture.value == null) ColorFilter.tint(MaterialTheme.colorScheme.primary) else null
                                )

                                try {
                                    val imageUrl = result.cover_image

                                    Glide.with(context)
                                        .asBitmap()
                                        .load(imageUrl)
                                        .into(object : CustomTarget<Bitmap>() {
                                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {

                                                artistPicture.value = resource
                                            }

                                            override fun onLoadCleared(placeholder: Drawable?) {}
                                        })
                                } catch (ignore: Exception) {}
                            }
                        }
                    )
                }
            }
        }
    }
}