package com.lissnedux.music.blasting.compose.screens.setup.welcome

import androidx.compose.foundation.background
import com.lissnedux.music.blasting.compose.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.Button
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lissnedux.music.blasting.compose.data.variables.SCREEN_PADDING
import com.lissnedux.music.blasting.compose.ui.composables.spacers.MediumHeightSpacer
import com.lissnedux.music.blasting.compose.ui.composables.text.TitleMedium

@Composable
fun WelcomeScreen(
    onNextClicked: () -> Unit = {}
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SCREEN_PADDING)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(fill = true, weight = 1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .height(80.dp)
                    .width(80.dp)
                    .clip(RoundedCornerShape(percent = 100))
                    .background(MaterialTheme.colorScheme.primary),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier
                        .height(60.dp),
                    painter = painterResource(id = androidx.compose.foundation.layout.R.drawable.play_empty),
                    contentDescription = "Logo Play Button",
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }

            MediumHeightSpacer()

            TitleMedium(
                text = stringResource(id = androidx.compose.foundation.layout.R.string.WelcomeToMusicBlaster),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f, fill = true)
            )

            Button(
                onClick = {
                    onNextClicked()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {

                Text(
                    text = stringResource(id = androidx.compose.foundation.layout.R.string.Next),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}