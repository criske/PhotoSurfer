package com.crskdev.photosurfer.entities

import com.crskdev.photosurfer.data.local.playwave.PlaywaveContentEntity
import com.crskdev.photosurfer.data.local.playwave.PlaywaveEntity
import com.crskdev.photosurfer.data.local.playwave.PlaywaveWithPhotos
import com.crskdev.photosurfer.data.local.playwave.song.Song

/**
 * Created by Cristian Pela on 15.10.2018.
 */
fun PlaywaveEntity.toPlaywave(exists: Boolean, size: Int): Playwave =
        Playwave(this.id, title, size, Song(
                songId,
                albumId,
                songPath,
                songTitle,
                songArtist,
                songDuration,
                exists), emptyList())

fun PlaywaveWithPhotos.toPlaywave(exists: Boolean): Playwave =
        Playwave(playwaveEntity.id,
                playwaveEntity.title,
                playwaveContents.size,
                Song(playwaveEntity.songId,
                        playwaveEntity.albumId,
                        playwaveEntity.songPath,
                        playwaveEntity.songTitle,
                        playwaveEntity.songArtist,
                        playwaveEntity.songDuration,
                        exists
                ),
                playwaveContents.asSequence().map {
                    it.toPlaywavePhoto()
                }.toList())

fun PlaywaveContentEntity.toPlaywavePhoto(): PlaywavePhoto =
        PlaywavePhoto(photoId, url, photoExists)

fun Playwave.toDB(): PlaywaveEntity =
        PlaywaveEntity().apply {
            songId = this@toDB.song.id
            albumId = this@toDB.song.albumId
            title = this@toDB.title
            songTitle = this@toDB.song.title
            songArtist = this@toDB.song.artist
            songDuration = this@toDB.song.duration
            songPath = this@toDB.song.path
        }

fun PlaywavePhoto.toDb(playwaveId: Int): PlaywaveContentEntity =
        PlaywaveContentEntity().apply {
            this.playwaveId = playwaveId
            this.photoId = this@toDb.id
            this.url = this@toDb.url
            this.photoExists = this@toDb.exists
        }