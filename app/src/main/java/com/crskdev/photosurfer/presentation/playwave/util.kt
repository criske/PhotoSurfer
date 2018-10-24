package com.crskdev.photosurfer.presentation.playwave

import android.net.Uri
import com.crskdev.photosurfer.data.local.playwave.song.Song
import com.crskdev.photosurfer.entities.Playwave
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Created by Cristian Pela on 21.10.2018.
 */
fun prettySongDuration(durationMillis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) - TimeUnit.MINUTES.toSeconds(minutes)
    val minutesFormat = if (minutes < 10) "%02d" else "%d"
    val secondsFormat = if (seconds < 10) "%02d" else "%d"
    return String.format("$minutesFormat:$secondsFormat", minutes, seconds)
}

internal fun positionPercent(realPosition: Long, duration: Long): Int =
        realPosition.toFloat().div(duration).times(100).roundToInt()

internal fun prettySongPosition(percent: Int, duration: Long): String =
        prettySongDuration(realPosition(percent, duration))

internal fun realPosition(percent: Int, duration: Long): Long =
        percent.toFloat().div(100).times(duration).roundToLong()


internal fun Song.toUI(): SongUI {
    val albumPath = takeIf { it.exists }?.let {
        val albumArtUri = Uri.parse("content://media/external/audio/albumart")
        Uri.withAppendedPath(albumArtUri, albumId.toString())
    }
    return SongUI(id, albumId, path, title, artist, prettySongDuration(duration), toString(), duration.toInt(), exists,
            albumPath)
}

internal fun SongUI.toEntity(): Song = Song(id, albumId, path, title, artist, durationInt.toLong(), exists)

internal fun Playwave.toUI(): PlaywaveUI = PlaywaveUI(this.id, this.title, this.size, this.song.toUI(),
        !this.song.exists, this.photos)

internal fun PlaywaveUI.toEntity(): Playwave = Playwave(id, title, size,
        song?.toEntity() ?: Song.NO_SONG, photos)
