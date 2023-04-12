package com.lissnedux.music.blasting.compose.screens.main.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.lissnedux.music.blasting.compose.activities.main.MainVM
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PlayerScreenVM(application: Application) : AndroidViewModel(application) {

    //************************************************
    // Variables
    //************************************************

    var mainVM: MainVM? = null

    private val _screenLoaded = MutableStateFlow(false)
    val screenLoaded = _screenLoaded.asStateFlow()

    private val _menuOpened = MutableStateFlow(false)
    val menuOpened = _menuOpened.asStateFlow()
    fun updateMenuOpened(newValue:Boolean) {
        _menuOpened.update { newValue }
    }

    //************************************************
    // Functions
    //************************************************

    fun loadScreen(vm: MainVM){

        mainVM = vm

        _screenLoaded.update { true }
    }

    //************************************************
    // Callbacks
    //************************************************


}