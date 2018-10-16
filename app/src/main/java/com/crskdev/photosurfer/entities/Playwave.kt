package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.local.playwave.song.Song
import com.crskdev.photosurfer.data.remote.PagingData

/**
 * Created by Cristian Pela on 15.10.2018.
 */
data class Playwave(val id: Int, val title :String, val song: Song, val photos: List<PlaywavePhoto>) : BaseEntity() {
    override val pagingData: PagingData? = null
}