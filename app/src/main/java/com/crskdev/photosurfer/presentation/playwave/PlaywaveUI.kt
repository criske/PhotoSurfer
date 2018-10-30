package com.crskdev.photosurfer.presentation.playwave

import com.crskdev.photosurfer.entities.PlaywavePhoto

/**
 * Created by Cristian Pela on 22.10.2018.
 */
data class PlaywaveUI(val id: Int, val title: String, val size: Int, val song: SongUI?, val hasError: Boolean,
                      val photos: List<PlaywavePhoto>) {
    companion object {
        val NONE = PlaywaveUI(-1, "", 0, null, false, emptyList())
    }
}