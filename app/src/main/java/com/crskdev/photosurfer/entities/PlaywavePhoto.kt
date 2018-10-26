package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.remote.PagingData
import java.util.*

/**
 * Created by Cristian Pela on 15.10.2018.
 */
class PlaywavePhoto(val id: String, val urls: EnumMap<ImageType, String>, val exists: Boolean): BaseEntity() {
    override val pagingData: PagingData? = null
}