package com.crskdev.photosurfer.services.playwave

/**
 * Created by Cristian Pela on 19.10.2018.
 */
interface PlaywaveSoundPlayer {

    interface TrackListener{

        fun onReady()

        fun onTrack(position: Long)

        fun complete()

    }

    fun load(songPath: String, duration: Long)

    fun play()

    fun stop()

    fun pause()

    fun seekTo(position: Long)

    fun unload()

    fun setTrackListener(trackListener: TrackListener)

    fun release()
}

class PlaywaveSoundPlayerImpl: PlaywaveSoundPlayer{

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun release() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun load(songPath: String, duration: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun play() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun seekTo(position: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unload() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setTrackListener(trackListener: PlaywaveSoundPlayer.TrackListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}