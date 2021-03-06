package com.crskdev.photosurfer.data.local.playwave

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.crskdev.photosurfer.data.local.Contract

/**
 * Created by Cristian Pela on 14.10.2018.
 */
@Entity(tableName = Contract.TABLE_PLAYWAVE,
        indices = [Index(value = ["id"])])
class PlaywaveEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    lateinit var title: String
    var size: Int = 0
    var songId: Long = -1
    var albumId: Long = -1
    lateinit var songTitle: String
    lateinit var songArtist: String
    lateinit var songPath: String
    var songDuration: Long = 0
}