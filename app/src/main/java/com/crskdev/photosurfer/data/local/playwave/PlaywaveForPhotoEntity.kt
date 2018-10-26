package com.crskdev.photosurfer.data.local.playwave

import androidx.room.ColumnInfo

/**
 * Created by Cristian Pela on 25.10.2018.
 */
class PlaywaveForPhotoEntity(
        @ColumnInfo(name = "id")
        val playwaveId: Int,
        @ColumnInfo(name = "title")
        val playwaveTitle: String,
        @ColumnInfo(name = "size")
        val playwaveSize: Int,
        val photoId: String?)