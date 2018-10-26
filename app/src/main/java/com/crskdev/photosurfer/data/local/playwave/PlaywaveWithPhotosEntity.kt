package com.crskdev.photosurfer.data.local.playwave

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Created by Cristian Pela on 14.10.2018.
 */
class PlaywaveWithPhotosEntity() {
    @Embedded
    lateinit var playwaveEntity: PlaywaveEntity

    @Relation(entity = PlaywaveContentEntity::class, parentColumn = "id", entityColumn = "playwaveId")
    lateinit var playwaveContents: List<PlaywaveContentEntity>
}