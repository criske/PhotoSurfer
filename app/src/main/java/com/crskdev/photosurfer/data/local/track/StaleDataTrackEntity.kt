package com.crskdev.photosurfer.data.local.track

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Cristian Pela on 20.08.2018.
 */
@Entity(tableName = "stale_data_track")
class StaleDataTrackEntity {
    @PrimaryKey
    lateinit var table: String
    var time: Long = -1
}