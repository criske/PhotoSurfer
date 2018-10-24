package com.crskdev.photosurfer.presentation.playwave

import android.net.Uri

data class SongUI(
        val id: Long,
        val albumId: Long,
        val path: String,
        val title: String,
        val artist: String,
        val duration: String,
        val fullInfo: String,
        val durationInt: Int,
        val exists: Boolean,
        val albumPath: Uri? = null){

}