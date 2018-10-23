package com.crskdev.photosurfer.services.playwave

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED
import android.os.Build
import com.crskdev.photosurfer.presentation.playwave.TrackingPlaywaveSoundPlayer

/**
 * Created by Cristian Pela on 19.10.2018.
 */
interface PlaywaveSoundPlayer {

    interface TrackListener {

        fun onReady()

        fun onTrack(position: Long)

        fun complete()

    }

    fun load(songPath: String, duration: Long)

    fun play(position: Long)

    fun stop()

    fun pause()

    fun seekTo(position: Long)

    fun unload()

    fun setTrackListener(trackListener: TrackListener)

    fun release()
}

class PlaywaveSoundPlayerImpl : PlaywaveSoundPlayer {

    private var mediaPlayer: MediaPlayer? = null

    private var startMediaPlayerSeverFailedAttempts = 0

    private val trackingPlaywaveSoundPlayer = TrackingPlaywaveSoundPlayer()

    private var onPrepareListener: MediaPlayer.OnPreparedListener = MediaPlayer.OnPreparedListener {
        trackingPlaywaveSoundPlayer.makeReady()
        startMediaPlayerSeverFailedAttempts = 0
    }

    override fun pause() {
        mediaPlayer?.pause()
        trackingPlaywaveSoundPlayer.pause()
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        trackingPlaywaveSoundPlayer.release()
    }

    override fun load(songPath: String, duration: Long) {
        trackingPlaywaveSoundPlayer.load(songPath, duration)
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setAudioAttributes(AudioAttributes
                        .Builder()
                        //.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build())
            } else {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
            }
            setDataSource(songPath)
            setOnPreparedListener(onPrepareListener)
            setOnErrorListener { mp, what, extra ->
                if (what == MEDIA_ERROR_SERVER_DIED) {
                    startMediaPlayerSeverFailedAttempts += 1
                    if (startMediaPlayerSeverFailedAttempts < 3)
                        load(songPath, duration)
                    else {
                        throw Exception("Could not start player server")
                    }
                }
                true
            }
            prepareAsync()
        }
    }

    override fun play(position: Long) {
        mediaPlayer?.start()
        if (position > 0) {
            mediaPlayer?.seekTo(position.toInt())
        }
        trackingPlaywaveSoundPlayer.play(position)
    }

    override fun stop() {
        mediaPlayer?.stop()
        trackingPlaywaveSoundPlayer.stop()
    }

    override fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
        trackingPlaywaveSoundPlayer.seekTo(position)
    }

    //TODO maybe remove.. release is enough?
    override fun unload() {
        release()
    }

    override fun setTrackListener(trackListener: PlaywaveSoundPlayer.TrackListener) {
        trackingPlaywaveSoundPlayer.setTrackListener(trackListener)
    }

}