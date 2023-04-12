package com.lissnedux.music.blasting.compose.screens.main.settings.themes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.lissnedux.music.blasting.compose.R
import com.lissnedux.music.blasting.compose.data.variables.SCREEN_PADDING
import com.lissnedux.music.blasting.compose.ui.composables.CustomToolbar
import com.lissnedux.music.blasting.compose.ui.composables.ThemeSelector
import com.lissnedux.music.blasting.compose.functions.getAppString
import com.lissnedux.music.blasting.compose.activities.main.MainVM
import com.lissnedux.music.blasting.compose.settings.SettingsVM
import com.lissnedux.music.blasting.compose.ui.composables.spacers.MediumHeightSpacer

@Composable
fun ThemesScreen(
    mainVM: MainVM,
    settingsVM: SettingsVM,
    onBackClick: () -> Unit
) {

    val context = LocalContext.current
    val themeAccent = settingsVM.themeAccentSetting.collectAsState().value


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(mainVM.surfaceColor.collectAsState().value)
            .padding(SCREEN_PADDING)
    ) {

        CustomToolbar(
            backText = remember { getAppString(context, R.string.Settings) },
            onBackClick = { onBackClick() }
        )

        MediumHeightSpacer()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f, fill = true)
                .verticalScroll(rememberScrollState())
        ) {

            ThemeSelector(
                selectedTheme = themeAccent,
                onThemeClick = {
                    settingsVM.updateThemeAccentSetting(it)
                }
            )
        }
    }
}