package com.crskdev.photosurfer.data.local

import androidx.room.Embedded
import com.crskdev.photosurfer.data.remote.PagingData

/**
 * Created by Cristian Pela on 25.08.2018.
 */
open class BaseDBEntity {
    var indexInResponse: Int = -1

    @Embedded
    var pagingData: PagingData? = null

    var lastUpdatedLocal: Long? = System.currentTimeMillis()
}
