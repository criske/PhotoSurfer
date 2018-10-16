package com.crskdev.photosurfer.data.local.playwave

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.crskdev.photosurfer.data.local.Contract

/**
 * Created by Cristian Pela on 14.10.2018.
 */
@Entity(tableName = Contract.TABLE_PLAYWAVE_CONTENT,
        foreignKeys = [ForeignKey(entity = PlaywaveEntity::class,
                parentColumns = ["id"], childColumns = ["playwaveId"])],
        indices = [Index(value = ["playwaveId"])])
class PlaywaveContentEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Int = -1
    var playwaveId: Int = -1
    lateinit var photoId: String
    lateinit var url: String
    var photoExists: Boolean = true
}
