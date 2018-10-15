package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.remote.PagingData

/**
 * Created by Cristian Pela on 15.10.2018.
 */
class PlaywavePhoto(val id: String, val url: String, val exists: Boolean): BaseEntity() {
    override val pagingData: PagingData? = null
}