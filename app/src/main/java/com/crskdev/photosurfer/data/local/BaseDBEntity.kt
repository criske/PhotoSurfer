package com.crskdev.photosurfer.data.local

import androidx.room.Embedded
import com.crskdev.photosurfer.data.remote.PagingData

/**
 * Created by Cristian Pela on 25.08.2018.
 */
open class BaseDBEntity {
    var indexInResponse: Int = -1
    //paging data
//    var total: Int? = null
//    var curr: Int? = null
//    var prev: Int? = null
//    var next: Int? = null

    @Embedded
    var pagingData: PagingData? = null
}
