package com.lissnedux.music.blasting.compose.ui.composables.spacers

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lissnedux.music.blasting.compose.data.variables.MEDIUM_SPACING

@Composable
fun MediumWidthSpacer(){
    Spacer(modifier = Modifier.width(MEDIUM_SPACING))
}