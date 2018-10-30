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

    fun loadAndPlay(song: SongUI) {
        state.postValue(PlayingSongState.Prepare(song))
        soundPlayer.loadAndPlay(song.path, song.durationInt)
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
            is PlayingSongState.Playing -> PlayingSongState.Stopped(s.song!!)
                    .apply {
                        soundPlayer.stop()
                    }
            is PlayingSongState.Ready,
            is PlayingSongState.Stopped,
            is PlayingSongState.Completed -> PlayingSongState.Playing(s.song!!, 0, prettySongDuration(0))
                    .apply {
                        soundPlayer.loadAndPlay(song!!.path, song.durationInt, position)
                    }
            is PlayingSongState.Paused -> PlayingSongState.Playing(s.song!!, s.position, s.positionDisplay)
                    .apply {
                        soundPlayer.play(position)
                    }
            is PlayingSongState.Seeking -> {
                if (s.stateBeforeSeek is PlayingSongState.Paused) {
                    soundPlayer.play(s.position)
                } else {
                    soundPlayer.loadAndPlay(s.song!!.path, s.song.durationInt, s.position)
                }
                PlayingSongState.Playing(s.song!!, s.position, s.positionDisplay)
            }
            else -> throw IllegalStateException("State before PLAY/STOP not allowed: $s") // should not reach this
        }
        state.postValue(nextState)
    }

    fun seekTo(position: Int, confirmedToPlayAt: Boolean) {
        assert(state.value !is PlayingSongState.None
                || state.value !is PlayingSongState.Prepare) {
            "The state before SEEK must not be NONE or PREPARE"
        }
        val s = state.value!!
        if (confirmedToPlayAt) {
            assert(state.value is PlayingSongState.Seeking) {
                "The state before confirmation SEEK must be SEEK"
            }
            val stateBeforeSeek = (s as PlayingSongState.Seeking).stateBeforeSeek
            if (stateBeforeSeek is PlayingSongState.Playing) {
                // we dispatch the seek to system player
                // only if the last state before seek was PLAY(meaning sound is running while we're seeking)
                soundPlayer.seekTo(position)
            }
            state.postValue(PlayingSongState.Seeking(s.song!!, position, prettySongDuration(position.toLong()), stateBeforeSeek,
                    true))
        } else {
            val stateBeforeSeek = if (s is PlayingSongState.Seeking) {
                s.stateBeforeSeek
            } else {
                s
            }
            state.postValue(PlayingSongState.Seeking(s.song!!, position, prettySongDuration(position.toLong()),
                    stateBeforeSeek, false))
        }
    }


    override fun onReady() {
        assert(state.value is PlayingSongState.Prepare) {
            "The state before READY must be PREPARE"
        }
        state.value?.song?.let {
            state.postValue(PlayingSongState.Ready(it))
        }
    }

    override fun onTrack(position: Int) {
        assert(state.value !is PlayingSongState.None) {
            "The state before PLAYING must not be NONE"
        }
        state.value?.let {
            if (it !is PlayingSongState.Seeking || it.confirmedToPlayAt) { //while seeking and not confirmed from ui don't dispatch playing cues
                it.song?.let { song ->
                    state.postValue(PlayingSongState.Playing(song,
                            position,
                            prettySongDuration(position.toLong()))
                    )
                }

            }
        }
    }

    override fun complete() {
        assert(state.value is PlayingSongState.Playing) {
            "The state before COMPLETED must be PLAYING"
        }
        val song = state.value!!.song!!
        state.postValue(PlayingSongState.Completed(song, song.durationInt, prettySongDuration(song.durationInt.toLong())))
    }

}