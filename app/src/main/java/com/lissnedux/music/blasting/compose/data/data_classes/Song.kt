package com.lissnedux.music.blasting.compose.data.data_classes

data class Song(

    val id: Long,
    var path: String,
    val title: String,
    val album: String,
    val albumID: Long,
    val duration: Int,
    val artist: String,
    val artistID: Long,
    val year: Int,
    val genre: String,
    var selected: Boolean = false
)