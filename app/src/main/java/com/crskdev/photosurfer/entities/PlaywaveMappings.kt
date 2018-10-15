package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.local.playwave.PlaywaveContentEntity
import com.crskdev.photosurfer.data.local.playwave.PlaywaveEntity
import com.crskdev.photosurfer.data.local.playwave.PlaywaveWithPhotos
import com.crskdev.photosurfer.data.local.playwave.song.Song

/**
 * Created by Cristian Pela on 15.10.2018.
 */
fun PlaywaveEntity.toPlaywave(song: Song?): Playwave =
        Playwave(this.id, song, emptyList())

fun PlaywaveWithPhotos.toPlaywave(song: Song?): Playwave =
        Playwave(playwaveEntity.id,
                song,
                playwaveContents.asSequence().map {
                    it.toPlaywavePhoto()
                }.toList())

fun PlaywaveContentEntity.toPlaywavePhoto(): PlaywavePhoto =
        PlaywavePhoto(photoId, url, photoExists)

fun Playwave.toDB(): PlaywaveEntity =
        PlaywaveEntity().apply {
            songId = this@toDB.song?.id ?: -1
        }

fun PlaywavePhoto.toDb(playwaveId: Int): PlaywaveContentEntity =
        PlaywaveContentEntity().apply {
            this.playwaveId = playwaveId
            this.photoId = this@toDb.id
            this.url = this@toDb.url
            this.photoExists = this@toDb.exists
        }