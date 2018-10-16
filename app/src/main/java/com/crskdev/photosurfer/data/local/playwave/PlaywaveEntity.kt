package com.crskdev.photosurfer.data.local.playwave

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.crskdev.photosurfer.data.local.Contract

/**
 * Created by Cristian Pela on 14.10.2018.
 */
@Entity(tableName = Contract.TABLE_PLAYWAVE,
        indices = [Index(value = ["songId"])])
class PlaywaveEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Int = -1
    var songId: Int = -1
    lateinit var title: String
}