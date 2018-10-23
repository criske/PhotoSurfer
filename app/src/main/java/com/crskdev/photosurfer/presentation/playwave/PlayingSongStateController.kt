package com.crskdev.photosurfer.presentation.playwave

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crskdev.photosurfer.services.playwave.PlaywaveSoundPlayer
import java.lang.IllegalStateException

class PlayingSongStateController(private val soundPlayer: PlaywaveSoundPlayer) : PlaywaveSoundPlayer.TrackListener {

    private val state: MutableLiveData<PlayingSongState> = MutableLiveData<PlayingSongState>()
            .apply {
                PlayingSongState.None
            }

    init {
        soundPlayer.setTrackListener(this)
    }

    fun getStateLiveData(): LiveData<PlayingSongState> = state

    fun clear() {
        state.postValue(PlayingSongState.None)
        soundPlayer.unload()
    }

    fun release() = soundPlayer.release()

    fun prepare(song: SongUI) {
        state.postValue(PlayingSongState.Prepare(song, 0, prettySongDuration( 0)))
        soundPlayer.load(song.path, song.durationLong)
    }

    fun pause() {
        assert(state.value is PlayingSongState.Playing) {
            "The state before PAUSE must be PLAYING"
        }
        val s = state.value!! as PlayingSongState.Playing
        state.postValue(PlayingSongState.Paused(s.song!!, s.position, s.positionDisplay))
        soundPlayer.pause()
    }

    fun justStop() {
        state.postValue(PlayingSongState.None)
        soundPlayer.stop()
    }

    fun playOrStop() {
        val s = state.value!!
        val nextState = when (s) {
            is PlayingSongState.Playing -> PlayingSongState.Stopped(s.song!!, 0, prettySongDuration(0))
                    .apply {
                        soundPlayer.stop()
                    }
            is PlayingSongState.Stopped -> PlayingSongState.Playing(s.song!!, 0, prettySongDuration(0))
                    .apply {
                        soundPlayer.play(0)
                    }
            is PlayingSongState.Ready,
            is PlayingSongState.Completed -> PlayingSongState.Playing(s.song!!, 0, prettySongDuration(0))
                    .apply {
                        soundPlayer.play(0)
                    }
            is PlayingSongState.Paused -> PlayingSongState.Playing(s.song!!, s.position, s.positionDisplay)
                    .apply {
                        soundPlayer.play(position)
                    }
            is PlayingSongState.Seeking -> PlayingSongState.Playing(s.song!!, s.position, s.positionDisplay)
                    .apply {
                        soundPlayer.play(position)
                    }
            else -> throw IllegalStateException("State before PLAY/STOP not allowed: $s") // should not reach this
        }
        state.postValue(nextState)
    }

    fun seekTo(position: Long, confirmedToPlayAt: Boolean) {
        assert(state.value !is PlayingSongState.None
                || state.value !is PlayingSongState.Prepare) {
            "The state before SEEK must not be NONE or PREPARE"
        }
        val s = state.value!!
        if (confirmedToPlayAt) {
            assert(state.value is PlayingSongState.Seeking) {
                "The state before confirmation SEEK must be SEEK"
            }

            println("SEEK THREAD " + Thread.currentThread())
            val stateBeforeSeek = (s as PlayingSongState.Seeking).stateBeforeSeek
            if (stateBeforeSeek is PlayingSongState.Playing) {
                // we dispatch the seek to system player
                // only if the last state before seek was PLAY(meaning sound is running while we're seeking)
                soundPlayer.seekTo(position)
            }
            state.postValue(PlayingSongState.Seeking(s.song!!, position, prettySongDuration(position), stateBeforeSeek,
                    true))
        } else {
            val stateBeforeSeek = if (s is PlayingSongState.Seeking) {
                s.stateBeforeSeek
            } else {
                s
            }
            state.postValue(PlayingSongState.Seeking(s.song!!, position, prettySongDuration(position),
                    stateBeforeSeek, false))
        }
    }


    override fun onReady() {
        assert(state.value is PlayingSongState.Prepare) {
            "The state before READY must be PREPARE"
        }
        state.postValue(PlayingSongState.Ready(state.value!!.song!!, 0, prettySongDuration(0)))
    }

    override fun onTrack(position: Long) {
        assert(state.value !is PlayingSongState.None) {
            "The state before PLAYING must not be NONE"
        }
        val s = state.value!!
        if (s !is PlayingSongState.Seeking || s.confirmedToPlayAt) { //while seeking and not confirmed from ui don't dispatch playing cues
            val song = s.song!!
            state.postValue(PlayingSongState.Playing(song,
                    position,
                    prettySongDuration(position))
            )
        }
    }

    override fun complete() {
        assert(state.value is PlayingSongState.Playing) {
            "The state before COMPLETED must be PLAYING"
        }
        val song = state.value!!.song!!
        state.postValue(PlayingSongState.Completed(song, song.durationLong, prettySongDuration(song.durationLong)))
    }

}