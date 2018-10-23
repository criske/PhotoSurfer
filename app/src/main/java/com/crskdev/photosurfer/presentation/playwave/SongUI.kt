package com.crskdev.photosurfer.presentation.playwave

data class SongUI(
        val id: Long,
        val path: String,
        val title: String,
        val artist: String,
        val duration: String,
        val fullInfo: String,
        val durationLong: Long,
        val exists: Boolean,
        val albumPath: String? = null){

}